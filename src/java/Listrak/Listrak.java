package Listrak;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import Utility.DBController;
import com.opencsv.CSVWriter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
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
import java.util.TimeZone;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author AN2
 */
public class Listrak extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    private File file;
    private FileWriter fw;
    private CSVWriter writer;

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        java.util.TimeZone.setDefault(TimeZone.getTimeZone("PST"));

        BufferedReader br = null;
        String onoff = "";
        ArrayList<String> time = new ArrayList<>();

        File file = new File("listrak_module_settings.txt");

        try {

            String sCurrentLine;
            if (!file.exists()) {
                file.createNewFile();
            }
            br = new BufferedReader(new FileReader("listrak_module_settings.txt"));
            System.out.println(file.getAbsolutePath());
            while ((sCurrentLine = br.readLine()) != null) {
                if (sCurrentLine.split("=")[0].equalsIgnoreCase("onoff")) {
                    onoff = sCurrentLine.split("=")[1];
                }
                if (sCurrentLine.split("=")[0].equalsIgnoreCase("time")) {
                    String sTimes[] = sCurrentLine.split("=")[1].split(";");
                    for (String t : sTimes) {
                        time.add(t);
                    }
                }
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        String settingsMessage = "";
        if (onoff.equalsIgnoreCase("checked")) {
            settingsMessage = "Auto update is <strong>ON</strong> @ ";
            Object scheduledTime[] = new String[time.size()];
            scheduledTime = time.toArray();
            SimpleDateFormat out2 = new SimpleDateFormat("hh:mm a");
            for (int i = 0; i < scheduledTime.length; i++) {
                if (scheduledTime[i] != null) {
                    Date dd = null;
                    try {
                        dd = out2.parse(scheduledTime[i].toString());
                    } catch (ParseException ex) {
                        ex.printStackTrace();
                    }
                    settingsMessage += out2.format(dd) + ", ";
                }
            }
            settingsMessage = settingsMessage.substring(0, settingsMessage.length() - 2) + ".";
        } else {
            settingsMessage = "Auto update is <strong>OFF</strong>.";
        }
        settingsMessage = "<small>(" + settingsMessage + ")</small>";
        request.setAttribute("settingsMessage", settingsMessage);
        
        String cookies = "";
        HashMap<String, String> cookieHM = new HashMap();
        Cookie cookie[] = request.getCookies();
        if (cookie != null) {
            for (Cookie c : cookie) {
                cookies += c.getName() + "=" + c.getValue() + "<br/>";
                cookieHM.put(c.getName(), c.getValue());
            }
        }
        request.setAttribute("cookies", cookies);

        String customersFile = cookieHM.get("customersFile");
        String ordersFile = cookieHM.get("ordersFile");
        String orderItemsFile = cookieHM.get("orderItemsFile");
        String startDate = cookieHM.get("startDate");
        String fileName = "";

        String action = request.getParameter("action");
        String reportName = request.getParameter("reportName");
        String url = "/listrak.jsp", message = "", downloadLink = "";

        SimpleDateFormat in = new SimpleDateFormat("MM/dd/yyyy");
        SimpleDateFormat out = new SimpleDateFormat("MM/dd/yyyy HH:mm");

        if (action == null) {
            action = "get report";
            request.setAttribute("action", action);
        }

        Cookie startDateCookie = null;
        if (startDate != null) {
            request.setAttribute("startDate", startDate);
        } else {
            startDateCookie = new Cookie("startDate", in.format(new GregorianCalendar().getTime()));
            response.addCookie(startDateCookie);
            request.setAttribute("startDate", startDateCookie.getValue());
            startDate = startDateCookie.getValue();
            cookieHM.put("startDate", startDateCookie.getValue());
        }

        if (action.equalsIgnoreCase("get report")) {
            //Check to see if date rand is with in limit
            startDate = request.getParameter("startDate");
            Date dateStartDate = null;
            GregorianCalendar cal = new GregorianCalendar();
            GregorianCalendar calInvalidDate = new GregorianCalendar();
            double daysRange = 0;
            if (startDate != null) {
                try {
                    dateStartDate = in.parse(startDate);
                    request.setAttribute("startDate", startDate);
                    response.addCookie(new Cookie("startDate", startDate));
                } catch (ParseException ex) {
                    ex.printStackTrace();
                    startDate = null;
                    calInvalidDate.set(Calendar.YEAR, -1000);
                    dateStartDate = new Date();
                    dateStartDate.setTime(calInvalidDate.getTimeInMillis());
                }
                daysRange = (double) (cal.getTime().getTime() - dateStartDate.getTime()) / (1000 * 60 * 60 * 24);
            }
            if (daysRange > 30) {
                dateStartDate = null;
                message = "<p><strong>Sorry, this app is restricted to pull data from the last 30 days only.</strong></p>";
                request.setAttribute("userForm", message);
            }

            //If date is good, get the report, save and print.
            if (dateStartDate != null) {
                response.setContentType("text/html;charset=UTF-8");
                String date = out.format(cal.getTime()).replace("/", "-").replace(":", " ");
                String print = "<br>";
                ResultSet resultSet = null;

                if (reportName.equalsIgnoreCase("customers")) {
                    fileName = "customers " + date + ".csv";
                    file = new File(getServletContext().getRealPath("/") + fileName);
                    fw = new FileWriter(file);
                    writer = new CSVWriter(fw, CSVWriter.DEFAULT_SEPARATOR, CSVWriter.DEFAULT_QUOTE_CHARACTER, CSVWriter.DEFAULT_LINE_END);

                    if (DBController.createConnection()) {
                        resultSet = DBController.getCustomers(in.format(dateStartDate));
                    } else {
                        request.setAttribute("userForm", "<br><br>From Servlet. - Connection Failed.");
                    }
                    response.addCookie(new Cookie("customersFile", fileName));
                    cookieHM.put("customersFile", fileName);
                } else if (reportName.equalsIgnoreCase("orders")) {
                    fileName = "orders " + date + ".csv";
                    file = new File(getServletContext().getRealPath("/") + fileName);
                    fw = new FileWriter(file);
                    writer = new CSVWriter(fw, CSVWriter.DEFAULT_SEPARATOR, CSVWriter.DEFAULT_QUOTE_CHARACTER, CSVWriter.DEFAULT_LINE_END);

                    if (DBController.createConnection()) {
                        resultSet = DBController.getOrders(in.format(dateStartDate));
                    } else {
                        request.setAttribute("userForm", "<br><br>From Servlet. - Connection Failed.");
                    }
                    response.addCookie(new Cookie("ordersFile", fileName));
                    cookieHM.put("ordersFile", fileName);
                } else if (reportName.equalsIgnoreCase("orderItems")) {
                    fileName = "orderItems " + date + ".csv";
                    file = new File(getServletContext().getRealPath("/") + fileName);
                    fw = new FileWriter(file);
                    writer = new CSVWriter(fw, CSVWriter.DEFAULT_SEPARATOR, CSVWriter.DEFAULT_QUOTE_CHARACTER, CSVWriter.DEFAULT_LINE_END);

                    if (DBController.createConnection()) {
                        resultSet = DBController.getOrderItems(in.format(dateStartDate));
                    } else {
                        request.setAttribute("userForm", "<br><br>From Servlet. - Connection Failed.");
                    }
                    response.addCookie(new Cookie("orderItemsFile", fileName));
                    cookieHM.put("orderItemsFile", fileName);
                }

                cookies = "";
                cookie = request.getCookies();
                if (cookie != null) {
                    for (Cookie c : cookie) {
                        cookies += c.getName() + "=" + c.getValue() + "<br/>";
                        cookieHM.put(c.getName(), c.getValue());
                    }
                }
                request.setAttribute("cookies", cookies);

                String[] dataCols;
                ResultSetMetaData resultSetMetaData;
                try {
                    resultSetMetaData = resultSet.getMetaData();
                    int colCount = resultSetMetaData.getColumnCount();
                    dataCols = new String[colCount];
                    print += "<tr style='font-weight: bold;'>";
                    for (int i = 1; i < colCount; i++) {
                        if (resultSetMetaData.getColumnName(i) != null) {
                            dataCols[i - 1] = resultSetMetaData.getColumnName(i);
                        } else {
                            dataCols[i - 1] = "";
                        }
                        print += "<td>" + dataCols[i - 1] + "</td>";
                    }
                    writer.writeNext(dataCols);
                    print += "</tr>";

                    while (resultSet.next()) {
                        dataCols = new String[colCount];
                        print += "<tr>";
                        for (int i = 1; i < colCount; i++) {
                            if (resultSet.getObject(i) != null) {
                                dataCols[i - 1] = resultSet.getObject(i).toString();
                            } else {
                                dataCols[i - 1] = "";
                            }
                            print += "<td>" + dataCols[i - 1] + "</td>";
                        }
                        
                        print += "</tr>";
                        writer.writeNext(dataCols);
                    }
                    writer.close();
                    fw.close();

                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
                request.setAttribute("userForm", print);
            } else {
                //invalid report date range
            }
        }

        cleanOldFiles();
        customersFile = cookieHM.get("customersFile");
        if (customersFile != null) {
            Date date = null;
            Path path = Paths.get(getServletContext().getRealPath("/") + "\\" + customersFile);
            BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);
            in = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                date = in.parse(attr.creationTime().toString().replace("T", " ").replace("Z", " "));
                GregorianCalendar gDate = new GregorianCalendar();
                gDate.setTime(date);
                gDate.add(Calendar.HOUR_OF_DAY, -7);
                date = gDate.getTime();
            } catch (ParseException ex) {
                ex.printStackTrace();
            }
            downloadLink = "<br/><a href=\"./" + customersFile + "\">[Downdload customers file]</a> Created on: " + out.format(date);
        }
        ordersFile = cookieHM.get("ordersFile");
        if (ordersFile != null) {
            Date date = null;
            Path path = Paths.get(getServletContext().getRealPath("/") + "\\" + ordersFile);
            BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);
            in = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                date = in.parse(attr.creationTime().toString().replace("T", " ").replace("Z", " "));
                GregorianCalendar gDate = new GregorianCalendar();
                gDate.setTime(date);
                gDate.add(Calendar.HOUR_OF_DAY, -7);
                date = gDate.getTime();
            } catch (ParseException ex) {
                ex.printStackTrace();
            }
            downloadLink += "<br/><a href=\"./" + ordersFile + "\">[Downdload orders file]</a> Created on: " + out.format(date);
        }
        orderItemsFile = cookieHM.get("orderItemsFile");
        if (orderItemsFile != null) {
            Date date = null;
            Path path = Paths.get(getServletContext().getRealPath("/") + "\\" + orderItemsFile);
            BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);
            in = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                date = in.parse(attr.creationTime().toString().replace("T", " ").replace("Z", " "));
                GregorianCalendar gDate = new GregorianCalendar();
                gDate.setTime(date);
                gDate.add(Calendar.HOUR_OF_DAY, -7);
                date = gDate.getTime();
            } catch (ParseException ex) {
                ex.printStackTrace();
            }
            downloadLink += "<br/><a href=\"./" + orderItemsFile + "\">[Downdload orderItems file]</a> Created on: " + out.format(date);
        }
        request.setAttribute("downloadLink", downloadLink);

        getServletContext()
                .getRequestDispatcher(url)
                .forward(request, response);

    }

    private void cleanOldFiles() {
        SimpleDateFormat in;
        File folder = new File(getServletContext().getRealPath("/"));
        File[] listOfFiles = folder.listFiles();
        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                String aFile = listOfFiles[i].getName();
                int gotADot = aFile.lastIndexOf(".");
                String ext;
                if (gotADot > 0) {
                    ext = aFile.substring(gotADot);
                    if (ext.equalsIgnoreCase(".csv")) {
                        Date date = null;
                        Path path = Paths.get(listOfFiles[i].getAbsolutePath());
                        BasicFileAttributes attr = null;
                        try {
                            attr = Files.readAttributes(path, BasicFileAttributes.class);
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                        in = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        try {
                            date = in.parse(attr.creationTime().toString().replace("T", " ").replace("Z", " "));
                            GregorianCalendar gDate = new GregorianCalendar();
                            gDate.setTime(date);
                            gDate.add(Calendar.HOUR_OF_DAY, -7);
                            date = gDate.getTime();
                        } catch (ParseException ex) {
                            ex.printStackTrace();
                        }
                        double hour = (new GregorianCalendar().getTimeInMillis() - date.getTime()) / (1000 * 60 * 60);
                        //System.out.println("File " + listOfFiles[i].getName() + " created: " + in.format(date));
                        //System.out.println("Now: " + in.format(new GregorianCalendar().getTime()) + " > " + ext + ">>" + hour);
                        if (hour >= 1) {
                            listOfFiles[i].delete();
                        }
                    }
                } else if (listOfFiles[i].isDirectory()) {
                    //System.out.println("Directory " + listOfFiles[i].getName());
                }
            }
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
    /*
     String key = "Key12345AKC12345"; // 128 bit key

     private String encrypt(String text) {
     Cipher cipher;
     Key aesKey = new SecretKeySpec(key.getBytes(), "AES");
     byte[] encrypted = null;
     try {
     // Create key and cipher
     cipher = Cipher.getInstance("AES");
     // encrypt the text
     cipher.init(Cipher.ENCRYPT_MODE, aesKey);
     encrypted = cipher.doFinal(text.getBytes());
     } catch (Exception e) {
     e.printStackTrace();
     }
     String returnThis = null;
     try {
     returnThis = new String(encrypted, "ISO-8859-1");
     } catch (UnsupportedEncodingException ex) {
     ex.printStackTrace();
     }
     return returnThis;
     }

     private String decrypt(String encryptedString) {
     Key aesKey = new SecretKeySpec(key.getBytes(), "AES");
     String decrypted = null;
     byte[] encrypted = null;
     try {
     encrypted = encryptedString.getBytes("ISO-8859-1");
     } catch (UnsupportedEncodingException ex) {
     ex.printStackTrace();
     }

     try {
     // decrypt the text
     Cipher cipher = Cipher.getInstance("AES");
     cipher.init(Cipher.DECRYPT_MODE, aesKey);
     decrypted = new String(cipher.doFinal(encrypted));
     System.err.println(decrypted);
     } catch (Exception e) {
     e.printStackTrace();
     }
     return decrypted;
     }
     */
}
