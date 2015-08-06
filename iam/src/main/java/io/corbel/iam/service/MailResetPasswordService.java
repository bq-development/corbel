package io.corbel.iam.service;

public interface MailResetPasswordService {

    void sendMailResetPassword(String clientId, String userId, String email, String domainId);
}
