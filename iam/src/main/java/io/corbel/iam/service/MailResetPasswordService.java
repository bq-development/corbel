package io.corbel.iam.service;

import io.corbel.iam.model.User;

public interface MailResetPasswordService {

    void sendMailResetPassword(String clientId, User user, String email, String domainId);
}
