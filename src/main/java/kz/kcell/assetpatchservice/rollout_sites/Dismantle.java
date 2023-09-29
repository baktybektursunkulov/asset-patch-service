package kz.kcell.assetpatchservice.rollout_sites;

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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Dismantle {
    public void reader() throws IOException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {


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
