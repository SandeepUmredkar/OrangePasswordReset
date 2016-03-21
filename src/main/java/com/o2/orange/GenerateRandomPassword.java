package com.o2.orange;

import java.util.Vector;

public class GenerateRandomPassword {
    protected static Vector passwordChars = null;

    public static String generateRandomPassword(int numChars) {
        StringBuffer randomPassword = new StringBuffer();

        // Get the characters to be used in creating the password
        Vector charsForPassword = getRandomPasswordChars();

        // Generate the random password
        for (int iter = 0; iter < numChars; iter++) {
            Character chosenChar = (Character) charsForPassword.get((int) (Math.random() * charsForPassword.size()));
            //Character chosenChar = charsForPassword[(int) (Math.random() * charsForPassword.length)];
            randomPassword.append(chosenChar);
        }

        return randomPassword.toString();
    }

    protected static Vector getRandomPasswordChars() {
        if (passwordChars != null) {
            // passwordChars has already been populated
            return passwordChars;
        } else {
            // Create the array of chars to use in the password
            passwordChars = new Vector();
            // Load the alphachars into the list of password charcters
            for (char pwdChar = 'A'; pwdChar <= 'Z'; pwdChar++) {
                if (pwdChar == 'I' || pwdChar == 'O' || pwdChar == 'U' || pwdChar == 'V' || pwdChar == 'Z' || pwdChar == 'L') {
                    // Skip these as this are restricted
                } else {
                    // Add the char to the vector
                    Character pwdCharacter = new Character(pwdChar);
                    passwordChars.add(pwdCharacter);
                }
            }

            // Load the numeric chars into the list of password charcters
            for (char pwdChar = '3'; pwdChar <= '9'; pwdChar++) {
                Character pwdCharacter = new Character(pwdChar);
                passwordChars.add(pwdCharacter);
            }

            // Return the populated array of Password Chars
            return passwordChars;
        }
    }
}
