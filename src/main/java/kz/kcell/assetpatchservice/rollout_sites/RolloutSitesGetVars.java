package kz.kcell.assetpatchservice.rollout_sites;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import static org.apache.xmlbeans.impl.piccolo.xml.Piccolo.STRING;


@Slf4j
@Service
public class RolloutSitesGetVars {
    @Autowired
    private JdbcTemplate jdbcTemplate;
    private final String catalogsUrl;


    private RolloutSitesGetVars(@Value("${catalogs.url:https://catalogs.test-flow.kcell.kz}") String catalogsUrl) {
        this.catalogsUrl = catalogsUrl;
    }

    public void reader() throws IOException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        String excelFilePath = "src/main/resources/rollout-sites/Адреса сайтов.xlsx";

        FileInputStream inputStream = new FileInputStream(excelFilePath);
        Workbook workbook = new XSSFWorkbook(inputStream);
        Sheet sheet = workbook.getSheetAt(0);
        List<String> siteNames = new ArrayList<>();
        for (Row row : sheet) {
            Cell cell = row.getCell(1);
            if (!Arrays.asList("ID", "Sitename", "Address").contains(cell.getStringCellValue())) {
                siteNames.add(cell.getStringCellValue());
            }
        }
        workbook.close();
        inputStream.close();

        SSLContextBuilder builder = new SSLContextBuilder();
        builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(builder.build());
        CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(sslsf).build();

        HttpGet httpGet = new HttpGet(catalogsUrl + "/camunda/catalogs/api/get/id/32");
        HttpResponse httpResponse = httpclient.execute(httpGet);

        HttpEntity entity = httpResponse.getEntity();
        String content = EntityUtils.toString(entity);
        JSONObject result = new JSONObject(content);
        JSONArray jsonArray = result.getJSONObject("data").getJSONArray("$list");


        String sql = "select  region_id, city_id, street, building, note, cadastral_number from adresses where id=( SELECT address_id  FROM facilities  WHERE id =(select facility_id from sites where site_name = ?))";
        List<DuplicateSites> duplicateSites = new ArrayList<>();
        List<List<DuplicateSites>> duplicates = new ArrayList<>();
        List<DuplicateSites> sitesList = new ArrayList<>();
        List<List<DuplicateSites>> sites = new ArrayList<>();

        for (String siteName : siteNames) {
            int count = jdbcTemplate.queryForObject("select COUNT(*) from sites where site_name = ?", new Object[]{siteName}, Integer.class);
            if (count > 1) {
                duplicateSites = jdbcTemplate.query(
                        "select id from sites where site_name = ?",
                        new Object[]{siteName}, // Параметры для PreparedStatement
                        (rs, rowNum) -> {
                            DuplicateSites duplicateSite = new DuplicateSites();
                            duplicateSite.setSiteName(siteName);
                            duplicateSite.setId(rs.getLong("id"));
                            return duplicateSite;
                        }
                );
                duplicates.add(duplicateSites);
            } else if (count == 0) {
                log.info("SiteName is not found = {}", siteName);
            } else {
                Long id = jdbcTemplate.queryForObject("select id from sites where site_name = ?", new Object[]{siteName}, Long.class);
                sitesList = jdbcTemplate.query(
                        sql,
                        new Object[]{siteName}, // Параметры для PreparedStatement
                        (rs, rowNum) -> {
                            DuplicateSites duplicateSite = new DuplicateSites();
                            duplicateSite.setId(id);
                            duplicateSite.setSiteName(siteName);
                            switch ((int)rs.getLong("region_id")) {
                                case 1:
                                    duplicateSite.setRegion_name("Almaty");
                                    break;
                                case 2:
                                    duplicateSite.setRegion_name("Astana");
                                    break;
                                case 3:
                                    duplicateSite.setRegion_name("North & Central");
                                    break;
                                case 4:
                                    duplicateSite.setRegion_name("East");
                                    break;
                                case 5:
                                    duplicateSite.setRegion_name("South");
                                    break;
                                case 6:
                                    duplicateSite.setRegion_name("West");
                                    break;
                                default:
                                    break;
                            }
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject oj = jsonArray.getJSONObject(i);
                                if (oj.getLong("id") == rs.getLong("city_id")) {
                                    duplicateSite.setCity_name(oj.getString("value"));
                                    break;
                                }
                            }
                            duplicateSite.setStreet(rs.getString("street"));
                            duplicateSite.setBuilding(rs.getString("building"));
                            duplicateSite.setNote(rs.getString("note"));
                            duplicateSite.setCadastral_number(rs.getString("cadastral_number"));

                            return duplicateSite;
                        }
                );
                sites.add(sitesList);
            }
        }

        Workbook workbookOut = new XSSFWorkbook();
        Sheet sheetOut = workbookOut.createSheet("Sheet1");


        int rowNum = 1;
        for (List<DuplicateSites> duplicateSite : duplicates) {
            for (DuplicateSites rowData : duplicateSite) {
                Row row = sheetOut.createRow(rowNum++);

                Cell cell = row.createCell(0);
                cell.setCellValue((String) rowData.getSiteName());
                Cell cellId = row.createCell(1);
                cellId.setCellValue((Long) rowData.getId());
            }
        }
        try (FileOutputStream outputStream = new FileOutputStream("src/main/resources/rollout-sites/1890-dublicates.xlsx")) {
            workbookOut.write(outputStream);
        }

        workbookOut.close();


        Workbook workbookOutSite = new XSSFWorkbook();
        Sheet sheetOutSite = workbookOutSite.createSheet("Sheet1");


        int rowNumSite = 1;
        for (List<DuplicateSites> site : sites) {

            for (DuplicateSites rowData : site) {
                Row row = sheetOutSite.createRow(rowNumSite++);
                Cell cellId = row.createCell(0);
                cellId.setCellValue((Long) rowData.getId());
                Cell cellSiteName = row.createCell(1);
                cellSiteName.setCellValue((String) rowData.getSiteName());
                Cell cellAddr = row.createCell(2);
                String address = rowData.getRegion_name();
                if (rowData.getCity_name() != null) {
                    address += ", " + rowData.getCity_name();
                }
                if (rowData.getStreet() != null) {
                    address += ", " + rowData.getStreet();
                }
                if (rowData.getBuilding() != null) {
                    address += ", " + rowData.getBuilding();
                }
                if (rowData.getNote() != null) {
                    address += ", " + rowData.getNote();
                }
                if (rowData.getCadastral_number() != null) {
                    address += ", " + rowData.getCadastral_number();
                }
                cellAddr.setCellValue((String) address);
            }
        }
        try (FileOutputStream outputStream = new FileOutputStream("src/main/resources/rollout-sites/Адреса сайтов-1990.xlsx")) {
            workbookOutSite.write(outputStream);
        }
        workbookOutSite.close();

    }


}
