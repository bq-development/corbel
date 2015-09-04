package io.corbel.oauth.service;

import io.corbel.oauth.model.Client;

/**
 * @author Francisco Sanchez
 */
public interface MailChangePasswordService {

    void sendMailChangePassword(Client client, String username, String email);
}
