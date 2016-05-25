package Utility;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import AMZ.AKC_Creds;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

/**
 *
 * @author Anh-Tuan
 */
public class DBController {

    private static final String connectionUrl = "jdbc:jtds:sqlserver://AKC-DATABASE:1433;databaseName=EVEREST_AKC;user="+AKC_Creds.EVEREST_DB_LOGIN+";password="+AKC_Creds.EVEREST_PWD;
    private static Connection connection;

    DBController() {
        //Constructor
    }

    public static boolean createConnection() {
        try {
            if (connection == null) {
                connection = DriverManager.getConnection(connectionUrl);
            }
            return true;
        } catch (SQLException e) {
            System.out.println("<<<<<" + e.getMessage());
            for (Throwable t : e) {
                System.out.println(t.getMessage());
            }
            return false;
        }
    }

    public static ResultSet getCustomers(String date) {
        ResultSet resultSet = null;
        try {
            PreparedStatement ps = connection.prepareStatement("SELECT LTRIM(RTRIM(ADDRESS.EMAIL)) AS [Email], ADDRESS.FIRST_NAME AS [First Name], ADDRESS.LAST_NAME AS [Last Name], '' AS [Gender], '' AS [Birthday], ADDRESS.ZIP AS [Zip Code], ADDRESS.CUST_CODE AS [CustomerNumber], '' AS [Meta1], '' AS [Meta2], '' AS [Meta3], '' AS [Meta4], '' AS [Meta5] "
                    + "FROM INVOICES INNER JOIN CUST ON (INVOICES.CUST_CODE = CUST.CUST_CODE) INNER JOIN ADDRESS ON (CUST.BILLCODE = ADDRESS.ADDR_CODE) INNER JOIN PERSONAL ON (INVOICES.SALES_REP = PERSONAL.IDNO) "
                    + "WHERE LEN(ADDRESS.EMAIL) > 0 AND INVOICES.STATUS = 8 AND INVOICES.ORDER_DATE >= ? AND PERSONAL.PFIRST NOT LIKE 'SUPERVISOR' AND INVOICES.PAID_SOFAR > 0 AND INVOICES.PAID = 'T' "
                    + "ORDER BY INVOICES.ORDER_DATE DESC");
            ps.setString(1, date);
            System.out.println(date);
            resultSet = ps.executeQuery();
        } catch (SQLException e) {
            for (Throwable t : e) {
                System.out.println(t.getMessage());
            }
        } catch (RuntimeException ex) {
            System.out.println(ex.getMessage());
        }
        return resultSet;
    }

    public static ResultSet getOrders(String date) {
        ResultSet resultSet = null;
        try {
            PreparedStatement ps = connection.prepareStatement("SELECT LTRIM(RTRIM(ADDRESS.EMAIL)) AS [Email], INVOICES.ORDER_NO AS [OrderNumber], LTRIM(STR(MONTH(INVOICES.ORDER_DATE)))+'/'+LTRIM(STR(DAY(INVOICES.ORDER_DATE)))+'/'+STR(YEAR(INVOICES.ORDER_DATE),4) + ' 00:00:00.000' AS [DateEntered], CAST(ROUND(INVOICES.PAID_SOFAR,2) AS DECIMAL(16,2)) AS [OrderTotal], '' AS [ItemTotal], CAST(ROUND(INVOICES.TAX_AMOUNT,2) AS DECIMAL(16,2)) AS [TaxTotal], '' AS [ShippingTotal], '' AS [HandlingTotal],  CASE   WHEN INVOICES.SHIPPED = 'T'    THEN 7    ELSE 6  END AS [Status],  CASE   WHEN INVOICES.SHIPPED = 'T'    THEN LTRIM(STR(MONTH(INVOICES.OP_ACTUAL_CLOSE_DATE)))+'/'+LTRIM(STR(DAY(INVOICES.OP_ACTUAL_CLOSE_DATE)))+'/'+STR(YEAR(INVOICES.OP_ACTUAL_CLOSE_DATE),4) + ' 00:00:00.000'    ELSE ''  END AS [ShipDate], INVOICES.TRACKING AS [TrackingNumber], INVOICES.DELIV_METH AS [ShippingMethod], '' AS [CouponCode], '' AS [DiscountTotal], '' AS [Meta1], '' AS [Meta2], '' AS [Meta3], '' AS [Meta4], '' AS [Meta5] "
                    + "FROM INVOICES INNER JOIN CUST ON (INVOICES.CUST_CODE = CUST.CUST_CODE) INNER JOIN ADDRESS ON (CUST.BILLCODE = ADDRESS.ADDR_CODE) INNER JOIN PERSONAL ON (INVOICES.SALES_REP = PERSONAL.IDNO) "
                    + "WHERE LEN(ADDRESS.EMAIL) > 0 AND INVOICES.STATUS = 8 AND INVOICES.ORDER_DATE >= ? AND PERSONAL.PFIRST NOT LIKE 'SUPERVISOR' AND INVOICES.PAID_SOFAR > 0 AND INVOICES.PAID = 'T' "
                    + "ORDER BY INVOICES.ORDER_DATE DESC");
            ps.setString(1, date);

            System.out.println(date);

            resultSet = ps.executeQuery();

        } catch (SQLException e) {
            for (Throwable t : e) {
                System.out.println(t.getMessage());
            }
        }
        return resultSet;
    }

