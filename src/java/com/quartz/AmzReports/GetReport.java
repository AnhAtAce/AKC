/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author AN2
 */
package com.quartz.AmzReports;

import AMZ.AKC_Creds;
import AMZ.AMZOrders;
import Utility.TextFileReadWrite;
import com.amazonaws.mws.MarketplaceWebService;
import com.amazonaws.mws.MarketplaceWebServiceClient;
import com.amazonaws.mws.MarketplaceWebServiceConfig;
import com.amazonaws.mws.MarketplaceWebServiceException;
import com.amazonaws.mws.model.GetReportRequest;
import com.amazonaws.mws.model.GetReportRequestListRequest;
import com.amazonaws.mws.model.GetReportRequestListResponse;
import com.amazonaws.mws.model.GetReportResponse;
import com.amazonaws.mws.model.RequestReportRequest;
import com.amazonaws.mws.model.RequestReportResponse;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

public class GetReport {
    
    public JSONObject requestReport(GetReportRequest request){
      
        final String accessKeyId = AKC_Creds.ACCESS_KEY;
        final String secretAccessKey = AKC_Creds.SECRET_KEY;

        final String appName = "Anh is cool";
        final String appVersion = "0.1";

        MarketplaceWebServiceConfig config = new MarketplaceWebServiceConfig();
        config.setServiceURL("https://mws.amazonservices.com");

        MarketplaceWebService service = new MarketplaceWebServiceClient(
                accessKeyId, secretAccessKey, appName, appVersion, config);

        JSONObject item, jsonReportRequest = new JSONObject();
        GetReportResponse response1 = null;
        File inventoryFile = new File("AMZ inventory report.txt");

        try {
            OutputStream report = new FileOutputStream(inventoryFile);
            request.setReportOutputStream(report);
            try {
                response1 = service.getReport(request);
            } catch (MarketplaceWebServiceException ex) {
                Logger.getLogger(GetReport.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(GetReport.class.getName()).log(Level.SEVERE, null, ex);
        }

        String[] invt = TextFileReadWrite.readFile(inventoryFile);
        String[] header = invt[0].split("\t");
        String[] itemField;
        JSONArray items = new JSONArray();
        for (int i = 1; i < invt.length; i++) {
            itemField = invt[i].split("\t");
            item = new JSONObject();
            for (int j = 0; j < itemField.length; j++) {
                item.put(header[j], itemField[j]);
            }
            items.put(addChildSKU(item));
        }
        jsonReportRequest.put("Listings", items);
        if (inventoryFile.exists()) {
            inventoryFile.delete();
        }
        return jsonReportRequest;
    }

    JSONObject addChildSKU(JSONObject in)
    {
        String sku = in.getString("sku").replace("_", " ");
        sku = sku.replace(" FBA-9", "")
                .replace(" FBA-8", "")
                .replace(" FBA-7", "")
                .replace(" FBA-6", "")
                .replace(" FBA-5", "")
                .replace(" FBA-4", "")
                .replace(" FBA-3", "")
                .replace(" FBA-2", "")
                .replace(" FBA-1", "")
                .replace(" FBA-0", "")
                .replace(" FBM-9", "")
                .replace(" FBM-8", "")
                .replace(" FBM-7", "")
                .replace(" FBM-6", "")
                .replace(" FBM-5", "")
                .replace(" FBM-4", "")
                .replace(" FBM-3", "")
                .replace(" FBM-2", "")
                .replace(" FBM-1", "")
                .replace(" FBM-0", "")
                .replace(" AKC-9", "")
                .replace(" AKC-8", "")
                .replace(" AKC-7", "")
                .replace(" AKC-6", "")
                .replace(" AKC-5", "")
                .replace(" AKC-4", "")
                .replace(" AKC-3", "")
                .replace(" AKC-2", "")
                .replace(" AKC-1", "")
                .replace(" AKC-0", "")
                .replace(" FBA", "")
                .replace(" FBM", "")
                .replace(" AKC", "")
                .replace("  "," ").trim();
        String[] skus;
        if(sku.contains(" "))
        {
            skus = sku.split(" ");
            in.put("ChildSKUs", skus);
        }
        else
        {
            skus = new String[1];
            skus[0] = sku;
            in.put("ChildSKUs", skus);
        }
        return in;
    }
    
    public JSONObject requestReport(GetReportRequestListRequest request){
      
        final String accessKeyId = AKC_Creds.ACCESS_KEY;
        final String secretAccessKey = AKC_Creds.SECRET_KEY;

        final String appName = "Anh is cool";
        final String appVersion = "0.1";

        MarketplaceWebServiceConfig config = new MarketplaceWebServiceConfig();
        config.setServiceURL("https://mws.amazonservices.com");

        MarketplaceWebService service = new MarketplaceWebServiceClient(
                accessKeyId, secretAccessKey, appName, appVersion, config);

        JSONObject jsonReportRequest = new JSONObject();
        GetReportRequestListResponse response1 = null;
        try {
            response1 = service.getReportRequestList(request);
            jsonReportRequest = new JSONObject(response1.toJSON());
        } catch (MarketplaceWebServiceException ex) {
            Logger.getLogger(GetReport.class.getName()).log(Level.SEVERE, null, ex);
        }

        return jsonReportRequest;
    }
    
    public JSONObject requestReport(RequestReportRequest request){
      
        final String accessKeyId = AKC_Creds.ACCESS_KEY;
        final String secretAccessKey = AKC_Creds.SECRET_KEY;

        final String appName = "Anh is cool";
        final String appVersion = "0.1";

        MarketplaceWebServiceConfig config = new MarketplaceWebServiceConfig();
        config.setServiceURL("https://mws.amazonservices.com");

        MarketplaceWebService service = new MarketplaceWebServiceClient(
                    accessKeyId, secretAccessKey, appName, appVersion, config);
        
        JSONObject jsonReportRequest = null;
        try {
            RequestReportResponse response1 = service.requestReport(request);
            jsonReportRequest = new JSONObject(response1.toJSON());
        } catch (MarketplaceWebServiceException ex) {
            Logger.getLogger(AMZOrders.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return jsonReportRequest;
    }
}
