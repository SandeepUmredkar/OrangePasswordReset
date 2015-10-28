package com.o2.myo2;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class DBConnector {
    private Properties applicationProperties = new Properties();

    public MongoCollection getPersonCollection(String mongoPath) throws IOException {
        String propFileName = "application.properties";
        File file = new File(propFileName).getAbsoluteFile();
        InputStream inputStream = new FileInputStream(file);
        applicationProperties.load(inputStream);
        MongoClient mongoClient = new MongoClient(new MongoClientURI(mongoPath));
        return mongoClient.getDatabase(applicationProperties.getProperty("dbName")).getCollection(applicationProperties.getProperty("collectionName"));
    }
}