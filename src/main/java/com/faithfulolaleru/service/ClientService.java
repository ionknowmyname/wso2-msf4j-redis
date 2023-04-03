/*
 * Copyright (c) 2016, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.faithfulolaleru.service;


import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.HttpHeaders;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * This is the Microservice resource class.
 * See <a href="https://github.com/wso2/msf4j#getting-started">https://github.com/wso2/msf4j#getting-started</a>
 * for the usage of annotations.
 *
 * @since 0.1-SNAPSHOT
 */
@Path("/service")
public class ClientService {

    private final CloseableHttpClient httpClient = HttpClients.createDefault();

    // HttpClient client = new DefaultHttpClient();

    private static HttpURLConnection conn;

    private static final java.util.logging.Logger log = java.util.logging.Logger.getLogger(ClientService.class.getName());

    // Jedis jedis = new Jedis("localhost");

    JedisPool pool = new JedisPool("localhost", 6379);
    // JedisPooled pool2 = new JedisPooled("localhost", 6379);


    @GET
    @Path("/")
    public String get() {
        StringBuffer responseContent = null;
        Jedis jedis = null;
        String result = null;

        try {
            jedis = pool.getResource();
            result = jedis.get("testing");
        } catch (Exception ex) {
            // pool.returnBrokenResource(jedis);
            // return result;
            ex.printStackTrace();
            System.out.println("Error from jedis try-catch --> " + ex.getMessage());
        }

        if(result != null) {
            return result;
        }

        try {
            URL url = new URL("https://jsonmock.hackerrank.com/api/football_matches");
            conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.addRequestProperty("Content-Type", "application/json");

            int status = conn.getResponseCode();

            InputStream in = (status < 200 || status > 299) ? conn.getErrorStream() : conn.getInputStream();

            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String responseLine;
            responseContent = new StringBuffer();

            while ((responseLine = reader.readLine()) != null) {
                responseContent.append(responseLine);
            }
            reader.close();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            conn.disconnect();

            if(responseContent != null) {
                assert jedis != null;
                jedis.set("testing", responseContent.toString());
            }
        }

        assert responseContent != null;
        return responseContent.toString();
    }

    @POST
    @Path("/")
    public void post() {
        // TODO: Implementation for HTTP POST request
        System.out.println("POST invoked");
    }

    @PUT
    @Path("/")
    public void put() {
        // TODO: Implementation for HTTP PUT request
        System.out.println("PUT invoked");
    }

    @DELETE
    @Path("/")
    public void delete() {
        // TODO: Implementation for HTTP DELETE request
        System.out.println("DELETE invoked");
    }

    @GET
    @Path("/old")
    public Object oldGet() {

        System.out.println("GET invoked");

        HttpGet request = new HttpGet("https://jsonmock.hackerrank.com/api/football_matches");

        request.addHeader(HttpHeaders.USER_AGENT, "Mozilla/5.0");

        try (CloseableHttpResponse response = httpClient.execute(request)) {

            System.out.println("response.getStatusLine --> " + response.getStatusLine().toString());

            HttpEntity entity = response.getEntity();
            Header headers = entity.getContentType();
            System.out.println(headers);

            if (entity != null) {
                // return it as a String
                String result = EntityUtils.toString(entity);
                // EntityUtils.consume(entity);
                System.out.println(" --> " + result);

                return result;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }


        return "Hello from WSO2 MSF4J";
    }
}
