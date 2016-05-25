<%-- 
    Document   : AMZOrders
    Created on : Jul 25, 2015, 4:40:36 PM
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
        <title>Amazon Seller Central Updater</title>
        <link href="./bootstrap/css/bootstrap.min.css" rel="stylesheet">
        <link rel="stylesheet" href="./style.css">
    </head>
    <body>
        <h2>Amazon Seller Central Updater</h2>
        <p>This module updates Amazon Seller Central the order status and inventory data in accordance with specifications mentioned in their <a target="_blank" href="https://developer.amazonservices.com/">Amazon Marketplace Web Service</a> documentations.</p>
        <p><a href="./" class="btn btn-info" role="button"><span class="glyphicon glyphicon-chevron-left"></span> Back</a> | 
            <a href="./AmazonModuleSettings" class="btn btn-info" role="button"><span class="glyphicon glyphicon-cog"></span> Shipping Confirm Settings</a> | 
            <a href="./AmazonInventoryModuleSettings" class="btn btn-info" role="button"><span class="glyphicon glyphicon-cog"></span> Inventory Update Settings</a>
        </p>
        <h4>Amazon Unshipped Orders</h4>
        <p>${contents}</p>
    </body>
</html>
