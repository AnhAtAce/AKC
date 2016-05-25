/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package AMZ;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.TimeZone;
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
public class AMZOrders extends HttpServlet {

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

        SignedRequestsHelper signer = new SignedRequestsHelper();
        Connect getOrder = new Connect();

        String contents = "";
        /*try{*/
        HashMap<String, String> valuePairs = new HashMap<>();
        valuePairs.put("Action", "ListOrders");
        valuePairs.put("SellerId", AKC_Creds.MERCHANT_ID);
        valuePairs.put("SignatureMethod", "HmacSHA256");
        valuePairs.put("SignatureVersion", "2");
        valuePairs.put("Version", "2013-09-01");
        valuePairs.put("CreatedAfter", signer.createAfterTimestamp());
        valuePairs.put("OrderStatus.Status.1", "Unshipped");
        valuePairs.put("OrderStatus.Status.2", "PartiallyShipped");
        valuePairs.put("FulfillmentChannel.Channel.1", "MFN");
        valuePairs.put("MarketplaceId.Id.1", AKC_Creds.MARKETPLACE_ID);
        String signedURL = signer.sign(valuePairs);

        JSONObject jsonOrders = null;
        try {
            jsonOrders = getOrder.sendGet(signedURL).getJSONObject("ListOrdersResponse").getJSONObject("ListOrdersResult").getJSONObject("Orders");
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }

       
        boolean amzError = false;
        JSONArray orderArr;
        JSONObject jsonOrder;
        try {
            orderArr = jsonOrders.getJSONArray("Order");
        } catch (Exception ex) {
            orderArr = new JSONArray();
            try {
                orderArr.put(jsonOrders.getJSONObject("Order"));
            } catch (Exception e) {
                System.out.printf("Error: Nothing returned from Amazon.");
                amzError = true;
            }
        }

        SimpleDateFormat in = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        SimpleDateFormat out = new SimpleDateFormat("MM/dd/yyyy HH:mm");
        SimpleDateFormat shortOut = new SimpleDateFormat("MM/dd/yyyy");
        if (!amzError) {
            contents += "<table class=\"table table-striped\"><tr style='font-weight: bold'><td>#</td><td>Amazon Order ID</td><td>Order Amount</td><td>   Shipping   </td><td>Buyer Name</td><td>Phone</td><td>Order Date/s</td></tr>";
            for (int i = 0; i < orderArr.length(); i++) {
                jsonOrder = orderArr.getJSONObject(i);
                Object phone = (jsonOrder.getJSONObject("ShippingAddress").isNull("Phone") ? "" : jsonOrder.getJSONObject("ShippingAddress").get("Phone"));
                String amazonOrderId = jsonOrder.getString("AmazonOrderId");
                double amount = jsonOrder.getJSONObject("OrderTotal").getDouble("Amount");
                String shipmentServiceLevelCategory = jsonOrder.getString("ShipmentServiceLevelCategory");
                
                String buyerName = jsonOrder.get("BuyerName").toString();
                
                String purchaseDate = jsonOrder.getString("PurchaseDate");
                String latestShipDate = jsonOrder.getString("LatestShipDate");

                try {
                    Date date = in.parse(latestShipDate);
                    GregorianCalendar gCal = new GregorianCalendar();
                    gCal.setTime(date);
                    System.out.println(out.format(gCal.getTime()) + " UTC");
                    gCal.setTimeZone(TimeZone.getTimeZone("PST"));
                    //adjust time offset from UTC to PST
                    gCal.add(Calendar.HOUR_OF_DAY, -7);

                    Date pDate = in.parse(purchaseDate);
                    GregorianCalendar pDCal = new GregorianCalendar();
                    pDCal.setTime(pDate);
                    pDCal.setTimeZone(TimeZone.getTimeZone("PST"));
                    pDCal.add(Calendar.HOUR_OF_DAY, -7);
                    purchaseDate = out.format(pDCal.getTime());

                    GregorianCalendar now = new GregorianCalendar();
                    Date dateNow = now.getTime();
                    long diff = date.getTime() - dateNow.getTime();
                    double diffDays = (double) (diff / (24 * 1000 * 60 * 60));
                    System.out.println(out.format(gCal.getTime()) + " PST " + diffDays);
                    latestShipDate = shortOut.format(gCal.getTime());

                    if (diffDays <= 1) {
                        purchaseDate += " [Shipping Deadline " + latestShipDate + "]";
                    }
                } catch (Exception ex) {
                    System.out.println("Date parse exception thrown");
                }

                String rowData[] = {
                    "" + (i + 1),
                    "" + amazonOrderId,
                    "" + amount,
                    "" + shipmentServiceLevelCategory,
                    "" + buyerName,
                    "" + phone,
                    "" + purchaseDate.replace("T", " ").replace("Z", " UTC (-7)")
                };

                contents += "<tr>";
                //contents += String.join("</td><td>", rowData);
                for (String e : rowData) {
                    contents += "<td>" + e + "</td>";
                }
                contents += "</tr>";

            }//end of for loop
            contents += "</table>";
            
        } else {
            contents = "<p>Error returned from server. Amazon may have throttled us down due to too many requests. Please try back again later.</p>";
        }

        request.setAttribute("contents", contents);
        getServletContext()
                .getRequestDispatcher("/AMZOrders.jsp")
                .forward(request, response);
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