    public static ResultSet getOrderItems(String date) {
        ResultSet resultSet = null;
        try {
            PreparedStatement ps = connection.prepareStatement("SELECT INVOICES.ORDER_NO AS [OrderNumber], X_INVOIC.ITEM_CODE AS [Sku], CAST(X_INVOIC.ITEM_QTY AS int) AS [Quanity], CAST(ROUND(X_INVOIC.ITEM_PRICE,2) AS DECIMAL(16,2)) AS [Price],  CASE WHEN X_INVOIC.SHIPPED = 'T'   THEN 7   ELSE 6  END AS [Status],  CASE   WHEN X_INVOIC.SHIPPED = 'T'    THEN LTRIM(STR(MONTH(INVOICES.OP_ACTUAL_CLOSE_DATE)))+'/'+LTRIM(STR(DAY(INVOICES.OP_ACTUAL_CLOSE_DATE)))+'/'+STR(YEAR(INVOICES.OP_ACTUAL_CLOSE_DATE),4) + ' 00:00:00.000'    ELSE ''  END AS [ShipDate], INVOICES.TRACKING AS [TrackingNumber], INVOICES.DELIV_METH AS [ShippingMethod], CAST(ROUND(X_INVOIC.DISCOUNT_V, 2) AS DECIMAL(16,2)) AS [DiscountPrice], '' AS [Meta1], '' AS [Meta2], '' AS [Meta3], '' AS [Meta4], '' AS [Meta5] "
                    + "FROM INVOICES INNER JOIN X_INVOIC ON (INVOICES.ORDER_NO = X_INVOIC.ORDER_NO) INNER JOIN CUST ON (INVOICES.CUST_CODE = CUST.CUST_CODE) INNER JOIN ADDRESS ON (CUST.BILLCODE = ADDRESS.ADDR_CODE) INNER JOIN ITEMS ON (X_INVOIC.ITEM_CODE = ITEMS.ITEMNO) INNER JOIN PERSONAL ON (INVOICES.SALES_REP = PERSONAL.IDNO) "
                    + "WHERE LEN(ADDRESS.EMAIL) > 0 AND X_INVOIC.STATUS = 8 AND INVOICES.STATUS = 8 AND INVOICES.ORDER_DATE >= ? AND   PERSONAL.PFIRST NOT LIKE 'SUPERVISOR' AND INVOICES.PAID_SOFAR > 0 AND INVOICES.PAID = 'T' AND X_INVOIC.ITEM_CODE != 'PICK' "
                    + "ORDER BY INVOICES.ORDER_NO DESC, X_INVOIC.[SEQUENCE] ASC");
            ps.setString(1, date);
            System.out.println(date);
            resultSet = ps.executeQuery();
        } catch (SQLException e) {
            for (Throwable t : e) {
                System.out.println(t.getMessage());
            }
        }
        return resultSet;
    }

    public static ResultSet getShippedYOrders(String date) {
        ResultSet resultSet = null;
        try {
            PreparedStatement ps = connection.prepareStatement("SELECT INVOICES.PO AS [Yahoo Order Number], INVOICES.SHIPPED AS [Shipped], INVOICES.TRACKING AS [Tracking] "
                    + "FROM INVOICES INNER JOIN CUST ON (INVOICES.CUST_CODE = CUST.CUST_CODE) INNER JOIN ADDRESS ON (CUST.BILLCODE = ADDRESS.ADDR_CODE) INNER JOIN PERSONAL ON (INVOICES.SALES_REP = PERSONAL.IDNO) "
                    + "WHERE INVOICES.STATUS = '8' AND INVOICES.PO IN "
                    + "("
                    + "SELECT INVOICES.PO "
                    + "FROM INVOICES INNER JOIN CUST ON (INVOICES.CUST_CODE = CUST.CUST_CODE) INNER JOIN ADDRESS ON (CUST.BILLCODE = ADDRESS.ADDR_CODE) INNER JOIN PERSONAL ON (INVOICES.SALES_REP = PERSONAL.IDNO) "
                    + "WHERE INVOICES.STATUS = '9' AND INVOICES.ORDER_DATE >= ? AND PERSONAL.PFIRST = 'SUPERVISOR' AND INVOICES.PO IS NOT NULL AND INVOICES.PO <> '' "
                    + ") "
                    + "ORDER BY INVOICES.PO");
            ps.setString(1, date);
            System.out.println(date);
            resultSet = ps.executeQuery();
        } catch (SQLException e) {
            for (Throwable t : e) {
                System.out.println(t.getMessage());
            }
        }
        return resultSet;
    }
    
