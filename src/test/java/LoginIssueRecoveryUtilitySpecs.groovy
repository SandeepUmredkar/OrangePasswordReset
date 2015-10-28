package com.o2.myo2

import com.mongodb.BasicDBObject
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoCursor
import spock.lang.Specification

class LoginIssueRecoveryUtilitySpecs extends Specification {

    private Properties applicationProperties;
    DBConnector dbConnector

    public setup() {
        dbConnector = new DBConnector();
        applicationProperties = new Properties();
        String propFileName = "application.properties";
        InputStream inputStream = new FileInputStream(propFileName);
        applicationProperties.load(inputStream);
    }

    //The msisdns and UIDs and taken from the file specified in application.properties and mongo also is the one configured in local.proeperties
    public void itShouldRecoverDataAndStoreInMongo() {
        setup:
        LoginIssueRepository loginIssueRepository = new LoginIssueRepository();
        LoginIssueService loginIssueService = new LoginIssueService(loginIssueRepository);
        LoginIssueController loginIssueController = new LoginIssueController(loginIssueService);

        when:
        MongoCollection collection = dbConnector.getPersonCollection("localhost");
        def queryForMsisdn1 = new BasicDBObject("normalisedAssetValue", "+447513276804")
                .append("uid", "13000000000013749046").append("assetType", "PAYMMBB")

        def queryForMsisdn2 = new BasicDBObject("normalisedAssetValue", "+447516624505")
                .append("uid", "13000000000012674537").append("assetType", "PAYMMBB")
        MongoCursor mongoUser1 = collection.find(queryForMsisdn1).iterator()
        boolean document1BeforeInsertion = mongoUser1.hasNext()
        MongoCursor mongoUser2 = collection.find(queryForMsisdn2).iterator()
        boolean document2BeforeInsertion = mongoUser2.hasNext()
        loginIssueController.recoverDeletedMsisdns(applicationProperties.getProperty("mongo.connectionUrl"), "recoveryTest.log");

        then:
         !document1BeforeInsertion
         !document2BeforeInsertion
         MongoCursor mongoUserAfterRecovery1 = collection.find(queryForMsisdn1).iterator()
         boolean document1AfterInsertion = mongoUserAfterRecovery1.hasNext()
         MongoCursor mongoUserAfterRecovery2 = collection.find(queryForMsisdn2).iterator()
         boolean document2AfterInsertion = mongoUserAfterRecovery2.hasNext()
         document1AfterInsertion
         document2AfterInsertion
    }
}
