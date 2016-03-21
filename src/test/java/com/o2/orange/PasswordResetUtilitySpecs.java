package com.o2.orange


import org.junit.Before;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

class PasswordResetUtilitySpecs {

    private Properties applicationProperties;

    @Before
    public void setUp() throws IOException {
        applicationProperties = new Properties();
        String propFileName = "application.properties";
        InputStream inputStream = new FileInputStream(propFileName);
        applicationProperties.load(inputStream);
    }

    //The msisdns and UIDs and taken from the file specified in application.properties and mongo also is the one configured in local.proeperties
    public void itShouldRecoverDataAndStoreInMongo() throws IOException {
        PasswordResetRepository passwordResetRepository = new PasswordResetRepository();
        PasswordResetService passwordResetService = new PasswordResetService(passwordResetRepository);
        PasswordResetController passwordResetController = new PasswordResetController(passwordResetService);

        passwordResetController.resetPassword(applicationProperties.getProperty("fileForUidsForPasswordReset"));
    }
}
