package io.corbel.oauth.service;

import io.corbel.oauth.model.Client;

/**
 * @author Alberto J. Rubio
 */
public interface MailResetPasswordService {

    void sendMailResetPassword(Client client, String userId, String email);
}
