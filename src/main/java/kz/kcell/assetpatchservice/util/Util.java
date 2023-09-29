package kz.kcell.assetpatchservice.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class Util {
    public static Map<Long, Map<String,String>> readCsvIntoMap(String file) {
        Map<Long, Map<String, String>> dataMap = new HashMap<>();

        try (
                InputStream inputStream = Util.class.getClassLoader().getResourceAsStream(file);
                BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))
        ) {
            String line;
            boolean isFirstLine = true;
            String[] headers = null;

            while ((line = br.readLine()) != null) {
                if (isFirstLine) {
                    headers = line.split(";");
                    isFirstLine = false;
                    continue;
                }

                String[] values = line.split(";");
                Long id = Long.parseLong(values[0]);

                Map<String, String> rowData = new HashMap<>();
                for (int i = 1; i < values.length; i++) {
                    rowData.put(headers[i], values[i]);
                }

                dataMap.put(id, rowData);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return dataMap;
    }
}
