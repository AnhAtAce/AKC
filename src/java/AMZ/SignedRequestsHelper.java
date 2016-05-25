package AMZ;

import java.io.UnsupportedEncodingException;

import java.net.URLEncoder;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TimeZone;
import java.util.TreeMap;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
//import static org.apache.http.params.CoreProtocolPNames.USER_AGENT;
import org.json.JSONArray;
import org.json.JSONObject;

public class SignedRequestsHelper {

    private static final String UTF8_CHARSET = "UTF-8";
    private static final String HMAC_SHA256_ALGORITHM = "HmacSHA256";
    private static final String REQUEST_METHOD = "GET";
    
    private String endpoint = AKC_Creds.SERVICE_URL;
    private String REQUEST_URI = AKC_Creds.ORDERS_REQUEST_URI;
    private String awsAccessKeyId = AKC_Creds.ACCESS_KEY;
    private String awsSecretKey = AKC_Creds.SECRET_KEY;

    private SecretKeySpec secretKeySpec = null;
    private Mac mac = null;
    
    public void setRequestUri(String url)
    {
        REQUEST_URI = url;
    }

    public SignedRequestsHelper() {
        try {
            byte[] secretyKeyBytes = awsSecretKey.getBytes(UTF8_CHARSET);
            secretKeySpec
                    = new SecretKeySpec(secretyKeyBytes, HMAC_SHA256_ALGORITHM);
            mac = Mac.getInstance(HMAC_SHA256_ALGORITHM);
            mac.init(secretKeySpec);
        } catch (UnsupportedEncodingException ex) {
            System.out.println("Error occured in SignedRequestsHelper constructor");
            System.out.println(ex.getMessage());
            System.out.println("End of error messages");
        } catch (NoSuchAlgorithmException ex) {
            System.out.println("Error occured in SignedRequestsHelper constructor");
            System.out.println(ex.getMessage());
            System.out.println("End of error messages");
        } catch (InvalidKeyException ex) {
            System.out.println("Error occured in SignedRequestsHelper constructor");
            System.out.println(ex.getMessage());
            System.out.println("End of error messages");
        }
    }

    public String sign(Map<String, String> params) {
        params.put("AWSAccessKeyId", awsAccessKeyId);
        params.put("Timestamp", timestamp());

        SortedMap<String, String> sortedParamMap
                = new TreeMap<String, String>(params);
        String canonicalQS = canonicalize(sortedParamMap);
        String toSign
                = REQUEST_METHOD + "\n"
                + endpoint + "\n"
                + REQUEST_URI + "\n"
                + canonicalQS;

        String hmac = hmac(toSign);
        String sig = percentEncodeRfc3986(hmac);
        String url = "https://" + endpoint + REQUEST_URI + "?"
                + canonicalQS + "&Signature=" + sig;

        return url;
    }

    private String hmac(String stringToSign) {
        String signature = null;
        byte[] data;
        byte[] rawHmac;
        try {
            data = stringToSign.getBytes(UTF8_CHARSET);
            rawHmac = mac.doFinal(data);
            Base64 encoder = new Base64();
            signature = new String(encoder.encode(rawHmac));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(UTF8_CHARSET + " is unsupported!", e);
        }
        return signature;
    }

    private String timestamp() {
        String timestamp = null;
        Calendar cal = Calendar.getInstance();
        DateFormat dfm = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        dfm.setTimeZone(TimeZone.getTimeZone("GMT"));
        timestamp = dfm.format(cal.getTime());
        return timestamp;
    }

    private String canonicalize(SortedMap<String, String> sortedParamMap) {
        if (sortedParamMap.isEmpty()) {
            return "";
        }

        StringBuffer buffer = new StringBuffer();
        Iterator<Map.Entry<String, String>> iter
                = sortedParamMap.entrySet().iterator();

        while (iter.hasNext()) {
            Map.Entry<String, String> kvpair = iter.next();
            buffer.append(percentEncodeRfc3986(kvpair.getKey()));
            buffer.append("=");
            buffer.append(percentEncodeRfc3986(kvpair.getValue()));
            if (iter.hasNext()) {
                buffer.append("&");
            }
        }
        String canonical = buffer.toString();
        return canonical;
    }

    private String percentEncodeRfc3986(String s) {
        String out;
        try {
            out = URLEncoder.encode(s, UTF8_CHARSET)
                    .replace("+", "%20")
                    .replace("*", "%2A")
                    .replace("%7E", "~");
        } catch (UnsupportedEncodingException e) {
            out = s;
        }
        return out;
    }

    public static String createAfterTimestamp() {
        String timestamp = null;
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -14);
        DateFormat dfm = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        dfm.setTimeZone(TimeZone.getTimeZone("GMT"));
        timestamp = dfm.format(cal.getTime());
        return timestamp;
    }

    public static void main(String[] args) {
        try {
            SignedRequestsHelper signer = new SignedRequestsHelper();

            HashMap<String, String> valuePairs = new HashMap<>();
            valuePairs.put("Action", "ListOrders");
            valuePairs.put("SellerId", AKC_Creds.MERCHANT_ID);
            valuePairs.put("SignatureMethod", "HmacSHA256");
            valuePairs.put("SignatureVersion", "2");
            valuePairs.put("Version", "2013-09-01");
            valuePairs.put("CreatedAfter", createAfterTimestamp());
            valuePairs.put("OrderStatus.Status.1", "Unshipped");
            valuePairs.put("OrderStatus.Status.2", "PartiallyShipped");
            valuePairs.put("FulfillmentChannel.Channel.1", "MFN");
            valuePairs.put("MarketplaceId.Id.1", AKC_Creds.MARKETPLACE_ID);

            String signedURL = signer.sign(valuePairs);
            Connect getOrder = new Connect();
            JSONObject jsonOrders = getOrder.sendGet(signedURL).getJSONObject("ListOrdersResponse").getJSONObject("ListOrdersResult").getJSONObject("Orders");
            JSONArray orderArr = jsonOrders.getJSONArray("Order");

            for (int i = 0; i < orderArr.length(); i++) {
                System.out.println(i + ": " + orderArr.get(i));
            }
        } catch (Exception ex) {
            System.out.println("Error occured in main");
            System.out.println(ex.getMessage());
            System.out.println("End of error messages");
        }

    }

}
