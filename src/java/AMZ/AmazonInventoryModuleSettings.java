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
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author AN2
 */
public class AmazonInventoryModuleSettings extends HttpServlet {

    private final SimpleDateFormat out = new SimpleDateFormat("h:mm a");
    private final SimpleDateFormat in = new SimpleDateFormat("MM/dd/yyyy h:mm:ss a");
    File settingsFile;
    JSONObject settings, hourUI;
    String onoff, emails;

    void readSettings() {
        settingsFile = new File("AMZ_Inventory_Module_Settings.json");
        if (!settingsFile.exists()) {
            settings = new JSONObject();
            settings.put("onoff", "unchecked");
        } else {
            String jsonText = TextFileReadWrite.readJSONFile(settingsFile).trim();
            System.err.println(jsonText);
            settings = new JSONObject(jsonText);
        }

        hourUI = new JSONObject();
        hourUI.put("12:00 AM", "0");
        hourUI.put("1:00 AM", "1");
        hourUI.put("2:00 AM", "2");
        hourUI.put("3:00 AM", "3");
        hourUI.put("4:00 AM", "4");
        hourUI.put("5:00 AM", "5");
        hourUI.put("6:00 AM", "6");
        hourUI.put("7:00 AM", "7");
        hourUI.put("8:00 AM", "8");
        hourUI.put("9:00 AM", "9");
        hourUI.put("10:00 AM", "10");
        hourUI.put("11:00 AM", "11");
        hourUI.put("12:00 PM", "12");
        hourUI.put("1:00 PM", "13");
        hourUI.put("2:00 PM", "14");
        hourUI.put("3:00 PM", "15");
        hourUI.put("4:00 PM", "16");
        hourUI.put("5:00 PM", "17");
        hourUI.put("6:00 PM", "18");
        hourUI.put("7:00 PM", "19");
        hourUI.put("8:00 PM", "20");
        hourUI.put("9:00 PM", "21");
        hourUI.put("10:00 PM", "22");
        hourUI.put("11:00 PM", "23");

    }

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        readSettings();
      
        //load settings to page
        Object[] keys = settings.keySet().toArray();
        System.err.println("*** " + keys.length + " setting sparameters");
        for (Object key : keys) {
            if (key.equals("time")) {
                String[] timeUI = new String[24];
                JSONArray time = settings.getJSONArray("time");
                for(int k=0; k<time.length(); k++)
                {
                    if(hourUI.has(time.get(k).toString()))
                    timeUI[hourUI.getInt(time.get(k).toString())] = "selected";
                }
                request.setAttribute("scheduledTime", timeUI);
            } else {
                request.setAttribute(key.toString(), settings.get(key.toString()));
            }
        }
        request.setAttribute("settingsDisplay", "<pre>"+settings.toString(3)+"</pre>");
        
        String action = request.getParameter("action");

        if (action != null && action.equalsIgnoreCase("save")) {
            onoff = request.getParameter("onoff") == null ? "unchecked" : "checked";

            emails = request.getParameter("emails");
            settings.put("nonInventoriedSKUs", request.getParameter("nonInventoriedSKUs"));
            settings.put("emails", (emails.replace(AKC_Creds.ANH_EMAIL, "") + "\r\n"+AKC_Creds.ANH_EMAIL).replace("\r\n\r\n", "\r\n").trim());
            String[] times = request.getParameterValues("time");
            
                //for testing purposes, will launch task the next minute
                GregorianCalendar launchTime = new GregorianCalendar();
                launchTime.add(Calendar.MINUTE, 1);
                SimpleDateFormat out = new SimpleDateFormat("h:mm a");
                String[] testTimes = new String[times.length+1];
                System.arraycopy(times, 0, testTimes, 0, times.length);
                testTimes[times.length] = out.format(launchTime.getTime());
                times = testTimes;
            
            settings.put("time", times);
            settings.put("inventory_buffer", Integer.valueOf(request.getParameter("inventory_buffer")));
            
            if (!onoff.equalsIgnoreCase(settings.getString("onoff"))) {
                settings.put("onoff", onoff);
                if (emails != null && emails.length() > 0) {
                    for (String email : emails.split("\n")) {
                        try {
                            InternetAddress.parse(email);
                            if (email.indexOf("@") > 0 && email.indexOf(".") > 0) {
                                GregorianCalendar cal = new GregorianCalendar();
                                String stat = onoff.equalsIgnoreCase("checked") ? "ON" : "OFF";
                                Emailer.Send(AKC_Creds.ANH_EMAIL, AKC_Creds.ANH_EMAIL_PWD, email, "Auto notification from application server", "Amazon order shipments auto update switched " + stat + " on " + in.format(cal.getTime()));
                            }
                        } catch (MessagingException ex) {
                            System.err.println(ex.getMessage());
                        }
                    }
                }
            }
            
            if (TextFileReadWrite.writeFile(settings.toString(), settingsFile)) {
                System.out.println("Saved settings\r\n" + settings.toString(3));
            } else {
                System.out.println("Saving settings failed");
            }
            response.sendRedirect(getServletContext().getContextPath() + "/AmazonInventoryModuleSettings");
        } else {
            getServletContext()
                    .getRequestDispatcher("/amazon_inventory_module_settings.jsp")
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
