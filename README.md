# Satispay Signature implementation

Example of a program that calls the following url providing a http signature:

```
https://staging.authservices.satispay.com/wally-services/protocol/tests/signature
```



###### Directory structure:

	httpsignature
		src
			main
			 	...
			 		App.java
			 		Connection.java
			 		SignatureGenerator.java
			 test
		resources
			client-rsa-private-key.pem     // not present
			client-rsa-public-key.pem	   // not present

* __App.java__ Contains the main method

* **Connection.java** Contains the logic to connect to a path, compile the signature and make requests

* **SignatureGenerator** Has three methods one to read the private key file, one to sign with  RSASSA-PKCS1-V1_5 and a private key and one to generate a SHA-512 hash.

  

#### How to run:

The project uses Java and Maven 3.6 it doesn't require any other dependency.

To run inside the directory:

```
mvn package
```

and after that inside the target directory:

```
java -jar httpsignature-1.0-SNAPSHOT.jar
```



You should see something like this, it means the request was succesfull 

```json
GET request
{"authentication_key":{"access_key":"signature-test-66289","customer_uid":"00d25aa7-aa6e-4941-b8af-a18d4d9b8db3","key_type":"RSA_PUBLIC_KEY","auth_type":"SIGNATURE","role":"DEVICE","enable":true,"insert_date":"2017-11-28T15:04:50.000Z","version":1},"signature":{"key_id":"signature-test-66289","algorithm":"RSA_SHA256","headers":["(request-target)","host","date","digest"],"signature":"R5CZcjev2AfuNIdKqcJWGUNZ85zcjactdbP0EW6KZrsjHrOkLduv79Ce6k201vsl67SbpUw/edNTj2B3+gi+jw5H9VYXHmBNcRqUVzG+Gx3yAa2QlaTexvvboyQPLULhEosFoXXlJo0Dxesi02lsjLE1xUk7wYL0IjcMQkKgOXF/P4SL1DQuJhe5rRs9vr9Fq0y2/cE09jy1rgx9/IhqJKM4VD+kgTNrQDdFwG85s9m1azmirXhwnBUFrhSf0bvrQX/zhIq8D0OSpcqgSd/TeLHmGbBUl1fhfyMk3vOBmek1cghvW++0QCdv3349WsHutJOJHKkqZRPUirv+PNe9Bw==","resign_required":false,"iteration_count":2617,"valid":true},"signed_string":"(request-target): get /wally-services/protocol/tests/signature\nhost: staging.authservices.satispay.com\ndate: Fri, 15 Mar 2019 17:08:02 GMT\ndigest: SHA-512=z4PhNX7vuL3xVChQ1m2AB9Yg5AULVxXcg/SpIdNs6c5H0NE8XYXysP+DGNKHfuwvY7kxvUdBeoGlODJ6+SfaPg=="}

...
```

#### What it does:


After creating the objects Signature Generator and Connection it reads the private key if something goes wrong here the program aborts.

```Java
SignatureGenerator sg = new SignatureGenerator();
Connection cn = new Connection(url);

keyPrivata = sg.readMykey(PathKeyPrivata);

if (keyPrivata.isEmpty()) {
    System.out.println(" Errore nella chiave privata");
    System.exit(-1);            
}
```

For this example the hash is empty but in reality it contains the hashed message that is being sent.

For every http method (GET,POST,PUT,DELETE) these are the steps that get executed:

* A connection is made with a parameter that defines the http method. The constructor sets up every parameter, the only one left to set up is the digest. 

* The text to be signed gets passed to the method **signSHA256RSA** with the private key, if something goes wrong here the program aborts, if not the method returns the signature in base64 format.
* The signature is passed to the request that returns the raw text response

```java
String base64digest = sg.makeSHA512Hash(""); 

cn.connect(request); 
cn.setDigest(base64digest);
toCrypt = cn.assembleSignature();

base64Signature = sg.signSHA256RSA(toCrypt,keyPrivata);
if(base64Signature.isEmpty()){
    System.out.println(" Errore base64 key");
    System.exit(-2);  
}

cn.setSignature(base64Signature);
String response = cn.makeRequest();

System.out.println(response);
System.out.println("\n");
```

