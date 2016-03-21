package com.o2.orange;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Properties;

public class PasswordResetUtility {
    public static void main(String args[]) throws IOException {
        Properties applicationProperties = new Properties();
        String propFileName = "application.properties";
        File file = new File(propFileName).getAbsoluteFile();
        InputStream inputStream = new FileInputStream(file);
        applicationProperties.load(inputStream);
        System.out.println("This is the utility which will reset the password for the UIDs provided in the file");
        System.out.println("UIDs are line separated");
        System.out.println("________________________________________________________________________________________________________________________________");
        System.out.println("Start Time for the utility" + new Date());
        PasswordResetRepository passwordResetRepository = new PasswordResetRepository();
        PasswordResetService passwordResetService = new PasswordResetService(passwordResetRepository);
        PasswordResetController passwordResetController = new PasswordResetController(passwordResetService);
        passwordResetController.resetPassword(applicationProperties.getProperty("fileForUidsForPasswordReset"));
        System.out.println("________________________________________________________________________________________________________________________________");
        System.out.println("Finish Time for the utility" + new Date());
    }
}
