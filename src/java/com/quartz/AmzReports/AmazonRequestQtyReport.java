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
import com.amazonaws.mws.model.IdList;
import com.amazonaws.mws.model.RequestReportRequest;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import org.json.JSONObject;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class AmazonRequestQtyReport implements Job {

    JSONObject settings;
    File settingsFile = new File("AMZ_Inventory_Module_Settings.json");
    
    private void requestReport(){
        GetReport get = new GetReport();
        final String merchantId = AKC_Creds.MERCHANT_ID;

        final IdList marketplaces = new IdList(Arrays.asList(
                AKC_Creds.MARKETPLACE_ID));

        RequestReportRequest request1 = new RequestReportRequest()
                .withMerchant(merchantId)
                .withMarketplaceIdList(marketplaces)
                .withReportType("_GET_FLAT_FILE_OPEN_LISTINGS_DATA_")
                .withReportOptions("ShowSalesChannel=true");

        JSONObject jsonReportResult = get.requestReport(request1);
        settings.put("QtyReportRequest", jsonReportResult);
        TextFileReadWrite.writeFile(settings.toString(3), settingsFile);
        System.out.println(":::>>>Amazon Qty Report Requested");
    }

    public void execute(JobExecutionContext context)
            throws JobExecutionException {
     
        settings = new JSONObject(TextFileReadWrite.readJSONFile(settingsFile));

        SimpleDateFormat out = new SimpleDateFormat("hh:mm a");

        String onoff = settings.get("onoff").toString();
        if (onoff.equalsIgnoreCase("checked")) {
            requestReport();
        } else {
            System.err.println("Amazon Seller Central Inventory Updater Switched OFF.");
        }
    }
}
