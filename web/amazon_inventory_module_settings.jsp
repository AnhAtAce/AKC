<%-- 
    Document   : amazon_inventory_module_settings
    Created on : May 17, 2016, 2:55:16 PM
    Author     : AN2
--%>
<!--
    Author: Nguyen, Anh Tuan    zzz159@yahoo.com
-->
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Amazon Seller Central Inventory Updater</title>
        <link href="./bootstrap/css/bootstrap.min.css" rel="stylesheet">
        <link rel="stylesheet" href="./jquery/jquery-ui.css">
        <link rel="stylesheet" href="./style.css">
        <link rel="stylesheet" href="./css/bootstrap-switch.css">
        <script src="./jquery/external/jquery/jquery.js"></script>
        <script src="./jquery/jquery-ui.js"></script>
        <script src="./js/highlight.js"></script>
        <script src="./js/bootstrap-switch.js"></script>
        <script src="./js/main.js"></script>

    </head>
    <body>
        <div>
            <h2>Amazon Seller Central Inventory Updater</h2>
            <p>This module updates Amazon Seller Central the order status and inventory data in accordance with specifications mentioned in their <a target="_blank" href="https://developer.amazonservices.com/">Amazon Marketplace Web Service</a> documentations.</p>
            <form action="" method="POST" class="form-inline">
                <p><a ref="" class="btn btn-warning" role="button"><i class="glyphicon glyphicon-flag"></i> Under construction <i class="glyphicon glyphicon-flag"></i></a></p>
                <p><a href="./AMZOrders" class="btn btn-info" role="button"><span class="glyphicon glyphicon-chevron-left"></span> Back</a></p>
                <table class="table table-bordered">
                    <tr>
                        <td>
                            <p>
                                Auto update <input name="onoff" id="switch-size" autocomplete="off" type="checkbox" ${onoff} data-size="mini">
                            </p>
                            <p>Update times (Hold the "Ctrl" key to select multiple): <br/> 
                            <select name="time" class="form-control" multiple="multiple" size="24">
                                <option value="12:00 AM" ${scheduledTime[0]}>12:00 AM</option>                                
                                <option value="1:00 AM" ${scheduledTime[1]}>1:00 AM</option>
                                <option value="2:00 AM" ${scheduledTime[2]}>2:00 AM</option>
                                <option value="3:00 AM" ${scheduledTime[3]}>3:00 AM</option>
                                <option value="4:00 AM" ${scheduledTime[4]}>4:00 AM</option>
                                <option value="5:00 AM" ${scheduledTime[5]}>5:00 AM</option>
                                <option value="6:00 AM" ${scheduledTime[6]}>6:00 AM</option>
                                <option value="7:00 AM" ${scheduledTime[7]}>7:00 AM</option>
                                <option value="8:00 AM" ${scheduledTime[8]}>8:00 AM</option>
                                <option value="9:00 AM" ${scheduledTime[9]}>9:00 AM</option>
                                <option value="10:00 AM" ${scheduledTime[10]}>10:00 AM</option>
                                <option value="11:00 AM" ${scheduledTime[11]}>11:00 AM</option>
                                <option value="12:00 PM" ${scheduledTime[12]}>12:00 PM</option>
                                <option value="1:00 PM" ${scheduledTime[13]}>1:00 PM</option>
                                <option value="2:00 PM" ${scheduledTime[14]}>2:00 PM</option>
                                <option value="3:00 PM" ${scheduledTime[15]}>3:00 PM</option>
                                <option value="4:00 PM" ${scheduledTime[16]}>4:00 PM</option>
                                <option value="5:00 PM" ${scheduledTime[17]}>5:00 PM</option>
                                <option value="6:00 PM" ${scheduledTime[18]}>6:00 PM</option>
                                <option value="7:00 PM" ${scheduledTime[19]}>7:00 PM</option>
                                <option value="8:00 PM" ${scheduledTime[20]}>8:00 PM</option>
                                <option value="9:00 PM" ${scheduledTime[21]}>9:00 PM</option>
                                <option value="10:00 PM" ${scheduledTime[22]}>10:00 PM</option>
                                <option value="11:00 PM" ${scheduledTime[23]}>11:00 PM</option>
                            </select>
                            <p>${settingsDisplay}</p>
                        </td>
                        <td>
                            <p>Send notifications on update events to these email(s):</p>
                            <textarea placeholder="One email per line..." name="emails" rows="4" cols="30">${emails}</textarea>
                            <br><br><br>
                            <p>Inventory buffer:</p>
                            <input name="inventory_buffer" type="number" min="0" max="10" step="1" value="${inventory_buffer}" size="6">
                            <br><br><br>
                            <p>Non-Inventoried SKUs & Notes in items:</p>
                            <textarea placeholder="One per line..." name="nonInventoriedSKUs" rows="4" cols="30">${nonInventoriedSKUs}</textarea>
                        </td>
                    </tr>
                    <tr>
                        <td colspan="2">
                            <input name="action" value="save" type="hidden">
                            <p><button type="submit" class="btn btn-primary"><span class="glyphicon glyphicon-floppy-disk"></span> Save</button></p>
                        </td>
                    </tr>
                </table>
            </form>
        </div>
    </body>
</html>
