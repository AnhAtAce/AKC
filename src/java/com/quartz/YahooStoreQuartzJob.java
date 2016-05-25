/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author AN2
 */
package com.quartz;

import AMZ.AKC_Creds;
import Utility.Emailer;
import Utility.DBController;
import Utility.TextFileReadWrite;
import Yahoo.Order.Update.YahooOrderUpdate;
import com.opencsv.CSVWriter;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class YahooStoreQuartzJob implements Job {

    private File file;
    private FileWriter fw;
    private CSVWriter writer;
    BufferedWriter bw;
    SimpleDateFormat in = new SimpleDateFormat("MM/dd/yyyy");
    SimpleDateFormat out1 = new SimpleDateFormat("MM/dd/yyyy HH:mm");
    HashMap<String, String> settingHM = new HashMap<>();

    private void writeUpdateTimestamp() {
        try {
            File f1 = new File("yahooOrderUpdate_module_settings.txt");
            fw = new FileWriter(f1);
            bw = new BufferedWriter(fw);
            settingHM.put("lastUpdate", out1.format(new Date()));
            for (String k : settingHM.keySet()) {
                bw.write(k + "=" + settingHM.get(k) + "\r\n");
            }
            bw.close();
            fw.close();
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }

    public void execute(JobExecutionContext context)
            throws JobExecutionException {
        SimpleDateFormat out = new SimpleDateFormat("hh:mm a");
        System.out.println(":::>>>Yahoo Store Job Time Check @ " + out.format(new Date()));

        BufferedReader br = null;
        String onoff = "", emails = "", lastUpdate = null;
        ArrayList<String> time = new ArrayList<>();
        File settingsFile = new File("yahooOrderUpdate_module_settings.txt");
        String emailArr[];
        String[] settings = TextFileReadWrite.readFile(settingsFile);
        for (String e : settings) {
            settingHM.put(e.split("=")[0], e.split("=")[1]);
            if (e.split("=")[0].equalsIgnoreCase("onoff")) {
                onoff = e.split("=")[1];
            }
            if (e.split("=")[0].equalsIgnoreCase("time")) {
                String sTimes[] = e.split("=")[1].split(";");
                for (String t : sTimes) {
                    time.add(t);
                }
            }
            if (e.split("=")[0].equalsIgnoreCase("emails")) {
                emails = e.split("=")[1];
            }
            if (e.split("=")[0].equalsIgnoreCase("lastUpdate")) {
                lastUpdate = e.split("=")[1];
            }
        }
        if (emails == null) {
            emails = ";";
        }
        Date lastDate;
        try {
            lastDate = out1.parse(lastUpdate);
        } catch (ParseException ex) {
            GregorianCalendar now = new  GregorianCalendar();
            now.set(Calendar.HOUR, -25);
            lastDate = now.getTime();
        }
        emailArr = emails.replace(";;", ";").split(";");
        if (onoff.equalsIgnoreCase("checked")) {
            for (String e : time) {
                Date dScheduled, dNow;
                try {
                    dScheduled = out.parse(e);
                    dNow = out.parse(out.format(new Date()));
                    if (dScheduled.getTime() == dNow.getTime()) {
                        System.out.println("Yahoo Store Task Scheduled: " + out.format(dScheduled) + " Now: " + out.format(dNow));
                        if (DBController.createConnection()) {
                            try {
                                
                                GregorianCalendar now = new  GregorianCalendar();
                                Date newDate = new Date();
                                now.setTime(newDate);
                                now.set(Calendar.HOUR, -25);           
                                System.out.println("#################### Send invoice created after: " + in.format(now.getTime()));
                                //^^ quick fix: get invoices that was created within the las 25 hours intead of from the last update
                                
                                
                                ResultSet resultSet;
                                resultSet = DBController.getShippedYOrders(in.format(now.getTime()));
                                String[] dataCols;
                                ResultSetMetaData resultSetMetaData;
                                resultSetMetaData = resultSet.getMetaData();
                                int colCount = resultSetMetaData.getColumnCount();
                                dataCols = new String[colCount];
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
                                    String shipper;
                                    if (dataCols[2].length() > 7) {
                                        shipper = dataCols[2].substring(0, 2).equalsIgnoreCase("94") ? "Usps" : "Ups";
                                    }else {
                                        shipper = "Not Available";
                                    }
                                    String tracking = dataCols[2].length()==0 ? "Not Available" : dataCols[2];
                                    if (dataCols[2].length() > 7) {
                                        StringBuilder sb = new StringBuilder();
                                        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
                                        sb.append("\r\n");
                                        sb.append("<!DOCTYPE TrackingUpdate SYSTEM \"http://store.yahoo.com/lib/vw/TrackingUpdate.dtd\">");
                                        sb.append("\r\n");
                                        sb.append("<TrackingUpdate password=\""+AKC_Creds.YAHOO_ORDER_EMAIL_UPDATE_PWD+"\">");
                                        sb.append("\r\n");                                        
                                        sb.append("\t");
                                        sb.append("<YahooOrder id=\"acekaraoke-").append(dataCols[0]).append("\" shipped=\"").append(shipped).append("\" trackingNumber=\"").append(tracking).append("\" shipper=\"").append(shipper).append("\" />");
                                        sb.append("\r\n");
                                        sb.append("</TrackingUpdate>");

                                        System.out.println("&&&&&EMAILS&&&&&");
                                        System.out.println(sb.toString());

                                        
                                        try {
                                            Emailer.Send(AKC_Creds.ANH_EMAIL, AKC_Creds.ANH_EMAIL_PWD, "tracking-update@store.yahoo.com", "Order Traking Update",
                                                    sb.toString()
                                            );
                                        } catch (MessagingException ex) {
                                            Logger.getLogger(YahooOrderUpdate.class.getName()).log(Level.SEVERE, null, ex);
                                        }
                                        
                                        
                                        try {
                                            Thread.sleep(3000);
                                        } catch (InterruptedException ex) {
                                            Logger.getLogger(YahooStoreQuartzJob.class.getName()).log(Level.SEVERE, null, ex);
                                        }

                                    }
                                }
                                System.out.println("===[Data send to Yahoo]===");
                                try {
                                    if (emails != null && emails.length() > 0) {
                                        for (String email : emailArr) {
                                                InternetAddress.parse(email);
                                                if (email.indexOf("@") > 0 && email.indexOf(".") > 0) {
                                                    GregorianCalendar cal = new GregorianCalendar();
                                                    Emailer.Send(AKC_Creds.ANH_EMAIL, AKC_Creds.ANH_EMAIL_PWD, email, "Auto notification from Yahoo order update application server",
                                                            "Scheduled Yahoo orders update executed.\n"
                                                            + "Order data date range: " + settingHM.get("lastUpdate") + " to " + out1.format(cal.getTime())
                                                    );
                                                }
                                        }
                                    }
                                    System.out.println("===[email notifications send]===");
                                    writeUpdateTimestamp();
                                } catch (MessagingException ex) {
                                    Logger.getLogger(YahooOrderUpdate.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            } catch (SQLException ex) {
                                System.out.println(ex.getMessage());
                            }
                        } else {
                            System.out.println("<<<Database connection error>>>");
                        }
                    } else {
                        System.out.println("Time not match | Scheduled: " + out.format(dScheduled) + " Now: " + out.format(dNow));
                    }
                } catch (ParseException ex) {
                    System.out.println(ex.getMessage());
                }
            }
        }
    }
}
