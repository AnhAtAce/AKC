/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package AMZ;

import Utility.DBController;
import Utility.TextFileReadWrite;
import com.amazonservices.mws.client.MwsUtl;
import com.amazonservices.mws.orders._2013_09_01.MarketplaceWebServiceOrders;
import com.amazonservices.mws.orders._2013_09_01.MarketplaceWebServiceOrdersAsyncClient;
import com.amazonservices.mws.orders._2013_09_01.MarketplaceWebServiceOrdersClient;
import com.amazonservices.mws.orders._2013_09_01.MarketplaceWebServiceOrdersConfig;
import com.amazonservices.mws.orders._2013_09_01.MarketplaceWebServiceOrdersException;
import com.amazonservices.mws.orders._2013_09_01.model.ListOrderItemsRequest;
import com.amazonservices.mws.orders._2013_09_01.model.ListOrderItemsResponse;
import com.amazonservices.mws.orders._2013_09_01.model.ListOrdersRequest;
import com.amazonservices.mws.orders._2013_09_01.model.ListOrdersResponse;
import com.amazonservices.mws.orders._2013_09_01.model.ResponseHeaderMetadata;
import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import javax.xml.datatype.XMLGregorianCalendar;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

/**
 *
 * @author AN2
 */
public class XMLOrderConfirmationFeedBuilder {

    /**
     * Developer AWS access key.
     */
    private static final String accessKey = AKC_Creds.ACCESS_KEY;//"replaceWithAccessKey";

    /**
     * Developer AWS secret key.
     */
    private static final String secretKey = AKC_Creds.SECRET_KEY;//"replaceWithSecretKey";

    /**
     * The client application name.
     */
    private static final String appName = AKC_Creds.APP_NAME;//"replaceWithAppName";

    /**
     * The client application version.
     */
    private static final String appVersion = AKC_Creds.APP_VERSION;//"replaceWithAppVersion";

    /**
     * The endpoint for region service and version. ex: serviceURL =
     * MWSEndpoint.NA_PROD.toString();
     */
    private static final String serviceURL = "https://" + AKC_Creds.SERVICE_URL + AKC_Creds.ORDERS_REQUEST_URI;//"replaceWithServiceURL";

    /**
     * The client, lazy initialized. Async client is also a sync client.
     */
    private static MarketplaceWebServiceOrdersAsyncClient client = null;

    private static final SimpleDateFormat in = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    private static final SimpleDateFormat out = new SimpleDateFormat("MM/dd/yyyy HH:mm");

    XMLOrderConfirmationFeedBuilder() {

    }

    public static HashMap<String, JSONObject> getUnshippedOrders() {

        // Get a client connection.
        // Make sure you've set the variables in MarketplaceWebServiceOrdersSampleConfig.
        MarketplaceWebServiceOrdersClient listOrderClient = getClient();

        // Create a request.
        ListOrdersRequest request = new ListOrdersRequest();

        String sellerId = AKC_Creds.MERCHANT_ID;//"example";
        request.setSellerId(sellerId);

        String mwsAuthToken = AKC_Creds.MWS_AUTH_TOKEN;//"example";
        request.setMWSAuthToken(mwsAuthToken);

        XMLGregorianCalendar createdAfter;// = MwsUtl.getDTF().newXMLGregorianCalendar();
        GregorianCalendar gregCalCreateAfter = new GregorianCalendar();
        gregCalCreateAfter.set(GregorianCalendar.DAY_OF_YEAR, -8);
        createdAfter = MwsUtl.getDTF().newXMLGregorianCalendar(gregCalCreateAfter);
        request.setCreatedAfter(createdAfter);

        //XMLGregorianCalendar createdBefore = MwsUtl.getDTF().newXMLGregorianCalendar();
        //request.setCreatedBefore(createdBefore);
        //XMLGregorianCalendar lastUpdatedAfter = MwsUtl.getDTF().newXMLGregorianCalendar();
        //request.setLastUpdatedAfter(lastUpdatedAfter);
        //XMLGregorianCalendar lastUpdatedBefore = MwsUtl.getDTF().newXMLGregorianCalendar();
        //request.setLastUpdatedBefore(lastUpdatedBefore);
        List<String> orderStatus = new ArrayList<>();
        //Amazon gives back old and handled partial orders on "PartiallyShipped" status
        orderStatus.add("PartiallyShipped");
        orderStatus.add("Unshipped");
        request.setOrderStatus(orderStatus);

        List<String> marketplaceId = new ArrayList<>();
        marketplaceId.add(AKC_Creds.MARKETPLACE_ID);
        request.setMarketplaceId(marketplaceId);

        List<String> fulfillmentChannel = new ArrayList<>();
        fulfillmentChannel.add("MFN");
        request.setFulfillmentChannel(fulfillmentChannel);
        //List<String> paymentMethod = new ArrayList<String>();
        //request.setPaymentMethod(paymentMethod);
        //String buyerEmail = "example";
        //request.setBuyerEmail(buyerEmail);
        //String sellerOrderId = "example";
        //request.setSellerOrderId(sellerOrderId);
        Integer maxResultsPerPage = 100;
        request.setMaxResultsPerPage(maxResultsPerPage);
        //List<String> tfmShipmentStatus = new ArrayList<String>();
        //request.setTFMShipmentStatus(tfmShipmentStatus);

        // Make the call.
        JSONArray ordersJSONArray = invokeListOrders(listOrderClient, request);

        HashMap<String, JSONObject> amzOrderSummary = new HashMap<>();
        for (int i = 0; i < ordersJSONArray.length(); i++) {
            amzOrderSummary.put(ordersJSONArray.getJSONObject(i).getString("AmazonOrderId"), ordersJSONArray.getJSONObject(i));
        }
        return amzOrderSummary;
    }

