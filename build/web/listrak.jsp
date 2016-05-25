<%-- 
    Document   : listrak
    Created on : Jul 16, 2015, 3:01:07 PM
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
        <title>Everest Data Export For Listrak</title>
        <link href="./bootstrap/css/bootstrap.min.css" rel="stylesheet">
        <link rel="stylesheet" href="./jquery/jquery-ui.css">
        <script src="./jquery/external/jquery/jquery.js"></script>
        <script src="./jquery/jquery-ui.js"></script>
        <link rel="stylesheet" href="./style.css">
        <script>
            $(function () {
                $("#datepicker").datepicker({
                    defaultDate: "+1w",
                    changeMonth: true,
                    numberOfMonths: 1,
                    minDate: '0',
                    minDate: '-29d',
                            maxDate: '-0m',
                });
            });

        </script>
    </head>
    <body>
        <div>
            <h2>Everest Data Export To Listrak</h2>
            <p>This module provide phone and walk-in customers data for Listrak in accordance with specifications mentioned in the <a href="./Customer Purchase Metrics - Integration Guide.docx">integration guide.</a></p>
            <form action="" method="POST" class="form-inline">
                <input name="action" type="hidden" value="get report"></input>
                <p><a href="./" class="btn btn-info" role="button"><span class="glyphicon glyphicon-chevron-left"></span> Back</a> | <a href="./ListrakModuleSettings" class="btn btn-info" role="button"><span class="glyphicon glyphicon-cog"></span> Settings</a> ${settingsMessage}</p>
                <div class="center-block input-append date" id="dp3" data-date="12-02-2012" data-date-format="dd-mm-yyyy">
                    <p>Report name: 
                        <select name="reportName">
                            <option value="customers">customers</option>
                            <option value="orders">orders</option>
                            <option value="orderitems">orderitems</option>
                        </select>
                        from date: 
                        <input name="startDate" type="text" id="datepicker" value=${startDate} required>
                        
                        <button class="btn btn-primary btn-xs" type="submit">Get Report</button>
                        ${downloadLink}
                    </p>
                </div>
                <table class="table table-striped">
                    <tr>
                    <tr>
                        ${userForm}
                    </tr>
                    </tr>
                </table>
            </form>

        </div>
    </body>
</html>
