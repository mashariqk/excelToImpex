package org.seo.redirects;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class App 
{
    public static void main( String[] args ) throws IOException, InvalidFormatException {
        Properties properties = new Properties();
        properties.load(new FileInputStream("core.properties"));
        Map<String,String> rawMap = FileUtils.readExcelAndReturnKeyValue(new XSSFWorkbook(new FileInputStream(new File(properties.getProperty("data")))));
        Map<String,String> reducedMap = FileUtils.reduceMap(rawMap);
        FileUtils.initializeImpex(properties);
        FileUtils.writeUriLines(reducedMap,properties);
        FileUtils.writeBuffer(properties);
        FileUtils.writeRedirectLines(reducedMap,properties);
    }
}
