/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package AMZ;

import Utility.DBController;
import Utility.TextFileReadWrite;
import java.io.File;
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
public class AMZFeedCreator {

    private HashMap<String, String> settingsHM;
    private final SimpleDateFormat out = new SimpleDateFormat("h:mm a");
    private final SimpleDateFormat in = new SimpleDateFormat("MM/dd/yyyy h:mm:ss a");
    private String action = null;
    private String onoff;
    private String[] scheduledTime;
    private String emails = null;
    private String[] times = null;
    private final File file = new File("amazon_module_settings.txt");
    private String timeDisplay;
    private String lastUpdate;

    AMZFeedCreator() {
        //Constructor
    }

    public boolean CreateShipmentTrackingFeed(String fromDate) {
        boolean complete = false;
        readSettings();
        if (DBController.createConnection()) {
            try {
                ResultSet resultSet = null;
                resultSet = DBController.getShippedAmzOrders(lastUpdate);
                String[] dataCols;
                ResultSetMetaData resultSetMetaData;
                resultSetMetaData = resultSet.getMetaData();
                int colCount = resultSetMetaData.getColumnCount();
                dataCols = new String[colCount];
                
                //need column names?
                for (int i = 1; i <= colCount; i++) {
                    if (resultSetMetaData.getColumnName(i) != null) {
                        dataCols[i - 1] = resultSetMetaData.getColumnName(i);
                    } else {
                        dataCols[i - 1] = "";
                    }
                }
                
                while (resultSet.next()) {
                    dataCols = new String[colCount];
                    for (int i = 1; i <= colCount; i++) {
                        if (resultSet.getObject(i) != null) {
                            dataCols[i - 1] = resultSet.getObject(i).toString();
                        } else {
                            dataCols[i - 1] = "";
                        }
                    }
                    String shipped = dataCols[1].equalsIgnoreCase("T") ? "YES" : "YES";
                    String shipper = "";
                    if (dataCols[2].length() > 7) {
                        shipper = dataCols[2].substring(0, 2).equalsIgnoreCase("94") ? "Usps" : "Ups";
                    } else {
                        shipper = "Not Available";
                    }
                    String tracking = dataCols[2].length() == 0 ? "Not Available" : dataCols[2];
                }
                

                
            } catch (SQLException ex) {
                System.out.println(ex.getMessage());
            }
        } else {
            System.out.println("<<<Database connection error>>>");
        }

        return complete;
    }

    private void readSettings() {
        String[] readLines = TextFileReadWrite.readFile(file);
        settingsHM = new HashMap<>();
        for (String e : readLines) {
            if (e.indexOf("=") > 0) {
                settingsHM.put(e.split("=")[0], e.split("=")[1]);
            }
        }
        scheduledTime = new String[24];
        ArrayList<String> time = new ArrayList<>(Arrays.asList(settingsHM.get("time").split(";")));
        timeDisplay = "Scheduled updates: ";
        for (String time1 : time) {
            Date d;
            try {
                d = out.parse(time1);
                scheduledTime[d.getHours()] = "selected";
                timeDisplay += out.format(d) + ", ";

            } catch (ParseException ex) {
                System.out.println(ex.getMessage());
            }
        }
        emails = settingsHM.get("emails").replace(";", "\r\n").trim();
        settingsHM.put("emails", emails);
        lastUpdate = settingsHM.get("lastUpdate");
        timeDisplay = timeDisplay.substring(0, timeDisplay.length() - 2) + ".<br>{Last update on: " + lastUpdate + "}";
    }
}
