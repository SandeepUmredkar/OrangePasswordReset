package com.o2.orange;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.json.JSONException;

public class PasswordResetService {
    private PasswordResetRepository passwordResetRepository;

    public PasswordResetService(PasswordResetRepository passwordResetRepository) {
        this.passwordResetRepository = passwordResetRepository;
    }

    public void resetPassword(String filePathForUidsListToResetPassword) throws JSONException, IOException {
        List<String> uidsForPasswordReset = passwordResetRepository.getUidsForPasswordReset(filePathForUidsListToResetPassword);
        passwordResetRepository.resetPassword(uidsForPasswordReset);
    }
}