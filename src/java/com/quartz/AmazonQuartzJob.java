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
import AMZ.XMLOrderConfirmationFeedBuilder;
import Utility.Emailer;
import Utility.TextFileReadWrite;
import com.amazonaws.mws.MarketplaceWebService;
import com.amazonaws.mws.MarketplaceWebServiceClient;
import com.amazonaws.mws.MarketplaceWebServiceConfig;
import com.amazonaws.mws.MarketplaceWebServiceException;
import com.amazonaws.mws.model.FeedSubmissionInfo;
import com.amazonaws.mws.model.IdList;
import com.amazonaws.mws.model.ResponseMetadata;
import com.amazonaws.mws.model.SubmitFeedRequest;
import com.amazonaws.mws.model.SubmitFeedResponse;
import com.amazonaws.mws.model.SubmitFeedResult;
import com.opencsv.CSVWriter;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import org.json.XML;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class AmazonQuartzJob implements Job {

    private final File file = new File("amazon_module_settings.txt");
    private FileWriter fw;
    private CSVWriter writer;
    BufferedWriter bw;
    SimpleDateFormat in = new SimpleDateFormat("MM/dd/yyyy");
    SimpleDateFormat out1 = new SimpleDateFormat("MM/dd/yyyy HH:mm");
    HashMap<String, String> settingsHM = new HashMap<>();

    private void writeUpdateTimestamp() {
        try {
            fw = new FileWriter(file);
            bw = new BufferedWriter(fw);
            settingsHM.put("lastUpdate", out1.format(new Date()));
            for (String k : settingsHM.keySet()) {
                bw.write(k + "=" + settingsHM.get(k) + "\r\n");
            }
            bw.close();
            fw.close();
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }

    /**
     * Calculate content MD5 header values for feeds stored on disk.
     */
    public String computeContentMD5HeaderValue(FileInputStream fis)
            throws IOException, NoSuchAlgorithmException {
        DigestInputStream dis = new DigestInputStream(fis,
                MessageDigest.getInstance("MD5"));
        byte[] buffer = new byte[8192];
        while (dis.read(buffer) > 0);
        String md5Content = new String(
                org.apache.commons.codec.binary.Base64.encodeBase64(
                        dis.getMessageDigest().digest()));
        // Effectively resets the stream to be beginning of the file
        // via a FileChannel.
        fis.getChannel().position(0);
        return md5Content;
    }
    
    private void submitFeed() throws IOException {

        /**
         * **********************************************************************
         * Access Key ID and Secret Access Key ID, obtained from:
         * http://aws.amazon.com
         **********************************************************************
         */
        final String accessKeyId = AKC_Creds.ACCESS_KEY;//"<Your Access Key ID>";
        final String secretAccessKey = AKC_Creds.SECRET_KEY;//"<Your Secret Access Key>";

        final String appName = "Ace karaoke";//<Your Application or Company Name>";
        final String appVersion = "0.1";//<Your Application Version or Build Number or Release Date>";

        File feedFile = new File("AMZ_Orders_Tracking_Feed.xml");
        
        MarketplaceWebServiceConfig config = new MarketplaceWebServiceConfig();
                
        /**
         * **********************************************************************
         * Uncomment to set the appropriate MWS endpoint.
         ***********************************************************************
         */
        // US
        config.setServiceURL("https://mws.amazonservices.com");
        // UK
        // config.setServiceURL("https://mws.amazonservices.co.uk");
        // Germany
        // config.setServiceURL("https://mws.amazonservices.de");
        // France
        // config.setServiceURL("https://mws.amazonservices.fr");
        // Italy
        // config.setServiceURL("https://mws.amazonservices.it");
        // Japan
        // config.setServiceURL("https://mws.amazonservices.jp");
        // China
        // config.setServiceURL("https://mws.amazonservices.com.cn");
        // Canada
        // config.setServiceURL("https://mws.amazonservices.ca");
        // India
        // config.setServiceURL("https://mws.amazonservices.in");
        /**
         * **********************************************************************
         * You can also try advanced configuration options. Available options
         * are:
         *
         * - Signature Version - Proxy Host and Proxy Port - User Agent String
         * to be sent to Marketplace Web Service
         *
         **********************************************************************
         */
        /**
         * **********************************************************************
         * Instantiate Http Client Implementation of Marketplace Web Service        
         **********************************************************************
         */   
        
        System.out.println("creating service object...");
        
        MarketplaceWebService service = new MarketplaceWebServiceClient(
                accessKeyId, secretAccessKey, appName, appVersion, config);

        System.out.println("service object created");

        
        /**
         * **********************************************************************
         * Setup request parameters and uncomment invoke to try out sample for
         * Submit Feed
         **********************************************************************
         */
        /**
         * **********************************************************************
         * Marketplace and Merchant IDs are required parameters for all
         * Marketplace Web Service calls.
         **********************************************************************
         */
        final String merchantId = AKC_Creds.MERCHANT_ID;//"<Your Merchant ID>";
        final String sellerDevAuthToken = AKC_Creds.MWS_AUTH_TOKEN;//"<Merchant Developer MWS Auth Token>";
        // marketplaces to which this feed will be submitted; look at the
        // API reference document on the MWS website to see which marketplaces are
        // included if you do not specify the list yourself
        final IdList marketplaces = new IdList(Arrays.asList(
                "ATVPDKIKX0DER"));

        SubmitFeedRequest request = new SubmitFeedRequest();
        request.setMerchant(merchantId);
        request.setMWSAuthToken(sellerDevAuthToken);
        request.setMarketplaceIdList(marketplaces);

        request.setFeedType("_POST_ORDER_FULFILLMENT_DATA_");
        
        FileInputStream fs;
        try {
            fs = new FileInputStream(feedFile);
            request.setContentMD5(this.computeContentMD5HeaderValue(fs));
        } catch (FileNotFoundException ex) {
            System.out.println("Filestream error occured for feed content file");
        } catch (NoSuchAlgorithmException ex) {
            System.out.println("Error: NoSuchAlgorithmException");
        }
        
        try {
            // MWS exclusively offers a streaming interface for uploading your
            // feeds. This is because
            // feed sizes can grow to the 1GB+ range - and as your business grows
            // you could otherwise
            // silently reach the feed size where your in-memory solution will no
            // longer work, leaving you
            // puzzled as to why a solution that worked for a long time suddenly
            // stopped working though
            // you made no changes. For the same reason, we strongly encourage you
            // to generate your feeds to
            // local disk then upload them directly from disk to MWS via Java -
            // without buffering them in Java
            // memory in their entirety.
            // Note: MarketplaceWebServiceClient will not retry a submit feed request
            // because there is no way to reset the InputStream from our client.
            // To enable retry, recreate the InputStream and resubmit the feed
            // with the new InputStream.
            //
            request.setFeedContent( new FileInputStream(feedFile /*or
            // "my-flat-file.txt" if you use flat files*/));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(AmazonQuartzJob.class.getName()).log(Level.SEVERE, null, ex);
        }
        invokeSubmitFeed(service, request);

    }

    /**
     * Submit Feed request sample Uploads a file for processing together with
     * the necessary metadata to process the file, such as which type of feed it
     * is. PurgeAndReplace if true means that your existing e.g. inventory is
     * wiped out and replace with the contents of this feed - use with caution
     * (the default is false).
     *
     * @param service instance of MarketplaceWebService service
     * @param request Action to invoke
     */
    public static void invokeSubmitFeed(MarketplaceWebService service,
            SubmitFeedRequest request) {
        try {

            SubmitFeedResponse response = service.submitFeed(request);

            System.out.println("SubmitFeed Action Response");
            System.out
                    .println("=============================================================================");
            System.out.println();

            System.out.print("    SubmitFeedResponse");
            System.out.println();
            if (response.isSetSubmitFeedResult()) {
                System.out.print("        SubmitFeedResult");
                System.out.println();
                SubmitFeedResult submitFeedResult = response
                        .getSubmitFeedResult();
                if (submitFeedResult.isSetFeedSubmissionInfo()) {
                    System.out.print("            FeedSubmissionInfo");
                    System.out.println();
                    FeedSubmissionInfo feedSubmissionInfo = submitFeedResult
                            .getFeedSubmissionInfo();
                    if (feedSubmissionInfo.isSetFeedSubmissionId()) {
                        System.out.print("                FeedSubmissionId");
                        System.out.println();
                        System.out.print("                    "
                                + feedSubmissionInfo.getFeedSubmissionId());
                        System.out.println();
                    }
                    if (feedSubmissionInfo.isSetFeedType()) {
                        System.out.print("                FeedType");
                        System.out.println();
                        System.out.print("                    "
                                + feedSubmissionInfo.getFeedType());
                        System.out.println();
                    }
                    if (feedSubmissionInfo.isSetSubmittedDate()) {
                        System.out.print("                SubmittedDate");
                        System.out.println();
                        System.out.print("                    "
                                + feedSubmissionInfo.getSubmittedDate());
                        System.out.println();
                    }
                    if (feedSubmissionInfo.isSetFeedProcessingStatus()) {
                        System.out
                                .print("                FeedProcessingStatus");
                        System.out.println();
                        System.out.print("                    "
                                + feedSubmissionInfo.getFeedProcessingStatus());
                        System.out.println();
                    }
                    if (feedSubmissionInfo.isSetStartedProcessingDate()) {
                        System.out
                                .print("                StartedProcessingDate");
                        System.out.println();
                        System.out
                                .print("                    "
                                        + feedSubmissionInfo
                                        .getStartedProcessingDate());
                        System.out.println();
                    }
                    if (feedSubmissionInfo.isSetCompletedProcessingDate()) {
                        System.out
                                .print("                CompletedProcessingDate");
                        System.out.println();
                        System.out.print("                    "
                                + feedSubmissionInfo
                                .getCompletedProcessingDate());
                        System.out.println();
                    }
                }
            }
            if (response.isSetResponseMetadata()) {
                System.out.print("        ResponseMetadata");
                System.out.println();
                ResponseMetadata responseMetadata = response
                        .getResponseMetadata();
                if (responseMetadata.isSetRequestId()) {
                    System.out.print("            RequestId");
                    System.out.println();
                    System.out.print("                "
                            + responseMetadata.getRequestId());
                    System.out.println();
                }
            }
            System.out.println(response.getResponseHeaderMetadata());
            System.out.println();
            System.out.println();

        } catch (MarketplaceWebServiceException ex) {

            System.out.println("Caught Exception: " + ex.getMessage());
            System.out.println("Response Status Code: " + ex.getStatusCode());
            System.out.println("Error Code: " + ex.getErrorCode());
            System.out.println("Error Type: " + ex.getErrorType());
            System.out.println("Request ID: " + ex.getRequestId());
            System.out.print("XML: " + ex.getXML());
            System.out.println("ResponseHeaderMetadata: " + ex.getResponseHeaderMetadata());
        }
    }

    public void execute(JobExecutionContext context)
            throws JobExecutionException {
        String error = "No error";
        SimpleDateFormat out = new SimpleDateFormat("hh:mm a");
        System.out.println(":::>>>Amazon Job Time Check @ " + out.format(new Date()));

        settingsHM = TextFileReadWrite.readSettings(file);

        String onoff = settingsHM.get("onoff"), emails = settingsHM.get("emails"), lastUpdate = settingsHM.get("lastUpdate");
        ArrayList<String> time = new ArrayList(Arrays.asList(settingsHM.get("time").split(";")));

        if (emails == null) {
            emails = ";";
        }
        Date lastDate;
        try {
            lastDate = out1.parse(lastUpdate);
        } catch (ParseException ex) {
            GregorianCalendar now = new GregorianCalendar();
            now.set(Calendar.DAY_OF_YEAR, -1);
            lastDate = now.getTime();
        }

        String[] emailArr = emails.replace(";;", ";").replace(";", " ").trim().split(" ");
        if (onoff.equalsIgnoreCase("checked")) {
            for (String e : time) {
                Date dScheduled, dNow;
                try {
                    dScheduled = out.parse(e);
                    dNow = out.parse(out.format(new Date()));
                    if (dScheduled.getTime() == dNow.getTime()) {
                        System.out.println("\t---> Amazon task launch");

                        XMLOrderConfirmationFeedBuilder.generateShipConfirmXML();
                        
                        try {
                            this.submitFeed();
                        } catch (IOException ex) {
                            System.out.println("Error occured in submitFeed method");
                            error += "Cannot submit feed.";
                        }
                        
                        try {
                            if (emails != null && emails.length() > 0) {
                                for (String recipient : emailArr) {
                                    InternetAddress.parse(recipient);
                                    if (recipient.indexOf("@") > 0 && recipient.indexOf(".") > 0) {
                                        String subject = "Auto notification from AMZ order update application server";
                                        String msg = "Scheduled Amazon orders update executed.\nError/s: " + error
                                                + "\n\n Update contents:\n\n" + XML.toJSONObject(TextFileReadWrite.readFile(new File("AMZ_Orders_Tracking_Feed.xml"))[0]).toString(3);
                                        Emailer.Send(AKC_Creds.ANH_EMAIL, AKC_Creds.ANH_EMAIL_PWD, recipient, subject, msg);
                                    }
                                }
                            }
                            System.out.println("===[email notifications send]===");
                            writeUpdateTimestamp();
                        } catch (MessagingException ex) {
                            System.out.println(ex.getMessage());
                        }
                        
                    } else {
                        //System.out.println("\txxx> AMZ task time not match | Scheduled: " + out.format(dScheduled) + " Now: " + out.format(dNow));
                    }
                } catch (ParseException ex) {
                    //System.out.println(ex.getMessage());
                    error += " Cannot create XML feed.";
                }
            }
        }

    }
}