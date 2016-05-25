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
import Utility.MyFTPClient;
import Utility.TextFileReadWrite;
import com.opencsv.CSVWriter;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class ListrakQuartzJob implements Job {

    private File file;
    private FileWriter fw;
    private CSVWriter writer;
    BufferedWriter bw;
    SimpleDateFormat in = new SimpleDateFormat("MM/dd/yyyy");
    SimpleDateFormat out1 = new SimpleDateFormat("MM/dd/yyyy HH:mm");
    HashMap<String, String> settingHM = new HashMap<>();

    private void writeUpdateTimestamp() {
        try {
            File f1 = new File("listrak_module_settings.txt");
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
        System.out.println(":::>>>Listrak Job Time Check @ " + out.format(new Date()));

        BufferedReader br = null;
        String onoff = "", emails = "", lastUpdate = null;
        ArrayList<String> time = new ArrayList<>();
        File settingsFile = new File("listrak_module_settings.txt");
        String emailArr[];
        String[] settings = TextFileReadWrite.readFile(settingsFile);
        for (String e : settings) {
            settingHM.put(e.split("=")[0], e.split("=")[1]);
            if (e.split("=")[0].equalsIgnoreCase("onoff")) {
                onoff = e.split("=")[1];
            }
            if (e.split("=")[0].equalsIgnoreCase("time")) {
                String sTimes[] = e.split("=")[1].split(";");
                time.addAll(Arrays.asList(sTimes));
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
        emailArr = emails.replace(";;", ";").split(";");

        if (onoff.equalsIgnoreCase("checked")) {
            for (String e : time) {
                Date dScheduled, dNow;
                try {
                    dScheduled = out.parse(e);
                    dNow = out.parse(out.format(new Date()));
                    if (dScheduled.getTime() == dNow.getTime()) {
                        System.out.println();
                        //System.out.println("--------Time matches......do stuffs......");
                        System.out.println("Time matches | Scheduled: " + out.format(dScheduled) + " Now: " + out.format(dNow));
                        //System.out.println();
                        try {
                            GregorianCalendar yesterday = new GregorianCalendar();
                            yesterday.set(Calendar.DAY_OF_YEAR, -1);                            
                            Date fromDate;
                            try {
                                if (settingHM.get("lastUpdate") == null) {
                                    fromDate = yesterday.getTime();
                                } else {
                                    fromDate = out1.parse(settingHM.get("lastUpdate"));
                                }
                            } catch (ParseException ex) {
                                fromDate = yesterday.getTime();
                                System.out.println(ex.getMessage());
                            }
                            
                            System.out.println("fromDate: " + out1.format(fromDate));
                            
                            ResultSet cResultSet, oResultSet, oiResultSet;
                            File cFile = new File("customers.txt");
                            File oFile = new File("orders.txt");
                            File oiFile = new File("orderitems.txt");
                            if (DBController.createConnection()) {
                                cResultSet = DBController.getCustomers(out1.format(fromDate));
                                oResultSet = DBController.getOrders(out1.format(fromDate));
                                oiResultSet = DBController.getOrderItems(out1.format(fromDate));
                                MyFTPClient ftp = new MyFTPClient();
                                if (TextFileReadWrite.writeFile(cResultSet, cFile)
                                    && TextFileReadWrite.writeFile(oResultSet, oFile)
                                    && TextFileReadWrite.writeFile(oiResultSet, oiFile)
                                    && ftp.sendFile(cFile)
                                    && ftp.sendFile(oFile)
                                    && ftp.sendFile(oiFile)) {//Everthing OK. All files updated w/o error

                                    if (emails != null && emails.length() > 0) {
                                        for (String email : emailArr) {
                                            try {
                                                InternetAddress.parse(email);
                                                if (email.indexOf("@") > 0 && email.indexOf(".") > 0) {
                                                    GregorianCalendar cal = new GregorianCalendar();
                                                    Emailer.Send(AKC_Creds.ANH_EMAIL, AKC_Creds.ANH_EMAIL_PWD, email, "Auto notification from application server",
                                                                    "Scheduled Listrak FTP update executed.\n" + 
                                                                    "Uploaded files:\n" + 
                                                                            "\t" + cFile.getName() + "\n" + 
                                                                            "\t" + oFile.getName() + "\n" + 
                                                                            "\t" + oiFile.getName() + "\n" +
                                                                    "Data date range: " + settingHM.get("lastUpdate") + " to " + out1.format(cal.getTime())
                                                    );
                                                }
                                            } catch (MessagingException ex) {
                                                System.out.println(ex.getMessage());
                                            }
                                        }
                                    }
                                    writeUpdateTimestamp();
                                }
                            }
                        } catch (Exception ex) {
                            System.out.println(ex.getMessage());
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
