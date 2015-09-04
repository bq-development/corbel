package io.corbel.oauth.service;

import io.corbel.oauth.model.Client;

/**
 * @author Alberto J. Rubio
 */
public interface MailValidationService {

    void sendMailValidation(Client client, String userId, String email);

}
