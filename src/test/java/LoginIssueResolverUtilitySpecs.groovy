package com.o2.myo2

import spock.lang.Specification

class LoginIssueResolverUtilitySpecs extends Specification {

    private Properties applicationProperties;

    public setup() {
        applicationProperties = new Properties();
        String propFileName = "application.properties";
        InputStream inputStream = new FileInputStream(propFileName);
        applicationProperties.load(inputStream);
    }

    //The msisdns and UIDs and taken from the file specified in application.properties and mongo also is the one configured in local.proeperties
    public void itShouldGetImpactedUsersInFile() {
        setup:
        LoginIssueRepository loginIssueRepository = new LoginIssueRepository();
        LoginIssueService loginIssueService = new LoginIssueService(loginIssueRepository);
        LoginIssueController loginIssueController = new LoginIssueController(loginIssueService);

        when:
        Map<String, List<String>> impactedUsers = loginIssueController.getImpactedUsers(applicationProperties.getProperty("mongo.connectionUrl"), "testFile");

        then:
        impactedUsers.get("13000000000013017106").get(0) == "+447507833288"
        impactedUsers.get("13000000000012523386").get(0) == "+447517320334"
    }

    //The msisdns and UIDs and taken from the file specified in application.properties and mongo also is the one configured in local.proeperties
    public void itShouldStoreImpactedUsersInFile() {
        setup:
        LoginIssueRepository loginIssueRepository = new LoginIssueRepository();
        LoginIssueService loginIssueService = new LoginIssueService(loginIssueRepository);
        LoginIssueController loginIssueController = new LoginIssueController(loginIssueService);
        Map<String, List<String>> impactedUsersMap = new HashMap<>();
        def firstList = new ArrayList()

        firstList.add("+447507833288")
        impactedUsersMap.put("13000000000013017106", firstList);
        def secondList = new ArrayList();
        secondList.add("+447516624505")
        impactedUsersMap.put("13000000000012674537", secondList);
        Scanner outputFileScanner = new Scanner(new File("outputFileForTest"));

        when:
        loginIssueController.storeImpactedusers("outputFileForTest", impactedUsersMap);

        then:
        String firstMsisdnAndUidLine = outputFileScanner.nextLine()
        firstMsisdnAndUidLine.contains("13000000000013017106")
        firstMsisdnAndUidLine.contains("+447507833288")
        String secondMsisdnAndUidLine = outputFileScanner.nextLine()
        secondMsisdnAndUidLine.contains("13000000000012674537")
        secondMsisdnAndUidLine.contains("+447516624505")
    }
}