    public static JSONObject getOrdersDetail(String amazonOrderId) {
        JSONObject returnThis;

        // Get a client connection.
        // Make sure you've set the variables in MarketplaceWebServiceOrdersSampleConfig.
        MarketplaceWebServiceOrdersClient clientLocal;
        clientLocal = getClient();

        // Create a request.
        ListOrderItemsRequest request = new ListOrderItemsRequest();
        String sellerId = AKC_Creds.MERCHANT_ID;//"example";
        request.setSellerId(sellerId);
        String mwsAuthToken = AKC_Creds.MWS_AUTH_TOKEN;//"example";
        request.setMWSAuthToken(mwsAuthToken);
        request.setAmazonOrderId(amazonOrderId);

        // Make the call.
        returnThis = invokeListOrderItems(clientLocal, request);

        return returnThis;
    }

    public static void generateShipConfirmXML() {
        HashMap<String, JSONObject> amzUnshippedOrderSummary = getUnshippedOrders();//new HashMap<>();
        HashMap<String, String> amzOrderIDTrackingList = new HashMap<>();
        HashMap<String, String> amzOrderIDTrackingListFromDB = new HashMap<>();

        ArrayList<String> unshippedIDs = new ArrayList<>(amzUnshippedOrderSummary.keySet());

        //test amazon order IDs
        /*
        unshippedIDs.add("108-8000458-0596216");
       */

        GregorianCalendar fromDate = new GregorianCalendar();
        fromDate.set(Calendar.DAY_OF_YEAR, -5);

        ResultSet shippedAMZOrders;
        if (DBController.createConnection()) {
            //shippedAMZOrders = DBController.getShippedAmzOrders(out.format(fromDate.getTime()));
            shippedAMZOrders = DBController.getShippedAmzOrders(unshippedIDs);
            try {
                while (shippedAMZOrders.next()) {
                    amzOrderIDTrackingListFromDB.put(shippedAMZOrders.getString(1).trim(), shippedAMZOrders.getString(4).trim());
                    System.out.println(shippedAMZOrders.getString(1) + "::" + shippedAMZOrders.getString(4));
                }
            } catch (SQLException ex) {
                System.out.println(ex.getMessage());
            }
        }
        
        GregorianCalendar today = new GregorianCalendar();
        today.set(today.get(Calendar.YEAR),today.get(Calendar.MONTH),today.get(Calendar.DAY_OF_YEAR),0,0,0);
        GregorianCalendar shipDueDate = new GregorianCalendar();
        
        for (String key : amzUnshippedOrderSummary.keySet()) {
            //System.out.println(amzUnshippedOrderSummary.get(key).toString(3));
            //System.out.println("]}>"+amzUnshippedOrderSummary.get(key).getString("LatestShipDate"));
            String trackingNum = amzOrderIDTrackingListFromDB.get(key);
            if (trackingNum != null) {
                Date shipDueDateDate = null;
                try {
                    shipDueDateDate = in.parse(amzUnshippedOrderSummary.get(key).getString("LatestShipDate"));
                    shipDueDate.setTime(shipDueDateDate);
                    shipDueDate.set(shipDueDate.get(Calendar.YEAR), shipDueDate.get(Calendar.MONTH), shipDueDate.get(Calendar.DAY_OF_YEAR), 0, 0, 0);
                } catch (ParseException ex) {
                    shipDueDate.set(shipDueDate.get(Calendar.YEAR),shipDueDate.get(Calendar.MONTH),shipDueDate.get(Calendar.DAY_OF_YEAR),0,0,0);
                }
                
                if(trackingNum.length() > 1)
                {
                    amzOrderIDTrackingList.put(key, amzOrderIDTrackingListFromDB.get(key));
                }else if(shipDueDate.getTimeInMillis() == today.getTimeInMillis())
                {
                    amzOrderIDTrackingList.put(key, amzOrderIDTrackingListFromDB.get(key));
                }
                //System.out.println("Shipped Order: " + key + " > " + amzOrderIDTrackingListFromDB.get(key));
            } else {
                amzOrderIDTrackingList.remove(key);
            }
        }

        //amzOrderIDTrackingList holds order IDs and Order tracking #
        HashMap<String, JSONObject> amzUnshippedOrderDetails = new HashMap<>();

        StringBuilder sb = new StringBuilder(10280);
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?><AmazonEnvelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"amzn-envelope.xsd\">");
        sb.append("<Header>");
        sb.append("<DocumentVersion>1.01</DocumentVersion>");
        sb.append("<MerchantIdentifier>Ace Karaoke</MerchantIdentifier>");
        sb.append("</Header>");
        sb.append("<MessageType>OrderFulfillment</MessageType>");

