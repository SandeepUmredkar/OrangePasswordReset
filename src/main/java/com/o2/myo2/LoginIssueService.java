package com.o2.myo2;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class LoginIssueService {
    private LoginIssueRepository loginIssueRepository;

    public LoginIssueService(LoginIssueRepository loginIssueRepository) {
        this.loginIssueRepository = loginIssueRepository;
    }

    public Map<String, List<String>> getImpactedUsers(String mongoPath, String orangeResultPath) throws IOException {
        List<User> users = loginIssueRepository.getAllDeactivatedUsers(orangeResultPath);
        return loginIssueRepository.getImpactedUsers(mongoPath, users);
    }

    public void storeImpactedUsers(String outputFilePath, Map<String, List<String>> impactedUsers) throws IOException {
        loginIssueRepository.storeImpactedUsers(outputFilePath, impactedUsers);
    }

    public void removeDeacvtivatedMsisdns(String outputFileToScan, String fileForRecovery, String mongoPath) throws IOException {
        List<User> impactedUsersAndMsisdns = loginIssueRepository.readImpactedMsisdnsFromFile(outputFileToScan);
        loginIssueRepository.removeMsisdnsFromFile(mongoPath, fileForRecovery, impactedUsersAndMsisdns);
    }

    public void recoverDeletedMsisdns(String mongoPath, String fileForRecovery) throws IOException {
        loginIssueRepository.recoverDeletedMsisdns(mongoPath, fileForRecovery);
    }
}