package org.seo.redirects;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

public class FileUtils {

    private static final String uriHeader = "INSERT_UPDATE SolrURIRedirect;url[unique=true];&redirectRefID\n";
    private static final String fillers = "\n\n\n\n\n\n";
    private static final String redHeader = "INSERT_UPDATE SolrFacetSearchKeywordRedirect;facetSearchConfig(name)[unique=true,default=$facetSearchConfigName];language(isocode)[unique=true,default=$lang];keyword[unique=true];matchType(code)[unique=true];redirect(&redirectRefID);ignoreCase[default=true];searchKeywordRedirect[default=true]\n";

    public static Map<String, String> readExcelAndReturnKeyValue(XSSFWorkbook workbook) {
        HashMap<String, String> rawMap = new HashMap<String, String>();
        Sheet sheet = workbook.getSheetAt(0);
        for (Row row : sheet) {
            String url = row.getCell(0).getStringCellValue();
            String searchTerms = row.getCell(1).getStringCellValue();
            rawMap.put(searchTerms, url);
        }
        return rawMap;
    }

    public static Map<String, String> reduceMap(Map<String, String> rawMap) {
        HashMap<String, String> reducedMap = new HashMap<String, String>();
        for (Entry entry : rawMap.entrySet()) {
            String value = entry.getValue().toString().trim().substring(entry.getValue().toString().trim().indexOf(".com/") + 4);
            String possibleKeys = entry.getKey().toString().trim();
            if (possibleKeys.indexOf(",") > 0) {
                for (String key : possibleKeys.split(",")) {
                    reducedMap.put(key.trim(), value);
                }
            } else {
                reducedMap.put(possibleKeys, value);
            }
        }
        return reducedMap;
    }

    public static Map<String, String> sortedMap(Map<String, String> unsortedMap) {
        List<Entry<String, String>> list = new LinkedList<Entry<String, String>>(unsortedMap.entrySet());
        Collections.sort(list, new Comparator<Entry<String, String>>() {
            public int compare(Entry<String, String> o1, Entry<String, String> o2) {
                return o1.getValue().compareTo(o2.getValue());
            }
        });
        Map<String, String> sortedMap = new HashMap<String, String>();

        for (Entry<String, String> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap;
    }

    public static void initializeImpex(Properties properties) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(properties.getProperty("output")));
        writer.write(properties.getProperty("impexBoiler"));
        writer.close();
    }

    public static Map<String, String> writeUriLines(Map<String, String> reducedMap, Properties properties) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(properties.getProperty("output"), true));
        writer.append(uriHeader);
        StringBuffer stringBuffer = new StringBuffer();
        Map<String, String> redMap = new HashMap<String, String>();
        Set<String> uniqueVals = new HashSet<String>();
        for (Entry entry : reducedMap.entrySet()) {
            //Multiple keys can point to same value, we do not want multiple lines of impex for the same value of URI
            if (uniqueVals.contains(entry.getValue())) {
                continue;
            } else {
                redMap.put(entry.getValue().toString(), entry.getKey().toString().toLowerCase().replaceAll(" ", "_"));
                uniqueVals.add(entry.getValue().toString());
                stringBuffer.append(";" + entry.getValue() + ";$contentCatalogName-redirectRefID-" + entry.getKey().toString().toLowerCase().replaceAll(" ", "_") + "\n");
            }
        }
        writer.append(stringBuffer);
        writer.close();
        return redMap;
    }

    public static void writeBuffer(Properties properties) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(properties.getProperty("output"), true));
        writer.append(fillers);
        writer.close();
    }

    public static void writeRedirectLines(Map<String, String> reducedMap, Map<String, String> helperMap, Properties properties) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(properties.getProperty("output"), true));
        writer.append(redHeader);
        StringBuffer stringBuffer = new StringBuffer();
        for (Entry entry : reducedMap.entrySet()) {
            stringBuffer.append(";;;\"" + entry.getKey() + "\";EXACT;$contentCatalogName-redirectRefID-" + helperMap.get(entry.getValue()) + "\n");
        }
        writer.append(stringBuffer);
        writer.close();
    }


}