        SimpleDateFormat amzDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        int messageNum = 0;
        for (String key : amzOrderIDTrackingList.keySet()) {
            messageNum++;
            JSONObject currentOrder = getOrdersDetail(key).getJSONObject("ListOrderItemsResponse").getJSONObject("ListOrderItemsResult");
            amzUnshippedOrderDetails.put(key, currentOrder);
                        
            String orderID = currentOrder.getString("AmazonOrderId");
            String tracking = amzOrderIDTrackingList.get(orderID).trim();

            System.out.println(">>>"+messageNum);
            System.out.println(">>>"+orderID);
            System.out.println(">>>"+tracking);

            sb.append("<Message>");
            sb.append("<MessageID>");
            sb.append(messageNum);
            sb.append("</MessageID>");
            sb.append("<OrderFulfillment>");
            sb.append("<AmazonOrderID>");
            System.out.println("-=-=-=-=-=-=-\n" + getOrdersDetail(key).toString(3));
            sb.append(orderID);
            sb.append("</AmazonOrderID>");
            sb.append("<FulfillmentDate>");
            sb.append(amzDateFormat.format(new GregorianCalendar().getTime()));
            sb.append("</FulfillmentDate>");
            sb.append("<FulfillmentData>");
            sb.append("<CarrierCode>");
            String shipper = tracking.length() == 18 ? "UPS" : tracking.length() == 30 || tracking.length() == 22 ? "USPS" : "Other";
            sb.append(shipper);
            sb.append("</CarrierCode>");
            sb.append("<ShippingMethod> </ShippingMethod>");
            sb.append("<ShipperTrackingNumber>");
            tracking = tracking.length() > 0? tracking:"N/A";
            sb.append(tracking);
            sb.append("</ShipperTrackingNumber>");
            sb.append("</FulfillmentData>");
            sb.append("</OrderFulfillment>");
            sb.append("</Message>");
        }
        sb.append("</AmazonEnvelope>");
        File file = new File("AMZ_Orders_Tracking_Feed.xml");
        System.out.println(file.getAbsoluteFile());
        System.out.println(sb.toString());
        TextFileReadWrite.writeFile(sb.toString(), file);
    }

    /**
     * Get a client connection ready to use.
     *
     * @return A ready to use client connection.
     */
    private static MarketplaceWebServiceOrdersClient getClient() {
        return getAsyncClient();
    }

    /**
     * Get an async client connection ready to use.
     *
     * @return A ready to use client connection.
     */
    private static synchronized MarketplaceWebServiceOrdersAsyncClient getAsyncClient() {
        if (client == null) {
            MarketplaceWebServiceOrdersConfig config = new MarketplaceWebServiceOrdersConfig();
            config.setServiceURL(serviceURL);
            // Set other client connection configurations here.
            client = new MarketplaceWebServiceOrdersAsyncClient(accessKey, secretKey,
                    appName, appVersion, config, null);
        }
        return client;
    }

    public static JSONArray invokeListOrders(
            MarketplaceWebServiceOrders client,
            ListOrdersRequest request) {
        try {
            //System.out.println("Invoked: invokeListOrders");
            // Call the service.
            ListOrdersResponse response = client.listOrders(request);
            ResponseHeaderMetadata rhmd = response.getResponseHeaderMetadata();
            //We recommend logging every the request id and timestamp of every call.
            //System.out.println("Response:");
            //System.out.println("RequestId: " + rhmd.getRequestId());
            //System.out.println("Timestamp: " + rhmd.getTimestamp());
            //System.out.println(response.toXML());

            JSONObject xmlJSONObj = null;
            try {
                xmlJSONObj = XML.toJSONObject(response.toXML());
                //System.out.println(xmlJSONObj.toString(3));
            } catch (JSONException je) {
                System.out.println("Error occured in getUnshippedOrders()->JSON operation");
                System.out.println(je.toString());
                System.out.println("End of error messages");
            }

            JSONArray ordersJSONArray;
            //JSONArray ordersJSONArray = ordersJSON.getJSONArray("Order");
            try {
                ordersJSONArray = xmlJSONObj.getJSONObject("ListOrdersResponse").getJSONObject("ListOrdersResult").getJSONObject("Orders").getJSONArray("Order");
            } catch (JSONException ex) {
                ordersJSONArray = new JSONArray();
                xmlJSONObj = xmlJSONObj.getJSONObject("ListOrdersResponse").getJSONObject("ListOrdersResult").getJSONObject("Orders").getJSONObject("Order");
                ordersJSONArray.put(xmlJSONObj);
            }

            return ordersJSONArray;
        } catch (MarketplaceWebServiceOrdersException ex) {
            // Exception properties are important for diagnostics.
            System.out.println("Service Exception:");
            ResponseHeaderMetadata rhmd = ex.getResponseHeaderMetadata();
            if (rhmd != null) {
                System.out.println("RequestId: " + rhmd.getRequestId());
                System.out.println("Timestamp: " + rhmd.getTimestamp());
            }
            System.out.println("Message: " + ex.getMessage());
            System.out.println("StatusCode: " + ex.getStatusCode());
            System.out.println("ErrorCode: " + ex.getErrorCode());
            System.out.println("ErrorType: " + ex.getErrorType());
            throw ex;
        }
    }

    public static JSONObject invokeListOrderItems(
            MarketplaceWebServiceOrders client,
            ListOrderItemsRequest request) {
        try {
            // Call the service.
            ListOrderItemsResponse response = client.listOrderItems(request);
            ResponseHeaderMetadata rhmd = response.getResponseHeaderMetadata();
            //We recommend logging every the request id and timestamp of every call.
            //System.out.println("Response:");
            //System.out.println("RequestId: " + rhmd.getRequestId());
            //System.out.println("Timestamp: " + rhmd.getTimestamp());
            //System.out.println(response.toXML());
            JSONObject orderItems = XML.toJSONObject(response.toXML());
            return orderItems;
        } catch (MarketplaceWebServiceOrdersException ex) {
            // Exception properties are important for diagnostics.
            System.out.println("Service Exception:");
            ResponseHeaderMetadata rhmd = ex.getResponseHeaderMetadata();
            if (rhmd != null) {
                System.out.println("RequestId: " + rhmd.getRequestId());
                System.out.println("Timestamp: " + rhmd.getTimestamp());
            }
            System.out.println("Message: " + ex.getMessage());
            System.out.println("StatusCode: " + ex.getStatusCode());
            System.out.println("ErrorCode: " + ex.getErrorCode());
            System.out.println("ErrorType: " + ex.getErrorType());
            throw ex;
        }
    }
}
