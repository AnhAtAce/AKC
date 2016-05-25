/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package AMZ;

import Utility.Emailer;
import Utility.TextFileReadWrite;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author AN2
 */
public class AmazonModuleSettings extends HttpServlet {

    private HashMap<String, String> settingsHM;
    private final SimpleDateFormat out = new SimpleDateFormat("h:mm a");
    private final SimpleDateFormat in = new SimpleDateFormat("MM/dd/yyyy h:mm:ss a");
    private String action = null;
    private String onoff;
    private String[] selectedTime24hr;
    private String emails = null;
    private String[] times = null;
    private final File file = new File("amazon_module_settings.txt");
    private String settingsDisplay;

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    private void readSettings() {
        settingsHM = TextFileReadWrite.readSettings(file);
        selectedTime24hr = settingsHM.get("selectedTime24hr").split(";");
        emails = settingsHM.get("emails").replace("\r\n", ";");
        settingsDisplay = settingsHM.get("settingsDisplay");
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");

        readSettings();
        
        request.setAttribute("settingsDisplay", settingsDisplay);
        //load settings to page
        for (String e : settingsHM.keySet()) {
            if (e.equalsIgnoreCase("time")) {
                request.setAttribute("scheduledTime", selectedTime24hr);
            } else {
                request.setAttribute(e, settingsHM.get(e));
            }
        }

        action = request.getParameter("action");
        onoff = request.getParameter("onoff") == null ? "unchecked" : "checked";
        if (action != null && action.equalsIgnoreCase("save")) {
            try {
                // if file doesnt exists, then create it
                if (!file.exists()) {
                    file.createNewFile();
                }

                if (!settingsHM.get("onoff").equalsIgnoreCase(onoff)) {
                    if (emails != null && emails.length() > 0) {
                        for (String email : emails.split(";")) {
                            try {
                                InternetAddress.parse(email);
                                if (email.indexOf("@") > 0 && email.indexOf(".") > 0) {
                                    GregorianCalendar cal = new GregorianCalendar();
                                    String stat = onoff.equalsIgnoreCase("checked") ? "ON" : "OFF";
                                    Emailer.Send(AKC_Creds.ANH_EMAIL, AKC_Creds.ANH_EMAIL_PWD, email, "Auto notification from application server", "Amazon inventory auto update switched " + stat + " on " + in.format(cal.getTime()));
                                }
                            } catch (MessagingException ex) {
                                System.err.println(ex.getMessage());
                            }
                        }
                    }
                }
                settingsHM.put("onoff", onoff);

                String timeToHM = "";
                times = request.getParameterValues("time");
                for (String e : times) {
                    System.out.println(">:" + e + ":<");
                    timeToHM += e + ";";
                }
                
                //for testing purposes, will launch task the next minute
                
                GregorianCalendar launchTime = new GregorianCalendar();
                launchTime.add(Calendar.MINUTE, 1);
                timeToHM += out.format(launchTime.getTime())+";";
                
                
                settingsHM.put("time", timeToHM);
                emails = request.getParameter("emails");
                if (emails != null) {
                    emails = emails.replace(AKC_Creds.ANH_EMAIL, "");
                    emails = emails.replace("\r\n", ";") + ";"+AKC_Creds.ANH_EMAIL+";\r\n";
                    emails = emails.replace(";;", ";");
                    settingsHM.put("emails", emails.trim());
                }

                String fromHM = "";
                for (String k : settingsHM.keySet()) {
                    fromHM += k + "=" + settingsHM.get(k) + "\r\n";
                }

                if (TextFileReadWrite.writeFile(fromHM, file)) {
                    System.out.println("Saved settings: \n" + fromHM);
                } else {
                    System.out.println("Saving settings failed");
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            response.sendRedirect(getServletContext().getContextPath() + "/AmazonModuleSettings");
        } else {
            getServletContext()
                    .getRequestDispatcher("/amazon_module_settings.jsp")
                    .forward(request, response);
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

}
