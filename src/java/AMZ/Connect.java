/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package AMZ;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

/**
 *
 * @author AN2
 */
public class Connect {

    // HTTP GET request
    public JSONObject sendGet(String signedURL) throws Exception {

        URL obj = new URL(signedURL);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        // optional default is GET
        con.setRequestMethod("GET");

        //add request header
        //con.setRequestProperty("User-Agent", USER_AGENT);
        int responseCode = con.getResponseCode();
        System.out.println("\nSending 'GET' request to URL : " + signedURL);
        System.out.println("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        
        String inputLine;
        
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        JSONObject xmlJSONObj = null;
        try {
            xmlJSONObj = XML.toJSONObject(response.toString());
            //String jsonPrettyPrintString = xmlJSONObj.toString(4);
            //System.out.println(jsonPrettyPrintString);
        } catch (JSONException je) {
            System.out.println("Error occured in sendGet()->JSON operation");
            System.out.println(je.toString());
            System.out.println("End of error messages");
        }
        
        if (responseCode == 503){
            xmlJSONObj = null;
        }

        //print result
        //System.out.println(response.toString());
        return xmlJSONObj;
    }
    
// HTTP Post request
    public JSONObject sendPost(String signedURL) throws Exception {

        URL obj = new URL(signedURL);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        // optional default is GET
        con.setRequestMethod("POST");

        //add request header
        //con.setRequestProperty("User-Agent", USER_AGENT);
        int responseCode = con.getResponseCode();
        System.out.println("\nSending 'GET' request to URL : " + signedURL);
        System.out.println("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        
        String inputLine;
        
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        JSONObject xmlJSONObj = null;
        try {
            xmlJSONObj = XML.toJSONObject(response.toString());
            //String jsonPrettyPrintString = xmlJSONObj.toString(4);
            //System.out.println(jsonPrettyPrintString);
        } catch (JSONException je) {
            System.out.println("Error occured in sendGet()->JSON operation");
            System.out.println(je.toString());
            System.out.println("End of error messages");
        }
        
        if (responseCode == 503){
            xmlJSONObj = null;
        }

        //print result
        //System.out.println(response.toString());
        return xmlJSONObj;
    }}
