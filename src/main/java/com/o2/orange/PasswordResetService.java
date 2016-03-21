package com.o2.orange;

import java.io.FileNotFoundException;
import java.util.List;

public class PasswordResetService {
    private PasswordResetRepository passwordResetRepository;

    public PasswordResetService(PasswordResetRepository passwordResetRepository) {
        this.passwordResetRepository = passwordResetRepository;
    }

    public void resetPassword(String filePathForUidsListToResetPassword) throws FileNotFoundException {
        List<String> uidsForPasswordReset = passwordResetRepository.getUidsForPasswordReset(filePathForUidsListToResetPassword);
        passwordResetRepository.resetPassword(uidsForPasswordReset);
    }
}