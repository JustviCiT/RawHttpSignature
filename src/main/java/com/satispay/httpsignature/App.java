package com.satispay.httpsignature;

import java.util.List;
import java.util.ArrayList;

public final class App {
    public static void main(String[] args) {
        String url = "https://staging.authservices.satispay.com/wally-services/protocol/tests/signature";
        //String url = "http://localhost:8000";
        String PathKeyPrivata = "client-rsa-private-key.pem";
        //String PathKeyPubblica = "client-rsa-public-key-pem";
        String keyPrivata = "";
        //String keyPubblica = "";
        String base64Signature ="";
        String toCrypt = "";

        SignatureGenerator sg = new SignatureGenerator();
        Connection cn = new Connection(url);

        keyPrivata = sg.readMykey(PathKeyPrivata);
        if (keyPrivata.isEmpty()) {
            System.out.println(" Errore nella chiave privata");
            System.exit(-1);            
        }
        // System.out.println(keyPrivata);

        List<String> myRequests =  new ArrayList<String>();
        myRequests.add("GET");
        myRequests.add("PUT");
        myRequests.add("POST");
        myRequests.add("DELETE");

        String base64digest = sg.makeSHA512Hash(""); 
        //System.out.println(base64digest);
        for (String request : myRequests )  {
            System.out.println(request+ " request");
            cn.connect(request); 
            cn.setDigest(base64digest);
    
            toCrypt = cn.assembleSignature();
            //System.out.println(toCrypt);
    
            base64Signature = sg.signSHA256RSA(toCrypt,keyPrivata);
            if(base64Signature.isEmpty()){
                System.out.println(" Errore base64 key");
                System.exit(-2);  
            }
            cn.setSignature(base64Signature);
    
            String response = cn.makeRequest();
            System.out.println(response);
            System.out.println("\n");
        }

        //cn.showResponseHeaders();
    }
}
