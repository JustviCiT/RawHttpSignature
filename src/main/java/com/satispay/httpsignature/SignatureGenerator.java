package com.satispay.httpsignature;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.InvalidKeyException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;

import java.security.Signature;
import java.security.MessageDigest;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.FileNotFoundException;


public class SignatureGenerator {
    /** 
     * Reads a file 
     * @param mypath path for the file to read
     * @return string the content of the file or empty on error
     */
    public String readMykey(String mypath){
        /* Read File */
        String fileContent="";
        
        try {
            fileContent= new String(Files.readAllBytes(Paths.get(mypath)));
        }
        catch (FileNotFoundException e) {
            System.out.println("File non trovato" + e);
            fileContent="";
        }
        catch (IOException ioe) {
            System.out.println("Exception lettura file" + ioe);
            fileContent="";
        }

        return fileContent;
    }

    /**
     *  Sha512 Hashing
     *  @param myText input string to be hashed
     *  @return hash base64 encoded string, empty on error
     *  */  
    public String makeSHA512Hash(String myText){
        byte[] hash = "".getBytes();

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-512");
            hash = digest.digest(myText.getBytes("UTF-8") );
        } catch (NoSuchAlgorithmException ex) {
            System.out.println("Algoritmo non trovato");
            return "";
        } catch (UnsupportedEncodingException e) {
            System.out.println("Fallimento Key UTF 8");
            return "";
        }

        return Base64.getEncoder().encodeToString(hash);
    }

    /**
     *  Create base64 encoded signature using SHA256/RSA.
     *  Based on https://www.quickprogrammingtips.com/java/how-to-create-sha256-rsa-signature-using-java.html
     *  @param input string used to sign
     *  @param strPk private key in pem format
     *  */  
    public String signSHA256RSA(String input, String privateKeyPEM) {
        // Remove markers and new line characters in private key
        privateKeyPEM = privateKeyPEM.replace("-----BEGIN PRIVATE KEY-----\n", "");
        privateKeyPEM = privateKeyPEM.replace("-----END PRIVATE KEY-----", "");

        //System.out.println(privateKeyPEM);

        // MimeDecoder !! instead of basic one 
        byte[] b1 = Base64.getMimeDecoder().decode(privateKeyPEM);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(b1);

        KeyFactory kf=null;
        Signature privateSignature = null;
        byte[] myReturn = "".getBytes();

        try {
            kf = KeyFactory.getInstance("RSA"); 
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Fallimento RSA");
            return "";
        }

        try {
            privateSignature = Signature.getInstance("SHA256withRSA");
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Fallimento SHA256withRSA");
            return "";
        }

        try {
            privateSignature.initSign(kf.generatePrivate(spec));
        } catch (InvalidKeySpecException e) {
            System.out.println("Fallimento KeySpec");
            return "";
        }catch (InvalidKeyException e) {
            System.out.println("Fallimento Key");
            return "";
        }

        try {
            privateSignature.update(input.getBytes("UTF-8"));
            myReturn = privateSignature.sign();
        } catch (UnsupportedEncodingException e) {
            System.out.println("Fallimento Key UTF 8");
            return "";
        }catch (SignatureException e) {
            System.out.println("Fallimento FiRMA");
            return "";
        }

        return Base64.getEncoder().encodeToString(myReturn);
    }
}