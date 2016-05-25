/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Utility;

import com.opencsv.CSVWriter;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

/**
 *
 * @author AN2
 */
public class TextFileReadWrite {
    public static final SimpleDateFormat out = new SimpleDateFormat("h:mm a");
    public static final SimpleDateFormat in = new SimpleDateFormat("MM/dd/yyyy h:mm:ss a");
    
    public static HashMap<String, String> readSettings(File file) {
        String[] readLines = TextFileReadWrite.readFile(file);
        HashMap<String, String> settingsHM = new HashMap<>();
        for (String e : readLines) {
            if (e.indexOf("=") > 0) {
                settingsHM.put(e.split("=")[0], e.split("=")[1]);
            }
        }
        String[] scheduledTimeTemp = new String[24];
        String settingsDisplay = "Scheduled updates: ", emails, autoUpdate, selectedTime24hr = "";
        ArrayList<String> time = new ArrayList<>(Arrays.asList(settingsHM.get("time").split(";")));
        for (String time1 : time) {
            Date d;
            try {
                d = out.parse(time1);
                scheduledTimeTemp[d.getHours()] = "selected";
                settingsDisplay += out.format(d) + ", ";

            } catch (ParseException ex) {
                System.out.println(ex.getMessage());
            }
        }
        for(String e : scheduledTimeTemp){
            selectedTime24hr += e + ";";
        }
        selectedTime24hr = selectedTime24hr.replace("null", "");
        settingsHM.put("selectedTime24hr", selectedTime24hr);
        emails = settingsHM.get("emails").replace(";", "\r\n").trim();
        settingsHM.put("emails", emails);
        autoUpdate = settingsHM.get("onoff").equalsIgnoreCase("checked")? "ON" : "OFF";
        settingsDisplay = settingsDisplay.substring(0, settingsDisplay.length() - 2) + ".<br>{Last update on: " + settingsHM.get("lastUpdate") + "} - Auto update is currently switched <strong>" + autoUpdate + "</strong>";
        settingsHM.put("settingsDisplay", settingsDisplay);

        return settingsHM;
    }

    public static String readJSONFile(File file) {
        BufferedReader br = null;
        String sCurrentLine = "", allLines = "";
        try {
            if (!file.exists()) {
                file.createNewFile();
                return "";
            }
            br = new BufferedReader(new FileReader(file));
            while ((sCurrentLine = br.readLine()) != null) {
                allLines += sCurrentLine;
            }
            br.close();
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException ex) {
                System.out.println(ex.getMessage());
            }
        }
        
        return allLines;
    }
    
    public static String[] readFile(File file) {
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException ex) {
                System.out.println(ex.getMessage());
            }
        }
        String[] returnThis;
        BufferedReader br = null;
        String sCurrentLine = "", allLines = "";
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            br = new BufferedReader(new FileReader(file));
            while ((sCurrentLine = br.readLine()) != null) {
                allLines += sCurrentLine + "~;";
            }
            br.close();
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException ex) {
                System.out.println(ex.getMessage());
            }
        }
        returnThis = allLines.split("~;");
        return returnThis;
    }

    public static boolean writeFile(ResultSet resultSet, File file) {
        boolean errorFree = true;
        try {
            FileWriter fw = new FileWriter(file);
            CSVWriter writer = new CSVWriter(fw, '\t', CSVWriter.DEFAULT_QUOTE_CHARACTER, CSVWriter.DEFAULT_LINE_END);
            String[] dataCols;
            ResultSetMetaData resultSetMetaData;
            resultSetMetaData = resultSet.getMetaData();
            int colCount = resultSetMetaData.getColumnCount();
            dataCols = new String[colCount];
            for (int i = 1; i < colCount; i++) {
                if (resultSetMetaData.getColumnName(i) != null) {
                    dataCols[i - 1] = resultSetMetaData.getColumnName(i);
                } else {
                    dataCols[i - 1] = "";
                }
            }
            writer.writeNext(dataCols);
            while (resultSet.next()) {
                dataCols = new String[colCount];
                for (int i = 1; i < colCount; i++) {
                    if (resultSet.getObject(i) != null) {
                        dataCols[i - 1] = resultSet.getObject(i).toString();
                    } else {
                        dataCols[i - 1] = "";
                    }
                }

                writer.writeNext(dataCols);
            }
            writer.close();
            fw.close();

        } catch (IOException ex) {
            System.err.println(ex.getMessage());
            errorFree = false;
        } catch (SQLException ex) {
            System.err.println(ex.getMessage());
            errorFree = false;
        }
        return errorFree;
    }

    public static boolean writeFile(String content, File file) {
        boolean completed = false;
        try {
            // if file doesnt exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }

            FileWriter fw = new FileWriter(file);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(content);
            bw.close();
            completed = true;

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return completed;
    }
}
