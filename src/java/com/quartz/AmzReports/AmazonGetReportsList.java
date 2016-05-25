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
import Utility.TextFileReadWrite;
import com.amazonaws.mws.model.GetReportRequest;
import com.amazonaws.mws.model.GetReportRequestListRequest;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class AmazonGetReportsList implements Job {

    JSONObject settings;
    File settingsFile = new File("AMZ_Inventory_Module_Settings.json");
    SimpleDateFormat inUTC = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    private void requestReport(){
        GetReport get = new GetReport();
        final String merchantId = AKC_Creds.MERCHANT_ID;

        GetReportRequestListRequest requestReportsList = new GetReportRequestListRequest()
                .withMerchant(merchantId);

        JSONObject jsonReportResult = get.requestReport(requestReportsList).getJSONObject("GetReportRequestListResponse")
                .getJSONObject("GetReportRequestListResult");

        JSONArray reportsArray = jsonReportResult.getJSONArray("ReportRequestInfo");
        JSONObject tR, latestFlatOpenListingReport = new JSONObject();
        latestFlatOpenListingReport.put("CompletedDate", "2000-05-18T23:40:19Z");

        String lastestCompleteDate = latestFlatOpenListingReport.getString("CompletedDate");
        Date cDDate = null, lDDate = null;
        try {
            lDDate = inUTC.parse(lastestCompleteDate);
        } catch (ParseException ex) {
            Logger.getLogger(AmazonGetReportsList.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        for (int i = 0; i < reportsArray.length(); i++) {
            tR = reportsArray.getJSONObject(i);

            if (tR.getString("ReportType").equals("_GET_FLAT_FILE_OPEN_LISTINGS_DATA_")) {
                try {
                    cDDate = inUTC.parse(tR.getString("CompletedDate"));
                } catch (ParseException ex) {
                    Logger.getLogger(AmazonGetReportsList.class.getName()).log(Level.SEVERE, null, ex);
                }

                if (cDDate.after(lDDate)) {
                    latestFlatOpenListingReport = tR;
                    try {
                        lDDate = inUTC.parse(latestFlatOpenListingReport.getString("CompletedDate"));
                    } catch (ParseException ex) {
                        Logger.getLogger(AmazonGetReportsList.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
        if(!settings.has("LatestQtyReport"))
        {
            JSONObject t = new JSONObject();
            t.put("ReportRequestId", "");
            settings.put("LatestQtyReport", t);
        }
               
        String storedLastestQtyReportRequestID = settings.getJSONObject("LatestQtyReport").getString("ReportRequestId");
      
        if (!storedLastestQtyReportRequestID.equals(latestFlatOpenListingReport.getString("ReportRequestId"))) {
            latestFlatOpenListingReport.put("Downloaded", false);
            settings.put("LatestQtyReport", latestFlatOpenListingReport);
            System.err.println("New AMZ qty report file found in list!\n"+latestFlatOpenListingReport.toString(3));
        }else
        {
            System.err.println("No new AMZ qty report file found in list!\n"+latestFlatOpenListingReport.toString(3));
        }

        if (!latestFlatOpenListingReport.getBoolean("Downloaded") && 
                latestFlatOpenListingReport.getString("ReportProcessingStatus").equals("_DONE_")) {
            //start downloading the compteted report
            System.err.println("Getting the inventory report!!!!!!!!!!!!!!!!!!!!!");
            
            GetReportRequest reportRequest = new GetReportRequest();
            reportRequest.setMerchant(merchantId);
            reportRequest.setReportId(latestFlatOpenListingReport.getString("GeneratedReportId"));
            reportRequest.setMWSAuthToken(AKC_Creds.MWS_AUTH_TOKEN);
            
            JSONObject inventoryReport = get.requestReport(reportRequest);

            inventoryReport.put("Date", latestFlatOpenListingReport.getString("CompletedDate"));
            TextFileReadWrite.writeFile(inventoryReport.toString(3), new File("Amz Inventory report.json"));
            latestFlatOpenListingReport.put("Downloaded", true);
            settings.put("LatestQtyReport", latestFlatOpenListingReport);
            System.err.println("New inventory report file downloaded!!!");
        }    
        
        TextFileReadWrite.writeFile(settings.toString(3), settingsFile);
    }

    public void execute(JobExecutionContext context)
            throws JobExecutionException {
        
        settings = new JSONObject(TextFileReadWrite.readJSONFile(settingsFile));
        
        SimpleDateFormat out = new SimpleDateFormat("hh:mm a");
        System.out.println(":::>>>Amazon Reports List Job Time Check @ " + out.format(new Date()));
        
        String onoff = settings.get("onoff").toString();
        if (onoff.equalsIgnoreCase("checked")) {
            requestReport();
        } else {
            System.err.println("Amazon Qty Report Job Not Scheduled At This Time.");
        }
    }
}
