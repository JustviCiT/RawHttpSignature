package com.satispay.httpsignature;

import java.net.*;
import java.util.List;
import java.util.Map;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;
import java.time.*;
import java.time.format.DateTimeFormatter;
/**
 * 
 * Example String to sign:
 * 
 * (request-target): get /wally-services/protocol/tests/signature
    host: staging.authservices.satispay.com
    date: Fri, 15 Mar 2019 12:05:18 GMT
 *
 * Example Auth:
 * 
 * Authorization: Signature keyId="Test",
 *  algorithm="rsa-sha256",
 *  headers="(request-target) host date digest",
 *  signature="ATp0r26dbMIxOopqw0OfABDT7CKMIoENumuruOtarj8n/97Q3htHFYpH8yOSQk3Z5zh8UxUym6FYTb5+A0Nz3NRsXJibnYi7brE/4tx5But9kkFGzG+xpUmimN4c3TMN7OFH//+r8hBf7BT9/GmHDUVZT2JzWGLZES2xDOUuMtA="
 * 
 */

public final class Connection {
    private String keyId;
    private String algorithm;
    private String headers;
    private String signature;
    private String date;
    private String digest;

    private String path;
    private String autorization;

    private URL url;
    private URLConnection con;
    private HttpURLConnection http;

    public Connection(String url){
        this.path = url;
        this.url = null;
        this.con = null;
        this.keyId = "signature-test-66289";
        this.algorithm = "rsa-sha256";
        this.headers = "(request-target) host date digest"; //  host date digest
        this.signature = "";
        this.setDigest("");
        this.generateDate();
        this.setAuthrization();
    }

    /**
    *  Converts stream to String
    *  @param inputStream 
    */
    private static String streamToString(InputStream inputStream) {
        Scanner Sc = new Scanner(inputStream, "UTF-8");
        String text = Sc.useDelimiter("\\Z").next();
        Sc.close();
        return text;
    }

    /**
    *  Generate today's date
    */
    private void generateDate(){
        // Date Thu, 14 Mar 2019 21:58:47 GMT
        ZonedDateTime date = ZonedDateTime.now().withZoneSameInstant(ZoneId.of("UTC"));
        this.date = date.format(DateTimeFormatter.RFC_1123_DATE_TIME);
    }

    /**
     *  Setup for every new connection/request
     *  @param type type of request GET, POST, PUT, DELETE
     */
    public void connect(String type){
        
        try {
            this.url = new URL(this.path);
            this.con = this.url.openConnection();
        } catch (MalformedURLException e) {
            System.out.println(" Errore nella connessione");
            return;
        } catch (IOException e) {
            System.out.println(" Errore nel path url");
            return;
        }

        this.http = (HttpURLConnection) this.con;
        //this.http.setDoOutput(true);

        try {
            this.http.setRequestMethod(type);
        } catch (ProtocolException e) {
            System.out.println(" Errore nel tipo di connessione");
            return ;
        }
    }

    /**
     * Creates the signature
     */
    public String assembleSignature(){
        //System.out.println(this.date);

        String out ="";
        String[] myHeaders = this.headers.split(" ");
        int len = myHeaders.length;
        for (int i = 0; i < len; i++) {
            if(myHeaders[i].equalsIgnoreCase("(request-target)")){
                out += "(request-target): "+ this.http.getRequestMethod().toLowerCase()+ " " + this.url.getPath() +"\n";
            }
            if(myHeaders[i].equalsIgnoreCase("date")){
                out += "date: "+this.date+"\n";
            }
            if(myHeaders[i].equalsIgnoreCase("digest")){
                out += "digest: "+this.digest+"\n";
            }
            if(myHeaders[i].equalsIgnoreCase("host")){
                out += "host: "+this.url.getHost()+"\n";
            }
        }

        // Removes last newline
        out = out.substring(0, out.length()-1);

        return out;
    }

    /** Used to make a single request
     *  @return string string from server
     */
    public String makeRequest(){

        this.http.setRequestProperty("Authorization", this.autorization);
        this.http.setRequestProperty("Date", this.date);
        this.http.setRequestProperty("Digest", this.digest);
        //System.out.println(this.autorization);

        // Execute the request
        InputStream inStream = null;
        try {
            inStream = this.http.getInputStream();
        } catch (IOException e) {
            System.out.println(" Errore nella richiesta connessione ");
            return "";
        }

        return streamToString(inStream);
    }

    /**
     * Used for debug
     */
    public void showResponseHeaders(){
        if (this.con == null)
            return;

        Map<String, List<String>> map = this.con.getHeaderFields();
        System.out.println("Printing Response Header...\n");

        for (Map.Entry<String, List<String>> entry : map.entrySet()) {
            System.out.println(entry.getKey() + entry.getValue());
        }
    }

    /**
     * SETTERS
    */

    private void setAuthrization(){
        this.autorization = " Signature "+"keyId=\""+this.keyId+"\","
                          + "algorithm=\""+this.algorithm+"\","
                          + "headers=\""+this.headers+"\","
                          + "signature=\""+this.signature+"\"";
                      //    + "satispaysequence=\"4\"";
    }
    public void setHeaders(String header){
        this.headers = header;
        this.setAuthrization();
    }
    public void setPath(String url){
        this.path = url;
    }
    public void setAlgorithm(String alg){
        this.algorithm = alg;
        this.setAuthrization();
    }
    public void setSignature(String signature){
        this.signature = signature;
        this.setAuthrization();
    }
    public void setKeyID(String id){
        this.keyId = id;
        this.setAuthrization();
    }
    public void setDigest(String digest){
        this.digest = "SHA-512="+digest;
        this.setAuthrization();
    }

}
