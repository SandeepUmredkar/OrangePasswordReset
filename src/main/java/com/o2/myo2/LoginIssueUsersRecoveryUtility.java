package com.o2.myo2;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class LoginIssueUsersRecoveryUtility {
    public static void main(String args[]) throws IOException {
        Properties applicationProperties = new Properties();
        String propFileName = "application.properties";
        File file = new File(propFileName).getAbsoluteFile();
        InputStream inputStream = new FileInputStream(file);
        applicationProperties.load(inputStream);
        LoginIssueRepository loginIssueRepository = new LoginIssueRepository();
        LoginIssueService loginIssueService = new LoginIssueService(loginIssueRepository);
        LoginIssueController loginIssueController = new LoginIssueController(loginIssueService);
        loginIssueController.recoverDeletedMsisdns(applicationProperties.getProperty("mongo.connectionUrl"), applicationProperties.getProperty("fileForRecovery"));
    }
}
