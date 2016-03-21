package com.o2.orange;

import java.io.IOException;

public class PasswordResetController {

    private PasswordResetService passwordResetService;

    public PasswordResetController(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
    }

    public void resetPassword(String filePathForUidsListToResetPassword) throws IOException {
        passwordResetService.resetPassword(filePathForUidsListToResetPassword);
    }
}