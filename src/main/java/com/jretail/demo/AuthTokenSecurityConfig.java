package com.jretail.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Base64;

@EnableWebSecurity
@Order(1)
public class AuthTokenSecurityConfig extends WebSecurityConfigurerAdapter {

    @Value("${bizimkendimizjava.http.auth.tokenName}")
    private String authHeaderName;

    @Value("${connection.url}")
    private String conurl;

    public static String urlstirng="";
    public static String masternum="";
    public static String periodnum="";

    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {
        String connectionUrl = conurl;
        ResultSet resultSet = null;
        ArrayList<String> apikeys=new ArrayList<>();
        ArrayList<String> conurls=new ArrayList<>();
        ArrayList<String> masternums=new ArrayList<>();
        ArrayList<String> periodnums=new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(connectionUrl);
             Statement statement = connection.createStatement();) {
            // Create and execute a SELECT SQL statement.
            String selectSql = "SELECT * FROM apikeys";
            resultSet = statement.executeQuery(selectSql);
            while (resultSet.next()) {
                apikeys.add(resultSet.getString(3));
                conurls.add(resultSet.getString(4));
                masternums.add(resultSet.getString(5));
                periodnums.add(resultSet.getString(6));
                System.out.println(resultSet.getString(3));
            }
            SecurityConfiguration filter = new SecurityConfiguration(authHeaderName);

            filter.setAuthenticationManager(new AuthenticationManager() {
                @Override
                public Authentication authenticate(Authentication authentication)
                        throws AuthenticationException {
                    String principal = (String) authentication.getPrincipal();
                    String coded= String.valueOf(Base64.getEncoder().encodeToString(principal.getBytes(StandardCharsets.UTF_8)));
                    if (!apikeys.contains(coded)) {
                        throw new BadCredentialsException("The API key was not found "
                                + "or not the expected value.");
                    }

                    urlstirng=conurls.get(apikeys.indexOf(coded));
                    masternum=masternums.get(apikeys.indexOf(coded));
                    periodnum=periodnums.get(apikeys.indexOf(coded));
                    authentication.setAuthenticated(true);
                    return authentication;
                }
            });
            httpSecurity.
                    antMatcher("/getjson/**").
                    csrf().disable().
                    sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).
                    and().addFilter(filter).authorizeRequests().anyRequest().authenticated();
        }
    }
}