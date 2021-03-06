package com.jretail.demo.Controller;

import com.jretail.demo.AuthTokenSecurityConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

@RestController
public class GetDataController {

    @Autowired
    private ReadData readData;


    @RequestMapping("/products/productslist")
    public String getdata(@RequestParam(value = "productid", defaultValue = "-1") String productid) {
        //ReadData readData = new ReadData();
        return readData.readdata(AuthTokenSecurityConfig.masternum, AuthTokenSecurityConfig.periodnum, productid, AuthTokenSecurityConfig.urlstirng);
    }
}
