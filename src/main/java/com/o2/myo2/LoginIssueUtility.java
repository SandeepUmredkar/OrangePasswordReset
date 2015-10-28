package com.o2.myo2;

import java.io.IOException;
import java.util.Date;
import java.util.Scanner;

public class LoginIssueUtility {
    public static void main(String args[]) throws IOException {
        System.out.println("Enter Your choice");
        System.out.println("1. For Just figuring out users impacted");
        System.out.println("2. For Just removing impacted users in mongo");
        System.out.println("3. For Just recovering removed users");
        Scanner userOperation = new Scanner(System.in);
        int operationNumber = Integer.parseInt(userOperation.next());
        switch (operationNumber) {
            case 1: {
                System.out.println("start Time for 1 Utility: " + new Date());
                LoginIssueResolverUtility.main(args);
                System.out.println("end Time for 1 Utility: " + new Date());
                break;
            }
            case 2: {
                System.out.println("start Time for 2 Utility: " + new Date());
                LoginIssueUsersRemoverUtility.main(args);
                System.out.println("end Time for 2 Utility : " + new Date());
                break;
            }
            case 3: {
                System.out.println("start Time for 3 Utility: " + new Date());
                LoginIssueUsersRecoveryUtility.main(args);
                System.out.println("end Time for 3 Utility: " + new Date());
                break;
            }
        }
    }
}
