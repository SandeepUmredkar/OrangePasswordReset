package com.o2.myo2;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class LoginIssueController {

    private LoginIssueService loginIssueService;

    public LoginIssueController(LoginIssueService loginIssueService) {
        this.loginIssueService = loginIssueService;
    }

    public Map<String, List<String>> getImpactedUsers(String mongoPath, String orangeResultPath) throws IOException {
        return loginIssueService.getImpactedUsers(mongoPath, orangeResultPath);
    }

    public void storeImpactedusers(String outputFilePath, Map<String, List<String>> impactedUsers) throws IOException {
        loginIssueService.storeImpactedUsers(outputFilePath, impactedUsers);
    }

    public void removeDeacvtivatedMsisdns(String outputFileToScan, String fileForRecovery, String mongoPath) throws IOException {
        loginIssueService.removeDeacvtivatedMsisdns(outputFileToScan, fileForRecovery, mongoPath);
    }

    public void recoverDeletedMsisdns(String mongoPath, String fileForRecovery) throws IOException {
        loginIssueService.recoverDeletedMsisdns(mongoPath, fileForRecovery);
    }
}