package com.o2.orange;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.xml.internal.messaging.saaj.util.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;


public class PasswordResetRepository {
    File file;
    HttpClient client;
    FileOutputStream outputFile;
    PrintWriter outputFileWriter;
    private Properties applicationProperties = new Properties();

    public PasswordResetRepository() throws IOException {
        client = new DefaultHttpClient();
        String propFileName = "application.properties";
        file = new File(propFileName).getAbsoluteFile();
        InputStream inputStream = new FileInputStream(file);
        String outputFilePath = applicationProperties.getProperty("UidPasswordResetExceptionLogFilePath");
        outputFile = new FileOutputStream(outputFilePath, false);
        outputFileWriter = new PrintWriter(outputFile);
        applicationProperties.load(inputStream);
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

    public List<String> resetPassword(List<String> uidsForPasswordReset) throws IOException, JSONException {
        String spooferUsername = applicationProperties.getProperty("spoofer_username");
        String spooferPassword = applicationProperties.getProperty("spoofer_password");
        String authCodeUrl = applicationProperties.getProperty("authCodeUrl");
        String accessTokenUrl = applicationProperties.getProperty("accessTokenUrl");
        String passwordResetUrl = applicationProperties.getProperty("passwordResetUrl");
        String timeGap = applicationProperties.getProperty("timeGap");
        for (String uidForPasswordReset : uidsForPasswordReset) {
            String authCode = getAuthCode(spooferUsername, spooferPassword, authCodeUrl, uidForPasswordReset);
            String accessToken = getAccessToken(authCode, accessTokenUrl, uidForPasswordReset);
            generateResetPassword(accessToken, uidForPasswordReset, passwordResetUrl);
        }
        return null;

    }

    private String getAuthCode(String spooferUsername, String spooferPassword, String authCodeUrl, String uidForPasswordReset) throws JSONException, UnsupportedEncodingException {
        HttpPost requestForAuthCode = new HttpPost(authCodeUrl);
        requestForAuthCode.setHeader("Authorization", getAuthorizationHeader());
        JSONObject jsonForAuthCode = new JSONObject();
        jsonForAuthCode.put("spoofer_username", spooferUsername);
        jsonForAuthCode.put("spoofer_password", spooferPassword);
        jsonForAuthCode.put("uid", uidForPasswordReset);
        StringEntity stringEntityForAuthCode = new StringEntity(jsonForAuthCode.toString());
        requestForAuthCode.setEntity(stringEntityForAuthCode);
        try {
            HttpResponse responseForAuthCode = client.execute(requestForAuthCode);
            int statusCodeForAuthCode = responseForAuthCode.getStatusLine().getStatusCode();
            if (!((statusCodeForAuthCode + "").startsWith("2"))) {
                outputFileWriter.println("AuthCode");
                outputFileWriter.println(uidForPasswordReset + "=" + statusCodeForAuthCode);
                return "";
            }
            Gson gson = new GsonBuilder().create();
            AuthCode authCode = gson.fromJson(responseForAuthCode.getEntity().toString(), AuthCode.class);
        } catch (Exception authCodeException) {
            outputFileWriter.println("AuthCode");
            outputFileWriter.println(uidForPasswordReset + "=" + authCodeException.getMessage());
        }

        return "";
    }

    private String getAccessToken(String authCode, String accessTokenUrl, String uidForPasswordReset) throws JSONException, UnsupportedEncodingException {
        HttpPost requestForAccessToken = new HttpPost(accessTokenUrl);
        requestForAccessToken.setHeader("Authorization", getAuthorizationHeader());
        JSONObject jsonForAccessToken = new JSONObject();
        jsonForAccessToken.put("code", authCode);
        jsonForAccessToken.put("grant_type", "authorization_code");
        jsonForAccessToken.put("scope", " [\"openid\",\"profile\",\"email\",\"phone\",\"legacy\"]");
        StringEntity stringEntityForAccessToken = new StringEntity(jsonForAccessToken.toString());
        requestForAccessToken.setEntity(stringEntityForAccessToken);
        try {
            HttpResponse responseForAccessToken = client.execute(requestForAccessToken);
            int statusCodeForAccessToken = responseForAccessToken.getStatusLine().getStatusCode();
            if (!((statusCodeForAccessToken + "").startsWith("2"))) {
                outputFileWriter.println("AccessToken");
                outputFileWriter.println(uidForPasswordReset + "=" + statusCodeForAccessToken);
                return "";
            }
            Gson gson = new GsonBuilder().create();
            AccessToken accessToken = gson.fromJson(responseForAccessToken.getEntity().toString(), AccessToken.class);
        } catch (Exception accessTokenException) {
            outputFileWriter.println("AccessToken");
            outputFileWriter.println(uidForPasswordReset + "=" + accessTokenException.getMessage());
        }
        return "";
    }

    private String generateResetPassword(String accessToken, String uidForPasswordReset, String passwordResetUrl) throws JSONException, UnsupportedEncodingException {
        HttpPost requestForRestPassword = new HttpPost(passwordResetUrl);
        requestForRestPassword.setHeader("Authorization", "Bearer " + accessToken);
        JSONObject jsonForResetPassword = new JSONObject();
        jsonForResetPassword.put("password", GenerateRandomPassword.generateRandomPassword(12));
        StringEntity stringEntityForResetPassword = new StringEntity(jsonForResetPassword.toString());
        requestForRestPassword.setEntity(stringEntityForResetPassword);
        try {
            HttpResponse responseForResetPassword = client.execute(requestForRestPassword);
            int statusCodeForPasswordReset = responseForResetPassword.getStatusLine().getStatusCode();
            if (!((statusCodeForPasswordReset + "").startsWith("2"))) {
                outputFileWriter.println("Password Reset");
                outputFileWriter.println(uidForPasswordReset + "=" + statusCodeForPasswordReset);
            }
        } catch (Exception passwordResetException) {
            outputFileWriter.println("Password Reset");
            outputFileWriter.println(uidForPasswordReset + "=" + passwordResetException.getMessage());
        }
        return "";
    }

    private String getAuthorizationHeader() {
        String username = applicationProperties.getProperty("username");
        String password = applicationProperties.getProperty("password");
        String header = username + ":" + password;
        byte[] unencodedByteArray = header.getBytes();
        byte[] encodedByteArray = Base64.encode(unencodedByteArray);
        String encodedString = new String(encodedByteArray);
        return "Basic " + encodedString;
    }
}