    public static ResultSet getShippedAmzOrders(String date) {
        ResultSet resultSet = null;
        try {
            PreparedStatement ps = connection.prepareStatement("SELECT INVOICES.PO AS [AMZ Order Number], INVOICES.DELIV_METH AS [Shipping Method], INVOICES.SHIPPED AS [Shipped], INVOICES.TRACKING AS [Tracking] FROM INVOICES INNER JOIN CUST ON (INVOICES.CUST_CODE = CUST.CUST_CODE) INNER JOIN ADDRESS ON (CUST.BILLCODE = ADDRESS.ADDR_CODE) INNER JOIN PERSONAL ON (INVOICES.SALES_REP = PERSONAL.IDNO) WHERE INVOICES.STATUS = '8' AND INVOICES.TRACKING <> '' AND INVOICES.PO IN ( SELECT INVOICES.PO FROM INVOICES INNER JOIN CUST ON (INVOICES.CUST_CODE = CUST.CUST_CODE) INNER JOIN ADDRESS ON (CUST.BILLCODE = ADDRESS.ADDR_CODE) INNER JOIN PERSONAL ON (INVOICES.SALES_REP = PERSONAL.IDNO) WHERE INVOICES.STATUS = '9' AND INVOICES.ORDER_DATE >= ? AND INVOICES.TERMS = 'AMZ' AND INVOICES.PO IS NOT NULL AND INVOICES.PO <> '' ) ORDER BY INVOICES.PO");
            ps.setString(1, date);
            System.out.println(date);
            resultSet = ps.executeQuery();
        } catch (SQLException e) {
            for (Throwable t : e) {
                System.out.println(t.getMessage());
            }
        }
        return resultSet;
    }
    
    public static ResultSet getShippedAmzOrders(ArrayList<String> IDs) {
        ResultSet resultSet = null;
        String IDList = "'";
        for(Object e : IDs.toArray()){
            IDList += e + "   ";
        }
        IDList = IDList.trim().replace("   ", "','") + "'";
        try {
            Statement stmt = connection.createStatement();
            resultSet = stmt.executeQuery("SELECT INVOICES.PO AS [AMZ Order Number], INVOICES.DELIV_METH AS [Shipping Method], INVOICES.SHIPPED AS [Shipped], INVOICES.TRACKING AS [Tracking] "
                    + "FROM INVOICES INNER JOIN CUST ON (INVOICES.CUST_CODE = CUST.CUST_CODE) INNER JOIN ADDRESS ON (CUST.BILLCODE = ADDRESS.ADDR_CODE) INNER JOIN PERSONAL ON (INVOICES.SALES_REP = PERSONAL.IDNO) "
                    + "WHERE INVOICES.STATUS = '8' AND INVOICES.PO IN ( " + IDList + " ) "
                    + "ORDER BY INVOICES.PO");
            //System.out.println(IDList + "\n" + ps.toString());
        } catch (SQLException e) {
            for (Throwable t : e) {
                System.out.println(t.getMessage());
            }
        }
        return resultSet;
    }

    public static ResultSet getItemStockQuantity(String ItemCode) {
        ResultSet resultSet = null;
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "SELECT [dbo].[ITEMS].[ITEMNO],"
                    + "	[dbo].[X_STK_AREA].[Q_STK],"
                    + "	[dbo].[X_STK_AREA].[AREA_CODE],"
                    + " [dbo].[ITEMS].[INVENTORED],"
                    + " [MATRIX_ITEM_TYPE]"
                    + "FROM [dbo].[ITEMS] LEFT JOIN [dbo].[X_STK_AREA]"
                    + "ON [dbo].[ITEMS].[ITEMNO] = [dbo].[X_STK_AREA].[ITEM_NO]"
                    + "WHERE [dbo].[ITEMS].[ITEMNO] = ?"
            );
            ps.setString(1, ItemCode);
            resultSet = ps.executeQuery();
        } catch (SQLException e) {
            for (Throwable t : e) {
                System.out.println(t.getMessage());
            }
        }
        return resultSet;
    }

    public static ResultSet getKitItems(String ItemCode) {
        ResultSet resultSet = null;
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "SELECT [ITEM_CODE]"
                    + "FROM [dbo].[X_KIT]"
                    + "WHERE [KIT_CODE] = ?"
            );
            ps.setString(1, ItemCode);
            resultSet = ps.executeQuery();
        } catch (SQLException e) {
            for (Throwable t : e) {
                System.out.println(t.getMessage());
            }
        }
        return resultSet;
    }
}
