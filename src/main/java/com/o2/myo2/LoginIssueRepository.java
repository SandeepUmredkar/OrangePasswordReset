package com.o2.myo2;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.util.JSON;
import org.bson.Document;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class LoginIssueRepository {
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    File file;
    private DBConnector connector;
    private Properties applicationProperties = new Properties();

    public LoginIssueRepository() throws IOException {
        connector = new DBConnector();
        String propFileName = "application.properties";
        file = new File(propFileName).getAbsoluteFile();
        InputStream inputStream = new FileInputStream(file);
        applicationProperties.load(inputStream);
    }

    public List<User> getAllDeactivatedUsers(String filePath) throws FileNotFoundException {
        List<User> users = new ArrayList<User>();
        Scanner fileScanner = new Scanner(new File(filePath));
        while (fileScanner.hasNextLine()) {
            String line = fileScanner.nextLine();
            if (line.trim().contains(" 447")) {
                String[] lineArray = line.split(" 447");
                users.add(new User(lineArray[0].trim(), lineArray[1].trim()));
            }
        }
        fileScanner.close();
        return users;
    }

    public Map<String, List<String>> getImpactedUsers(String mongoPath, List<User> users) throws IOException {
        Map<String, List<String>> finalResult = new HashMap<String, List<String>>();
        MongoCollection personCollection = connector.getPersonCollection(mongoPath);
        MongoCursor mongoUser;
        for (User user : users) {
            mongoUser = personCollection.find(new BasicDBObject("normalisedAssetValue", "+447" + user.msisdn)
                    .append("uid", user.uid).append("assetType", "PAYMMBB")).iterator();
            if (mongoUser.hasNext()) {
                List<String> existingUsers = finalResult.get(user.uid);
                if (existingUsers == null) {
                    existingUsers = new ArrayList<String>();
                    finalResult.put(user.uid, existingUsers);
                }
                existingUsers.add("+447" + user.msisdn);
            }
            mongoUser.close();
        }
        return finalResult;
    }

    public void storeImpactedUsers(String outputFilePath, Map<String, List<String>> impactedUsers) throws IOException {
        File impactedUsersFile = new File(applicationProperties.getProperty("outputFile"));
        FileOutputStream outputFile;
        if (impactedUsersFile.exists()) {
            outputFile = new FileOutputStream(outputFilePath, true);
        } else {
            outputFile = new FileOutputStream(outputFilePath, false);
        }
        PrintWriter outputFileWriter = new PrintWriter(outputFile);
        for (Map.Entry<String, List<String>> impactedUser : impactedUsers.entrySet()) {
            for (String impactedMsisdn : impactedUser.getValue()) {
                outputFileWriter.println(impactedUser.getKey() + "=" + impactedMsisdn);
            }
        }
        outputFileWriter.flush();
        outputFileWriter.close();
        outputFile.close();
    }

    public List<User> readImpactedMsisdnsFromFile(String outputFileToScan) throws IOException {
        List<User> users = new ArrayList<User>();
        StringTokenizer stringTokenizer;
        BufferedReader br = new BufferedReader(new FileReader(outputFileToScan));
        String singleImpactedUser;
        while ((singleImpactedUser = br.readLine()) != null) {
            stringTokenizer = new StringTokenizer(singleImpactedUser, "=");
            users.add(new User(stringTokenizer.nextToken(), stringTokenizer.nextToken()));
        }
        return users;
    }

    public void removeMsisdnsFromFile(String mongoPath, String fileForRecovery, List<User> impactedUsersAndMsisdns) throws IOException {
        FileOutputStream outputFile = new FileOutputStream(fileForRecovery);
        PrintWriter outputFileWriter = new PrintWriter(outputFile);
        MongoCollection personCollection = connector.getPersonCollection(mongoPath);
        Document mongoUser;
        for (User user : impactedUsersAndMsisdns) {
            mongoUser = (Document) personCollection.findOneAndDelete(new BasicDBObject("normalisedAssetValue", user.msisdn)
                    .append("uid", user.uid).append("assetType", "PAYMMBB"));
            if (mongoUser != null) {
                if (mongoUser.containsKey("_id")) {
                    mongoUser.remove("_id");
                }

                outputFileWriter.println("user=" + JSON.serialize(mongoUser));
            }
        }
        outputFileWriter.flush();
        outputFileWriter.close();
        outputFile.close();
    }

    public void recoverDeletedMsisdns(String mongoPath, String fileForRecovery) throws IOException {
        String errorFilePath = applicationProperties.getProperty("errorFilePath");
        FileOutputStream outputFile = new FileOutputStream(errorFilePath);
        PrintWriter outputFileWriter = new PrintWriter(outputFile);
        Scanner fileScanner = new Scanner(new File(fileForRecovery));
        MongoCollection personCollection = connector.getPersonCollection(mongoPath);
        ObjectMapper objectMapper = new ObjectMapper();
        while (fileScanner.hasNextLine()) {
            String line = fileScanner.nextLine();
            String[] lineArray = line.split("user=");
            Document dbObject = objectMapper.readValue(lineArray[1], Document.class);
            try {
                Object createdOn = dbObject.get("createdOn");
                if (createdOn != null) {
                    String createdOnTime = ((Map) createdOn).get("$date").toString();
                    dbObject.put("createdOn", changeMongoDateISODate(createdOnTime));
                }
                Object lastUpdatedOn = dbObject.get("lastUpdatedOn");
                if (lastUpdatedOn != null) {
                    String lastUpdatedOnTime = ((Map) lastUpdatedOn).get("$date").toString();
                    dbObject.put("lastUpdatedOn", changeMongoDateISODate(lastUpdatedOnTime));
                }
                Object assetClaimedOn = dbObject.get("assetClaimedOn");
                if (assetClaimedOn != null) {
                    String assetClaimedOnTime = ((Map) assetClaimedOn).get("$date").toString();
                    dbObject.put("assetClaimedOn", changeMongoDateISODate(assetClaimedOnTime));
                }
                dbObject.put("isRecovered", true);
                try {
                    personCollection.insertOne(dbObject);
                } catch (MongoException exception) {
                    outputFileWriter.println(exception.toString());
                    outputFileWriter.println(dbObject);
                }
            } catch (ParseException parseException) {
                outputFileWriter.println(parseException.toString());
                outputFileWriter.println(dbObject);
            }
        }
        outputFileWriter.flush();
        outputFileWriter.close();
        outputFile.close();
        fileScanner.close();
    }

    private Date changeMongoDateISODate(String stringDate) throws ParseException {
        return formatter.parse(stringDate);
    }
}
