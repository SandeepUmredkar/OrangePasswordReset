package com.o2.myo2

import com.mongodb.BasicDBObject
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoCursor
import spock.lang.Specification

class LoginIssueRemoverUtilitySpecs extends Specification {

    private Properties applicationProperties;
    private DBConnector dbConnector;

    public setup() {
        applicationProperties = new Properties();
        String propFileName = "application.properties";
        InputStream inputStream = new FileInputStream(propFileName);
        applicationProperties.load(inputStream);
        dbConnector = new DBConnector();
    }

    //The msisdns and UIDs and taken from the file specified in application.properties and mongo also is the one configured in local.proeperties
    public void itShouldRemoveImpactedUsersFromMongo() {
        setup:
        LoginIssueRepository loginIssueRepository = new LoginIssueRepository();
        LoginIssueService loginIssueService = new LoginIssueService(loginIssueRepository);
        LoginIssueController loginIssueController = new LoginIssueController(loginIssueService);

        when:
        MongoCollection collection = dbConnector.getPersonCollection("localhost");
        def queryForMsisdn1 = new BasicDBObject("normalisedAssetValue", "+447511430091")
                .append("uid", "13000000000013623002").append("assetType", "PAYMMBB")

        def queryForMsisdn2 = new BasicDBObject("normalisedAssetValue", "+447516624505")
                .append("uid", "13000000000012674537").append("assetType", "PAYMMBB")
        MongoCursor mongoUser1 = collection.find(queryForMsisdn1).iterator()
        boolean document1BeforeDeletion = mongoUser1.hasNext()
        MongoCursor mongoUser2 = collection.find(queryForMsisdn2).iterator()
        boolean document2BeforeDeletion = mongoUser2.hasNext()
        loginIssueController.removeDeacvtivatedMsisdns("outputFileForTest", "recoveryTest.log", applicationProperties.getProperty("mongo.connectionUrl"));

        then:
        document1BeforeDeletion
        document2BeforeDeletion
        MongoCursor mongoUserAfterDeletion1 = collection.find(queryForMsisdn1).iterator()
        boolean document1AfterDeletion = mongoUserAfterDeletion1.hasNext()
        MongoCursor mongoUserAfterDeletion2 = collection.find(queryForMsisdn2).iterator()
        boolean document2AfterDeletion = mongoUserAfterDeletion2.hasNext()
        !document1AfterDeletion
        !document2AfterDeletion
    }
}
