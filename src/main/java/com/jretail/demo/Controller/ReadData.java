package com.jretail.demo.Controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

import java.sql.*;
import java.util.regex.Pattern;

@Service
public class ReadData {
    String readdata(String masternum, String periodnum, String producutid, String conurl) {
        if (!Pattern.matches("^-?[0-9]*$", producutid)) {
            return "";
        }
        String sqlcommand;
        if (producutid.equals("-1")) {
            sqlcommand = "SELECT CODE, DESCRIPTION,BARCODE, SALES_PRICE FROM " +
                    "(select A.ID,CODE,DESCRIPTION,B.BARCODE,C.SALES_PRICE,C.CREATE_DATE from JRETAIL_MASTER_" + masternum + "..MATERIAL A " +
                    "JOIN JRETAIL_MASTER_" + masternum + "..MATERIAL_BARCODE B on A.ID=B.MATERIAL_ID " +
                    "JOIN JRETAIL_PERIOD_" + masternum + "_" + periodnum + "..MATERIAL_SALES_PRICE  C ON A.ID=C.MATERIAL_ID) Y " +
                    "WHERE Y.CREATE_DATE = (SELECT MAX(CREATE_DATE) FROM JRETAIL_PERIOD_" + masternum + "_" + periodnum + "..MATERIAL_SALES_PRICE X WHERE X.MATERIAL_ID=Y.ID )";
        } else {
            sqlcommand = "SELECT CODE, DESCRIPTION,BARCODE, SALES_PRICE FROM " +
                    "(select A.ID,CODE,DESCRIPTION,B.BARCODE,C.SALES_PRICE,C.CREATE_DATE from JRETAIL_MASTER_" + masternum + "..MATERIAL A " +
                    "JOIN JRETAIL_MASTER_" + masternum + "..MATERIAL_BARCODE B on A.ID=B.MATERIAL_ID " +
                    "JOIN JRETAIL_PERIOD_" + masternum + "_" + periodnum + "..MATERIAL_SALES_PRICE  C ON A.ID=C.MATERIAL_ID) Y " +
                    "WHERE Y.CREATE_DATE = (SELECT MAX(CREATE_DATE) FROM JRETAIL_PERIOD_" + masternum + "_" + periodnum + "..MATERIAL_SALES_PRICE X WHERE X.MATERIAL_ID=Y.ID ) " +
                    "AND BARCODE='" + producutid + "'";
        }
        System.out.println("con is:" + conurl);
        String connectionUrl = conurl;
        ResultSet resultSet = null;
        try (Connection connection = DriverManager.getConnection(connectionUrl);
             Statement statement = connection.createStatement();) {
            System.out.println(producutid);
            resultSet = statement.executeQuery(sqlcommand);
            JSONArray array = new JSONArray();
            JSONObject object = new JSONObject();
            while (resultSet.next()) {
                JSONObject record = new JSONObject();
                record.put("ID", resultSet.getString(1));
                record.put("description", resultSet.getString(2));
                record.put("barcode", resultSet.getString(3));
                record.put("price", resultSet.getString(4));
                array.add(record);
            }
            object.put("Product name", array);
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String prettyJsonString = gson.toJson(object);
            System.out.println(object.toString());
            return prettyJsonString;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "";
    }
}
