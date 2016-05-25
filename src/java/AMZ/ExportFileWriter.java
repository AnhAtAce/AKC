/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package AMZ;

import com.opencsv.CSVParser;
import com.opencsv.CSVWriter;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.TimeZone;
import javax.swing.JOptionPane;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author AN2
 */
public class ExportFileWriter {

    private int line = 0;
    private FileWriter fw;
    private BufferedWriter bw;
    private File file;
    private HashMap<String, JSONObject> ordersHashMap;
    private HashMap<String, String> orders;
    private HashMap<String, JSONObject> orderItemsHashMap;
    private HashMap<String, String> orderShType;
    private HashMap<String, Double> orderShPrice;
    private HashMap<String, ArrayList<String>> asinSkuHashMap;
    private ArrayList<String> asinSku = null;

    private String orderID = "";
    private CSVWriter writer;

    ExportFileWriter(HashMap<String, JSONObject> ordersHashMap, HashMap<String, JSONObject> orderItemsHashMap, String path) {
        ordersHashMap = ordersHashMap;
        orderItemsHashMap = orderItemsHashMap;
        orders = new HashMap<>();
        orderShType = new HashMap<>();
        orderShPrice = new HashMap<>();
        asinSkuHashMap = new HashMap<>();

        if (ordersHashMap != null) {
            String fileContents = "";
            String stateName = "ALABAMA,ALASKA,AMERICAN SAMOA,ARIZONA,ARKANSAS,CALIFORNIA,COLORADO,CONNECTICUT,DELAWARE,DISTRICT OF COLUMBIA,FLORIDA,FEDERATED STATES OF MICRONESIA,GEORGIA,GUAM,HAWAII,IDAHO,ILLINOIS,INDIANA,IOWA,KANSAS,KENTUCKY,LOUISIANA,MAINE,MARSHALL ISLANDS,MARYLAND,MASSACHUSETTS,MICHIGAN,MINNESOTA,MISSISSIPPI,MISSOURI,MONTANA,NEBRASKA,NEVADA,NEW HAMPSHIRE,NEW JERSEY,NEW MEXICO,NEW YORK,NORTH CAROLINA,NORTH DAKOTA,NORTHERN MARIANA ISLANDS,OHIO,OKLAHOMA,OREGON,PALAU,PENNSYLVANIA,PUERTO RICO,RHODE ISLAND,SOUTH CAROLINA,SOUTH DAKOTA,TENNESSEE,TEXAS,U.S. MINOR OUTLYING ISLANDS,UTAH,VERMONT,VIRGINIA,VIRGIN ISLANDS OF THE U.S.,WASHINGTON,WEST VIRGINIA,WISCONSIN,WYOMING";
            String stateCode = "AL,AK,AS,AZ,AR,CA,CO,CT,DE,DC,FL,FM,GA,GU,HI,ID,IL,IN,IA,KS,KY,LA,ME,MH,MD,MA,MI,MN,MS,MO,MT,NE,NV,NH,NJ,NM,NY,NC,ND,MP,OH,OK,OR,PW,PA,PR,RI,SC,SD,TN,TX,UM,UT,VT,VA,VI,WA,WV,WI,WY";
            String states_full_Array[] = stateName.split(",");
            String states_code_Array[] = stateCode.split(",");
            HashMap<String, String> statesHM = new HashMap<>();
            for (int i = 0; i < states_code_Array.length; i++) {
                statesHM.put(states_full_Array[i], states_code_Array[i]);
            }
            String countryCode[] = {"AC", "AD", "AE", "AF", "AG", "AI", "AL", "AM", "AN", "AO", "AQ", "AR", "AS", "AT", "AU", "AW", "AX", "AZ", "BA", "BB", "BE", "BD", "BF", "BG", "BH", "BI", "BJ", "BM", "BN", "BO", "BR", "BS", "BT", "BV", "BW", "BY", "BZ", "CA", "CC", "CD", "CF", "CG", "CH", "CI", "CK", "CL", "CM", "CN", "CO", "CR", "CU", "CV", "CX", "CY", "CZ", "DE", "DJ", "DK", "DM", "DO", "DZ", "EC", "EE", "EG", "ER", "ES", "ET", "EU", "FI", "FJ", "FK", "FM", "FO", "FR", "GA", "GB", "GD", "GE", "GF", "GG", "GH", "GI", "GL", "GM", "GN", "GP", "GQ", "GR", "GS", "GT", "GU", "GW", "GY", "HK", "HM", "HN", "HR", "HT", "HU", "ID", "IE", "IL", "IM", "IN", "IO", "IQ", "IR", "IS", "IT", "JE", "JM", "JO", "JP", "KE", "KG", "KH", "KI", "KM", "KN", "KR", "KW", "KY", "KZ", "LA", "LB", "LC", "LI", "LK", "LR", "LS", "LT", "LU", "LV", "LY", "MA", "MC", "MD", "ME", "MG", "MH", "MK", "ML", "MM", "MN", "MO", "MP", "MQ", "MR", "MS", "MT", "MU", "MV", "MW", "MX", "MY", "MZ", "NA", "NC", "NE", "NF", "NG", "NI", "NL", "NO", "NP", "NR", "NU", "NZ", "OM", "PA", "PE", "PF", "PG", "PH", "PK", "PL", "PM", "PN", "PR", "PS", "PT", "PW", "PY", "QA", "RE", "RO", "RS", "RU", "RW", "SA", "UK", "SB", "SC", "SD", "SE", "SG", "SH", "SI", "SJ", "SK", "SL", "SM", "SN", "SO", "SR", "ST", "SU", "SV", "SY", "SZ", "TC", "TD", "TF", "TG", "TH", "TJ", "TK", "TI", "TM", "TN", "TO", "TP", "TR", "TT", "TV", "TW", "TZ", "UA", "UG", "UK", "UM", "US", "UY", "UZ", "VA", "VC", "VE", "VG", "VI", "VN", "VU", "WF", "WS", "YE", "YT", "ZA", "ZM", "ZW"};
            String countryName[] = {"ASCENSION ISLAND", "ANDORRA", "UNITED ARAB EMIRATES", "AFGHANISTAN", "ANTIGUA AND BARBUDA", "ANGUILLA", "ALBANIA", "ARMENIA", "NETHERLANDS ANTILLES", "ANGOLA", "ANTARCTICA", "ARGENTINA", "AMERICAN SAMOA", "AUSTRIA", "AUSTRALIA", "ARUBA", "ÅLAND", "AZERBAIJAN", "BOSNIA AND HERZEGOVINA", "BARBADOS", "BELGIUM", "BANGLADESH", "BURKINA FASO", "BULGARIA", "BAHRAIN", "BURUNDI", "BENIN", "BERMUDA", "BRUNEI DARUSSALAM", "BOLIVIA", "BRAZIL", "BAHAMAS", "BHUTAN", "BOUVET ISLAND", "BOTSWANA", "BELARUS", "BELIZE", "CANADA", "COCOS (KEELING) ISLANDS", "CONGO (DEMOCRATIC REPUBLIC)", "CENTRAL AFRICAN REPUBLIC", "CONGO (REPUBLIC)", "SWITZERLAND", "COTE D’IVOIRE", "COOK ISLANDS", "CHILE", "CAMEROON", "PEOPLE’S REPUBLIC OF CHINA", "COLOMBIA", "COSTA RICA", "CUBA", "CAPE VERDE", "CHRISTMAS ISLAND", "CYPRUS", "CZECH REPUBLIC", "GERMANY", "DJIBOUTI", "DENMARK", "DOMINICA", "DOMINICAN REPUBLIC", "ALGERIA", "ECUADOR", "ESTONIA", "EGYPT", "ERITREA", "SPAIN", "ETHIOPIA", "EUROPEAN UNION", "FINLAND", "FIJI", "FALKLAND ISLANDS (MALVINAS)", "MICRONESIA, FEDERATED STATES OF", "FAROE ISLANDS", "FRANCE", "GABON", "UNITED KINGDOM (NO NEW REGISTRATIONS, SEE ALSO UK)", "GRENADA", "GEORGIA", "FRENCH GUIANA", "GUERNSEY", "GHANA", "GIBRALTAR", "GREENLAND", "GAMBIA", "GUINEA", "GUADELOUPE", "EQUATORIAL GUINEA", "GREECE", "SOUTH GEORGIA AND THE SOUTH SANDWICH ISLANDS", "GUATEMALA", "GUAM", "GUINEA-BISSAU", "GUYANA", "HONG KONG", "HEARD AND MC DONALD ISLANDS", "HONDURAS", "CROATIA (LOCAL NAME: HRVATSKA)", "HAITI", "HUNGARY", "INDONESIA", "IRELAND", "ISRAEL", "ISLE OF MAN", "INDIA", "BRITISH INDIAN OCEAN TERRITORY", "IRAQ", "IRAN (ISLAMIC REPUBLIC OF)", "ICELAND", "ITALY", "JERSEY", "JAMAICA", "JORDAN", "JAPAN", "KENYA", "KYRGYZSTAN", "CAMBODIA", "KIRIBATI", "COMOROS", "SAINT KITTS AND NEVIS", "KOREA, REPUBLIC OF", "KUWAIT", "CAYMAN ISLANDS", "KAZAKHSTAN", "LAO PEOPLE’S DEMOCRATIC REPUBLIC", "LEBANON", "SAINT LUCIA", "LIECHTENSTEIN", "SRI LANKA", "LIBERIA", "LESOTHO", "LITHUANIA", "LUXEMBOURG", "LATVIA", "LIBYAN ARAB JAMAHIRIYA", "MOROCCO", "MONACO", "MOLDOVA, REPUBLIC OF", "MONTENEGRO", "MADAGASCAR", "MARSHALL ISLANDS", "MACEDONIA, THE FORMER YUGOSLAV REPUBLIC OF", "MALI", "MYANMAR", "MONGOLIA", "MACAU", "NORTHERN MARIANA ISLANDS", "MARTINIQUE", "MAURITANIA", "MONTSERRAT", "MALTA", "MAURITIUS", "MALDIVES", "MALAWI", "MEXICO", "MALAYSIA", "MOZAMBIQUE", "NAMIBIA", "NEW CALEDONIA", "NIGER", "NORFOLK ISLAND", "NIGERIA", "NICARAGUA", "NETHERLANDS", "NORWAY", "NEPAL", "NAURU", "NIUE", "NEW ZEALAND", "OMAN", "PANAMA", "PERU", "FRENCH POLYNESIA", "PAPUA NEW GUINEA", "PHILIPPINES, REPUBLIC OF THE", "PAKISTAN", "POLAND", "ST. PIERRE AND MIQUELON", "PITCAIRN", "PUERTO RICO", "PALESTINE", "PORTUGAL", "PALAU", "PARAGUAY", "QATAR", "REUNION", "ROMANIA", "SERBIA", "RUSSIAN FEDERATION", "RWANDA", "SAUDI ARABIA", "SCOTLAND", "SOLOMON ISLANDS", "SEYCHELLES", "SUDAN", "SWEDEN", "SINGAPORE", "ST. HELENA", "SLOVENIA", "SVALBARD AND JAN MAYEN ISLANDS", "SLOVAKIA (SLOVAK REPUBLIC)", "SIERRA LEONE", "SAN MARINO", "SENEGAL", "SOMALIA", "SURINAME", "SAO TOME AND PRINCIPE", "SOVIET UNION", "EL SALVADOR", "SYRIAN ARAB REPUBLIC", "SWAZILAND", "TURKS AND CAICOS ISLANDS", "CHAD", "FRENCH SOUTHERN TERRITORIES", "TOGO", "THAILAND", "TAJIKISTAN", "TOKELAU", "EAST TIMOR (NEW CODE)", "TURKMENISTAN", "TUNISIA", "TONGA", "EAST TIMOR (OLD CODE)", "TURKEY", "TRINIDAD AND TOBAGO", "TUVALU", "TAIWAN", "TANZANIA, UNITED REPUBLIC OF", "UKRAINE", "UGANDA", "UNITED KINGDOM", "UNITED STATES MINOR OUTLYING ISLANDS", "UNITED STATES", "URUGUAY", "UZBEKISTAN", "VATICAN CITY STATE (HOLY SEE)", "SAINT VINCENT AND THE GRENADINES", "VENEZUELA", "VIRGIN ISLANDS (BRITISH)", "VIRGIN ISLANDS (U.S.)", "VIET NAM", "VANUATU", "WALLIS AND FUTUNA ISLANDS", "SAMOA", "YEMEN", "MAYOTTE", "SOUTH AFRICA", "ZAMBIA", "ZIMBABWE"};
            HashMap<String, String> countriesHM = new HashMap<>();
            for (int i = 0; i < countryName.length; i++) {
                countriesHM.put(countryCode[i], countryName[i]);
            }
            SimpleDateFormat in = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            SimpleDateFormat out = new SimpleDateFormat("MM/dd/yyyy HH:mm");

            JSONObject JSONOrder;
            for (String key : ordersHashMap.keySet()) {
                JSONOrder = ordersHashMap.get(key);
                /*
                System.out.println("<><><><><><><><><><><><><><><><><><><><><><><><>");
                System.out.println(JSONOrder.toString(2));
                System.out.println("<><><><><><><><><><><><><><><><><><><><><><><><>");
                */
                String sh = "", fName = "", lName = "", fullName = JSONOrder.getJSONObject("ShippingAddress").getString("Name").replace(",", ".").trim();
                if (fullName.contains(" ")) {
                    //split name here
                    int space = fullName.length() - fullName.replace(" ", "").length();
                    String fn[] = fullName.split(" ");
                    if (space == 1) {
                        fName = fn[0];
                        lName = fn[1];
                    } else {//two or more space means tree or more parts to the name field
                        int odd = space % 2, half = space / 2;
                        if (odd == 0)//odd name parts
                        {
                            fName = "";
                            for (int i = 0; i < half; i++) {
                                fName += fn[i] + " ";
                            }
                            fName = fName.trim();

                            lName = "";
                            for (int i = half; i < (half * 2 + 1); i++) {
                                lName += fn[i] + " ";
                            }
                            lName = lName.trim();
                        } else//even name parts
                        {
                            fName = "";
                            for (int i = 0; i < half * 2; i++) {
                                fName += fn[i] + " ";
                            }
                            fName = fName.trim();

                            lName = "";
                            for (int i = half * 2; i < space + 1; i++) {
                                lName += fn[i] + " ";
                            }
                            lName = lName.trim();
                        }
                    }
                } else {
                    fName = fullName;
                    lName = fullName;
                }

                String latestDeliveryDate = "", latestShipDate = "", add1 = "", add2 = "", city = "", state = "", zip = "", country = "", phone = "", amount = "", purchaseDate = "";
                try {
                    add1 = JSONOrder.getJSONObject("ShippingAddress").getString("AddressLine1");
                    add1 = add1.replace(",", ".");
                    try {
                        add2 = JSONOrder.getJSONObject("ShippingAddress").getString("AddressLine2");
                        add2 = add2.replace(",", ".");
                    } catch (JSONException ex) {
                    }
                } catch (JSONException ex) {
                    add1 = JSONOrder.getJSONObject("ShippingAddress").getString("AddressLine2");
                    add1 = add1.replace(",", ".");
                }
                try {
                    city = JSONOrder.getJSONObject("ShippingAddress").getString("City");
                    city = city.replace(",", ".");
                } catch (JSONException ex) {
                    city = "";
                }
                try {
                    state = JSONOrder.getJSONObject("ShippingAddress").getString("StateOrRegion");
                    state = state.replace(",", ".").toUpperCase();
                    state = (statesHM.get(state) != null) ? statesHM.get(state) : state;
                } catch (JSONException ex) {
                    state = "";
                }
                try {
                    country = JSONOrder.getJSONObject("ShippingAddress").getString("CountryCode");
                    country = country.replace(",", ".").toUpperCase();
                    country = (countriesHM.get(country) != null) ? countriesHM.get(country) : country;
                } catch (JSONException ex) {
                    country = "";
                }
                try {
                    zip = JSONOrder.getJSONObject("ShippingAddress").get("PostalCode").toString();
                    zip = zip.replace(",", ".");
                    zip = (zip.contains("-")) ? zip.split("-")[0] : zip;
                } catch (JSONException ex) {
                    zip = "";
                }
                try {
                    phone = JSONOrder.getJSONObject("ShippingAddress").get("Phone").toString();
                    phone = phone.replace(",", ".").replace("-", "").trim();
                    String newPhone = "";
                    for (char c : phone.toCharArray()) {
                        try {
                            Integer.parseInt(c + "");
                            newPhone += c;
                        } catch (NumberFormatException nfe) {
                        }
                    }
                    if ((newPhone.length() == 11 && newPhone.subSequence(0, 1) == "1")) {
                        phone = newPhone.substring(1, newPhone.length());
                    } else if (newPhone.length() == 10) {
                        phone = newPhone;
                    }
                } catch (JSONException ex) {
                    phone = "";
                }
                try {
                    amount = JSONOrder.getJSONObject("OrderTotal").get("Amount").toString();
                    amount = amount.replace(",", ".");
                } catch (JSONException ex) {
                    phone = "";
                }
                try {
                    purchaseDate = JSONOrder.getString("PurchaseDate");
                    try {
                        Date date = in.parse(purchaseDate);
                        purchaseDate = out.format(date);
                    } catch (Exception ex) {
                        System.out.println("Date parse exception thrown");
                    }
                } catch (JSONException ex) {
                    purchaseDate = "";
                }
                try {
                    purchaseDate = JSONOrder.getString("PurchaseDate");
                    try {
                        Date date = in.parse(purchaseDate);
                        purchaseDate = out.format(date);
                    } catch (Exception ex) {
                        System.out.println("Date parse exception thrown");
                    }
                } catch (JSONException ex) {
                    purchaseDate = "";
                }
                try {
                    sh = JSONOrder.get("ShipmentServiceLevelCategory").toString();
                    orderShType.put(key, sh);
                } catch (JSONException ex) {
                    sh = "";
                }
                try {
                    latestDeliveryDate = JSONOrder.get("LatestDeliveryDate").toString();
                    try {
                        Date date = in.parse(latestDeliveryDate);
                        GregorianCalendar gCal = new GregorianCalendar();
                        gCal.setTime(date);
                        System.out.println(out.format(gCal.getTime())+" UTC");
                        gCal.setTimeZone(TimeZone.getTimeZone("PST"));
                        //adjust time offset from UTC to PST
                        gCal.add(Calendar.HOUR_OF_DAY, -7);
                        System.out.println(out.format(gCal.getTime())+" PST");
                        latestDeliveryDate = out.format(gCal.getTime());

                    } catch (Exception ex) {
                        System.out.println("Date parse exception thrown");
                    }
                } catch (JSONException ex) {
                    latestDeliveryDate = "";
                }
                try {
                    latestShipDate = JSONOrder.get("LatestShipDate").toString();
                    try {
                        Date date = in.parse(latestShipDate);
                        GregorianCalendar gCal = new GregorianCalendar();
                        gCal.setTime(date);
                        System.out.println(out.format(gCal.getTime())+" UTC");
                        gCal.setTimeZone(TimeZone.getTimeZone("PST"));
                        //adjust time offset from UTC to PST
                        gCal.add(Calendar.HOUR_OF_DAY, -7);
                        System.out.println(out.format(gCal.getTime())+" PST");

                        latestShipDate = out.format(gCal.getTime());
                    } catch (Exception ex) {
                        System.out.println("Date parse exception thrown");
                    }
                } catch (JSONException ex) {
                    latestShipDate = "";
                }
                fileContents
                        = JSONOrder.getString("AmazonOrderId") + ","
                        + purchaseDate + ",,"
                        + fName + "," + lName + ","
                        + add1 + "," + add2 + ","
                        + city + ","
                        + state + ","
                        + country + ","
                        + zip + ","
                        + phone + ","
                        + fName + "," + lName + ","
                        + add1 + "," + add2 + ","
                        + city + ","
                        + state + ","
                        + country + ","
                        + zip + ","
                        + phone + ",,not_supplied,unknown,ShipmentServiceLevelCategory,AMAZON,,,,"
                        + amount + ",NONE,,,,AMZ Transaction ID: " + key 
                        + " - Customer is protected by Amazon's 90 days A-Z guarantee. [Latest ship date: " 
                        + latestShipDate + "] [Latest delivery date: " + latestDeliveryDate + "]";
                
                orders.put(key, fileContents);
            }
        }

        if (orderItemsHashMap != null) {
            file = new File(path + "\\items.csv");
            file.delete();
            String pickItemStringForm = "{  \"ItemTax\": {    \"CurrencyCode\": \"USD\",    \"Amount\": \"0.00\"  },  \"ConditionNote\": \"\",  \"QuantityShipped\": 0,  \"ItemPrice\": {    \"CurrencyCode\": \"USD\",    \"Amount\": \"0\"  },  \"Title\": \"\",  \"ASIN\": \"\",  \"SellerSKU\": \"PICK\",  \"ShippingTax\": {    \"CurrencyCode\": \"USD\",    \"Amount\": \"0.00\"  },  \"ShippingPrice\": {    \"CurrencyCode\": \"USD\",    \"Amount\": 0  },  \"ConditionSubtypeId\": \"New\",  \"GiftWrapPrice\": {    \"CurrencyCode\": \"USD\",    \"Amount\": \"0\"  },  \"ShippingDiscount\": {    \"CurrencyCode\": \"USD\",    \"Amount\": \"0.00\"  },  \"QuantityOrdered\": 1,  \"ConditionId\": \"New\",  \"GiftWrapTax\": {    \"CurrencyCode\": \"USD\",    \"Amount\": \"0.00\"  },  \"PromotionDiscount\": {    \"CurrencyCode\": \"USD\",    \"Amount\": \"0.00\"  },  \"OrderItemId\": 65912392256826}";
            JSONObject pickItem = new JSONObject(pickItemStringForm);
            
            //loop through all order IDs. Return an JSONObect that will have another JSONObject or JSONArray
            for (String key : orderItemsHashMap.keySet()) {
                JSONObject JSONItems = orderItemsHashMap.get(key);

                try {
                    JSONObject JSONItem = JSONItems.getJSONObject("OrderItem");
                    writeItem(JSONItem, key);
                    writeItem(pickItem, key);
                } catch (JSONException ex) {
                    System.out.println("Try catch -> JSONArray");
                    JSONArray JSONItemsArray = JSONItems.getJSONArray("OrderItem");
                    for (int i = 0; i < JSONItemsArray.length(); i++) {
                        JSONObject JSONItem = JSONItemsArray.getJSONObject(i);
                        writeItem(JSONItem, key);
                        //System.out.println(">>>>>JSONARRAY<<<<<");
                    }
                    writeItem(pickItem, key);
                }
            }
        }

        try {
            String fileContents = "", shService;
            file = new File(path + "\\Orders.csv");
            fw = new FileWriter(file.getPath());
            //bw = new BufferedWriter(fw);
            writer = new CSVWriter(fw, CSVWriter.DEFAULT_SEPARATOR, CSVWriter.DEFAULT_QUOTE_CHARACTER, CSVWriter.DEFAULT_LINE_END);

            ArrayList<String> asinSkuArrL = null;
            for (String key : orders.keySet()) {
                shService = "FREESHIP";
                if (orderShType.get(key).equalsIgnoreCase("Expedited")) {
                    shService = (orderShPrice.get(key) >= 29) ? "UPSB" : "USPSP";
                }
                fileContents = orders.get(key);
                fileContents = fileContents.replace("ShipmentServiceLevelCategory", shService);
                asinSkuArrL = asinSkuHashMap.get(key);
                if (asinSkuArrL != null) {
                    for (String item : asinSkuArrL) {
                        fileContents += item;
                    }
                }
                CSVParser sp = new CSVParser();
                writer.writeNext(sp.parseLine(fileContents));
            }

            writer.close();
            fw.close();
        } catch (IOException ex) {
            System.out.println("Error occured in ExportFileWriter constructor -> write order header file");
            System.out.println(ex.getMessage());
            System.out.println("End of error messages");
            
            JOptionPane.showMessageDialog(null, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }

        JOptionPane.showMessageDialog(null, "Export Complete.", "Done", JOptionPane.INFORMATION_MESSAGE, null);
    }

    private void writeItem(JSONObject JSONItem, String orderID) {
        System.out.println(JSONItem.toString(2));
        
        if (!this.orderID.equals(orderID)) {
            //reset item line count on new order
            this.orderID = orderID;
            line = 0;
            asinSku = new ArrayList();
            asinSkuHashMap.put(orderID, asinSku);
        }
        try {
            fw = new FileWriter(file.getAbsoluteFile(), true);
            bw = new BufferedWriter(fw);

            String qty = null, title = null, shPrice = null, itemDiscount = null, asin = null,
                    itemCode = null, itemPrice = null, shDiscount = null;
            for (String key : JSONItem.keySet()) {
                switch (key) {
                    case "ItemPrice":
                        itemPrice = JSONItem.getJSONObject(key).get("Amount").toString();
                        break;
                    case "Title":
                        title = JSONItem.get(key).toString();
                        title = title.replace(",", ".").replace("\"", "`");
                        break;
                    case "ASIN":
                        asin = JSONItem.get(key).toString();
                        break;
                    case "SellerSKU":
                        itemCode = JSONItem.get(key).toString();
                        itemCode = itemCode.replace(" PICK", "").replace(" AKC", "").replace(",", ".").replace("_", " ").replace("  ", " ");
                        String itemCodeArr[] = itemCode.split(" ");
                        itemCode = "";
                        for (String each : itemCodeArr) {
                            if (each.length() > 1) {
                                if (!each.substring(0, 2).equalsIgnoreCase("fb")) {
                                    try {
                                        Integer.parseInt(each);
                                    } catch (NumberFormatException nfe) {
                                        itemCode += each + " ";
                                    }
                                }
                            } else {
                                try {
                                    Integer.parseInt(each);
                                } catch (NumberFormatException nfe) {
                                    itemCode += each + " ";
                                }
                            }
                        }
                        itemCode = itemCode.trim();
                        break;
                    case "ShippingPrice":
                        shPrice = JSONItem.getJSONObject(key).get("Amount").toString();
                        break;
                    case "ShippingDiscount":
                        shDiscount = JSONItem.getJSONObject(key).get("Amount").toString();
                        break;
                    case "QuantityOrdered":
                        qty = JSONItem.get(key).toString();
                        break;
                    case "PromotionDiscount":
                        itemDiscount = JSONItem.getJSONObject(key).get("Amount").toString();
                        break;
                }
            }
            line++;
            String itemRowData = orderID + "," + line + ",," + itemCode + "," + qty + "," + itemPrice;

            asinSku = asinSkuHashMap.get(orderID);
            if (!itemCode.equalsIgnoreCase("pick")) {
                asinSku.add(" < " + itemCode + " = " + asin + " >");
            }
            asinSkuHashMap.put(orderID, asinSku);

            bw.write(itemRowData);
            bw.newLine();
            if (Double.parseDouble(itemDiscount.toString()) > 0) {
                line++;
                String itemDiscountRowData = orderID + "," + line + ",," + "SALEDISC" + ",1," + itemDiscount;
                bw.write(itemDiscountRowData);
                bw.newLine();
            }
            if (Double.parseDouble(shPrice.toString()) > 0) {
                line++;
                String shRowData = orderID + "," + line + ",,SH,1," + shPrice;
                bw.write(shRowData);
                bw.newLine();
                double shPriceForOrder = (orderShPrice.get(orderID) == null) ? 0 : orderShPrice.get(orderID).doubleValue();
                shPriceForOrder += Double.parseDouble(shPrice);
                orderShPrice.put(orderID, new Double(shPriceForOrder));
            }
            if (Double.parseDouble(shDiscount.toString()) > 0) {
                line++;
                String shDiscountRowData = orderID + "," + line + ",," + "SALEDISC" + ",1," + shDiscount;
                bw.write(shDiscountRowData);
                bw.newLine();
                double shPriceForOrder = (orderShPrice.get(orderID) == null) ? 0 : orderShPrice.get(orderID).doubleValue();
                shPriceForOrder -= Double.parseDouble(shDiscount);
                orderShPrice.put(orderID, new Double(shPriceForOrder));
            }

            bw.close();
            fw.close();
        } catch (IOException ex) {
            System.out.println("Error occured in ExportFileWriter -> writeItem()");
            System.out.println(ex.getMessage());
            System.out.println("End of error messages");
            
            JOptionPane.showMessageDialog(null, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
