package com.o2.orange;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

import javax.ws.rs.core.MultivaluedMap;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.xml.internal.messaging.saaj.util.Base64;


public class PasswordResetRepository {
    File file;
    HttpClient client;
    FileOutputStream outputFile;
    PrintWriter outputFileWriter;
    private Properties applicationProperties = new Properties();
    FileOutputStream successFullResetUids ;
    FileOutputStream FailedResetUids ;
    PrintWriter successFileWriter;
    PrintWriter failedFileWriter;

    public PasswordResetRepository() throws IOException {
        String propFileName = "application.properties";
        file = new File(propFileName).getAbsoluteFile();
        InputStream inputStream = new FileInputStream(file);
        applicationProperties.load(inputStream);
        String outputFilePath = applicationProperties.getProperty("UidPasswordResetExceptionLogFilePath");
        outputFile = new FileOutputStream(outputFilePath, false);
        outputFileWriter = new PrintWriter(outputFile);
       String successFullResetUidsPath = applicationProperties.getProperty("successFullResetUidsPath");
       String failedResetUidsPath = applicationProperties.getProperty("failedResetUidsPath");
       successFileWriter = new PrintWriter(successFullResetUidsPath);
       failedFileWriter = new PrintWriter(failedResetUidsPath);
       // applicationProperties.load(inputStream);
    }

    public List<String> getUidsForPasswordReset(String filePathForUidsListToResetPasswword) throws FileNotFoundException {
        List<String> uidsForPasswordReset = new ArrayList<String>();
        Scanner fileScanner = new Scanner(new File(filePathForUidsListToResetPasswword));
        while (fileScanner.hasNextLine()) {
            String lineForUid = fileScanner.nextLine();
            uidsForPasswordReset.add(lineForUid);
        }
        fileScanner.close();
        return uidsForPasswordReset;
    }

    public void resetPassword(List<String> uidsForPasswordReset) throws IOException, JSONException {
        String spooferUsername = applicationProperties.getProperty("spoofer_username");
        String spooferPassword = applicationProperties.getProperty("spoofer_password");
        String authCodeUrl = applicationProperties.getProperty("authCodeUrl");
        String accessTokenUrl = applicationProperties.getProperty("accessTokenUrl");
        String passwordResetUrl = applicationProperties.getProperty("passwordResetUrl");
        String timeGap = applicationProperties.getProperty("timeGap");
        for (String uidForPasswordReset : uidsForPasswordReset) {
            String authCode = getAuthCode(spooferUsername, spooferPassword, authCodeUrl, uidForPasswordReset);
            String accessToken = getAccessToken(authCode, accessTokenUrl, uidForPasswordReset);
            boolean response = generateResetPassword(accessToken, uidForPasswordReset, passwordResetUrl);
            if(response)
            {
            	successFileWriter.println(uidForPasswordReset);
            }
            else
            {
            	failedFileWriter.println(uidForPasswordReset);
            }
        }
        
        outputFileWriter.flush();
        successFileWriter.flush();
        failedFileWriter.flush();
        failedFileWriter.close();
        successFileWriter.close();
        outputFileWriter.close();

    }
    
