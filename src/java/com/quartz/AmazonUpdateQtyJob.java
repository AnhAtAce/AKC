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

import Utility.DBController;
import Utility.TextFileReadWrite;
import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class AmazonUpdateQtyJob implements Job {

    JSONObject settings;
    JSONArray amzListings;
    File settingsFile = new File("AMZ_Inventory_Module_Settings.json");
    File listingsFile = new File("Amz Inventory report.json");

    SimpleDateFormat inUTC = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    Date now = new Date();
    SimpleDateFormat out = new SimpleDateFormat("h:mm a");

    

    private void getQtyFromQSLServer() {
        try {
            amzListings = new JSONObject(TextFileReadWrite.readJSONFile(listingsFile)).getJSONArray("Listings");

            JSONObject item;
            String childSku;
            JSONArray childSkus, fbmListings = new JSONArray(), childSkusArray;
            JSONObject childSkuJSONObject;
            int rowCounter;
            if (DBController.createConnection()) {
                System.err.println(inUTC.format(new Date()) + " >>> Getting inventory data from Everest database.");
                String nonInventoriedSKUs = settings.getString("nonInventoriedSKUs");
                ResultSet itemQtyResultSet, kitResultSet, kitItemQtyResultSet;
                for (int i = 0; i < amzListings.length(); i++) {
                    item = amzListings.getJSONObject(i);
                    if (item.has("quantity")) {
                        childSkus = item.getJSONArray("ChildSKUs");
                        childSkusArray = new JSONArray();
                        for (int j = 0; j < childSkus.length(); j++) {
                            rowCounter = 0;
                            childSku = childSkus.getString(j);
                            childSkuJSONObject = new JSONObject();
                            if (!nonInventoriedSKUs.contains(childSku)) {
                                itemQtyResultSet = DBController.getItemStockQuantity(childSku);
                                try {
                                    while (itemQtyResultSet.next()) {
                                        if (itemQtyResultSet.getInt(5) == 3) {
                                            //Kit
                                            kitResultSet = DBController.getKitItems(childSku);
                                            while (kitResultSet.next()) {
                                                JSONObject kItem = new JSONObject();
                                                kItem.put("ItemCode", kitResultSet.getString(1));

                                                kitItemQtyResultSet = DBController.getItemStockQuantity(kitResultSet.getString(1));

                                                while (kitItemQtyResultSet.next()) {
                                                    if (kitItemQtyResultSet.getString(3) != null) {
                                                        kItem.put(kitItemQtyResultSet.getString(3), kitItemQtyResultSet.getDouble(2));
                                                    }
                                                    kItem.put("Inventoried", kitItemQtyResultSet.getString(4));
                                                }

                                                childSkusArray.put(kItem);
                                            }
                                        } else if (itemQtyResultSet.getInt(5) == 0) {
                                            rowCounter++;
                                            childSkuJSONObject.put("ItemCode", childSku);
                                            childSkuJSONObject.put("Inventoried", itemQtyResultSet.getString(4));
                                            if (itemQtyResultSet.getString(3) != null) {
                                                childSkuJSONObject.put(itemQtyResultSet.getString(3), itemQtyResultSet.getDouble(2));
                                            }
                                        }
                                    }
                                    if (rowCounter > 0) {
                                        childSkusArray.put(childSkuJSONObject);
                                    } else {
                                        childSkuJSONObject.put(childSku, "not exist in Everest");
                                        childSkuJSONObject.put("Inventoried", "F");
                                        childSkusArray.put(childSkuJSONObject);
                                    }
                                } catch (SQLException ex) {
                                    System.err.println(ex.getMessage());
                                    Logger.getLogger(AmazonUpdateQtyJob.class.getName()).log(Level.SEVERE, null, ex);
                                    break;
                                }

                            }
                        }

                        int amzQty = Integer.valueOf(item.getString("quantity"));
                        int bufferQty = settings.getInt("inventory_buffer");
                        int actualQty = 0, lowestQty = 999;
                        JSONObject s;
                        String oosItems = "";
                        for (int q = 0; q < childSkusArray.length(); q++) {
                            s = childSkusArray.getJSONObject(q);
                            if (s.getString("").equalsIgnoreCase("F")) {
                                continue;
                            }
                            if (settings.getString("mainBin").equalsIgnoreCase("checked")) {
                                if(s.has("MAIN")){
                                    actualQty += s.getInt("MAIN");
                                }
                            }
                            if (settings.getString("sgMainBin").equalsIgnoreCase("checked")) {
                                if (s.has("RTL04_STF")) {
                                    actualQty += s.getInt("RTL04_STF");
                                }
                            }
                            if ((actualQty - bufferQty) <= 0) {
                                //out of stock
                                item.put("newQty", 0);
                                oosItems += s.getString("ItemCode");
                            }
                        }
                        if (!item.has("newQty")) {
                            item.put("newQty", lowestQty - bufferQty);
                        }
                        item.put("oosChildren", oosItems);

                        item.put("ChildSKUs", childSkusArray);
                        fbmListings.put(item);
                    } else {
                        //no quantity field = FBA item
                    }
                }

            } else {
                System.err.println("Cannot Connect to Database : Qty Update Job");
            }

            TextFileReadWrite.writeFile(fbmListings.toString(3), new File("FBM Listings.json"));
            System.err.println(inUTC.format(new Date()) + " >>> Local inventory json file updated.");
        } catch (JSONException ex) {
            System.err.println(ex.getMessage());
        }
    }

    public void execute(JobExecutionContext context)
            throws JobExecutionException {

        settings = new JSONObject(TextFileReadWrite.readJSONFile(settingsFile));

        String onoff = settings.get("onoff").toString();
        if (onoff.equalsIgnoreCase("checked")) {
            JSONArray sTimes = settings.getJSONArray("time");
            for (int i = 0; i < sTimes.length(); i++) {
                if (sTimes.getString(i).equals(out.format(now))) {
                    System.err.println("Amazon Qty Report Job @ " + out.format(now) + " STARTED.");
                    getQtyFromQSLServer();
                } else {
                    //System.err.println("Amazon Qty Report Job Scheduled At "+sTimes.getString(i)+" Not "+out.format(now)+".");
                }
            }
        } else {
            System.err.println("Amazon Qty Report Job Turn Off.");
        }
    }
}