    private String getAuthCode(String spooferUsername, String spooferPassword, String authCodeUrl, String uidForPasswordReset) throws JSONException, UnsupportedEncodingException {
    	String authCode = null;
    	Client client = Client.create();
		WebResource webResource = client.resource(authCodeUrl); 
		  JSONObject jsonForAuthCode = new JSONObject();
	        jsonForAuthCode.put("spoofer_username", spooferUsername);
	        jsonForAuthCode.put("spoofer_password", spooferPassword);
	        jsonForAuthCode.put("uid", uidForPasswordReset);
	        try {
    	ClientResponse responseForAuthCode = webResource
    				.accept("application/json")
    				.header("Content-Type","application/json")
    				.header("Authorization",getAuthorizationHeader())
    				.post(ClientResponse.class, jsonForAuthCode.toString());
    		int statusCodeForAuthCode = responseForAuthCode.getStatus();
    		MultivaluedMap<String, String> responseHeader = responseForAuthCode.getHeaders();
    		String apiGwTransId = responseHeader.get("Apigw-Transaction-Id") != null ? responseHeader.get("Apigw-Transaction-Id").toString() : "Apigw-Transaction-Id is null" ;
  
            if (!((statusCodeForAuthCode + "").startsWith("2"))) {
                outputFileWriter.println("AuthCode");
                outputFileWriter.println(uidForPasswordReset + "=" + statusCodeForAuthCode + " apiGwTransId" + "=" + apiGwTransId);
                return authCode;
            }
            AuthCode authCodeObject = new ObjectMapper().readValue(responseForAuthCode.getEntity(String.class),  AuthCode.class);
            authCode = authCodeObject.getAuth_code();
            outputFileWriter.println(uidForPasswordReset + "=" + statusCodeForAuthCode + " apiGwTransId" + "=" + apiGwTransId  + "generated Auth code" + "=" + authCode);
        } catch (Exception authCodeException) {
            outputFileWriter.println("AuthCode");
            outputFileWriter.println(uidForPasswordReset + "=" + authCodeException.getMessage());
        }

        return authCode;
    }
    
   
    
    
    private String getAccessToken(String authCode, String accessTokenUrl, String uidForPasswordReset) throws JSONException, UnsupportedEncodingException {
    	
    	
    	String accessToken = null;
    	Client client = Client.create();
		WebResource webResource = client.resource(accessTokenUrl);
		JSONArray accessTokenInputArray = new JSONArray();
		accessTokenInputArray.put("openid");
		accessTokenInputArray.put("profile");
		accessTokenInputArray.put("email");
		accessTokenInputArray.put("phone");
		accessTokenInputArray.put("legacy");
		
		JSONObject jsonForAccessToken = new JSONObject();
		  jsonForAccessToken.put("code", authCode);
	        jsonForAccessToken.put("grant_type", "authorization_code");
	        jsonForAccessToken.put("scope",accessTokenInputArray );
	        try {
    	ClientResponse requestForAccessToken = webResource
    				.accept("application/json")
    				.header("Content-Type","application/json")
    				.header("Authorization",getAuthorizationHeader())
    				.post(ClientResponse.class, jsonForAccessToken.toString());
    		int statusCodeForAccessToken = requestForAccessToken.getStatus();
    		MultivaluedMap<String, String> responseHeader = requestForAccessToken.getHeaders();
    		String apiGwTransId = responseHeader.get("Apigw-Transaction-Id") != null ? responseHeader.get("Apigw-Transaction-Id").toString() : "Apigw-Transaction-Id is null" ;
  
            if (!((statusCodeForAccessToken + "").startsWith("2"))) {
                outputFileWriter.println("AccessToken");
                outputFileWriter.println(uidForPasswordReset + "=" + statusCodeForAccessToken  + " apiGwTransId" + "=" + apiGwTransId);
                return "";
            }
            AccessToken accessTokenObejct = new ObjectMapper().readValue(requestForAccessToken.getEntity(String.class), AccessToken.class);
            accessToken = accessTokenObejct.getAccess_token();
            outputFileWriter.println(uidForPasswordReset + "=" + statusCodeForAccessToken  + " apiGwTransId" + "=" + apiGwTransId + "generated access token " + "=" + accessToken);
        } catch (Exception accessTokenException) {
            outputFileWriter.println("AccessToken");
            outputFileWriter.println(uidForPasswordReset + "=" + accessTokenException.getMessage());
        }
        return accessToken;
    }

    private boolean generateResetPassword(String accessToken, String uidForPasswordReset, String passwordResetUrl) throws JSONException, UnsupportedEncodingException {
    	
    	Client client = Client.create();
		WebResource webResource = client.resource(passwordResetUrl);
		JSONObject jsonForResetPassword = new JSONObject();
		 jsonForResetPassword.put("password", GenerateRandomPassword.generateRandomPassword(12));
	        try {
    	ClientResponse requestForAccessToken = webResource
    				.accept("application/json")
    				.header("Content-Type","application/json")
    				.header("Authorization","Bearer " + accessToken)
    				.post(ClientResponse.class, jsonForResetPassword.toString());
    		int statusCodeForPasswordReset = requestForAccessToken.getStatus();
    		MultivaluedMap<String, String> responseHeader = requestForAccessToken.getHeaders();
      		String apiGwTransId = responseHeader.get("Apigw-Transaction-Id") != null ? responseHeader.get("Apigw-Transaction-Id").toString() : "Apigw-Transaction-Id is null" ;
            if (!((statusCodeForPasswordReset + "").startsWith("2"))) {
                outputFileWriter.println("Password Reset");
                outputFileWriter.println(uidForPasswordReset + "=" + statusCodeForPasswordReset + " apiGwTransId" + "=" + apiGwTransId);
                return false;
            }
            else
            {
            	outputFileWriter.println(uidForPasswordReset + "=" + statusCodeForPasswordReset + " apiGwTransId" + "=" + apiGwTransId + "- Successfully called reset Api for UID");
            	return true; 
            }
            
            	
        } catch (Exception passwordResetException) {
            outputFileWriter.println("Password Reset");
            outputFileWriter.println(uidForPasswordReset + "=" + passwordResetException.getMessage());
        }
        return false;
    }

    private String getAuthorizationHeader() {
        String username = applicationProperties.getProperty("userName");
        String password = applicationProperties.getProperty("password");
        String header = username + ":" + password;
        byte[] unencodedByteArray = header.getBytes();
        byte[] encodedByteArray = Base64.encode(unencodedByteArray);
        String encodedString = new String(encodedByteArray);
        return "Basic " + encodedString;
    }
}