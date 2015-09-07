package io.corbel.oauth.cli.dsl

import io.corbel.lib.cli.console.Description
import io.corbel.lib.cli.console.Shell
import io.corbel.oauth.model.Client
import io.corbel.oauth.model.Role
import io.corbel.oauth.model.User
import io.corbel.oauth.repository.ClientRepository
import io.corbel.oauth.repository.UserRepository
import org.springframework.dao.DataIntegrityViolationException

/**
 * @author Alberto J. Rubio
 */
@Shell("oauth")
class OauthShell {

    ClientRepository clientRepository
    UserRepository userRepository;

    public OauthShell(ClientRepository clientRepository, UserRepository userRepository) {
        this.clientRepository = clientRepository
        this.userRepository = userRepository
    }

    @Description("Creates a new client on the DB. The input parameter is a map containing the client data.")
    def createClient(clientFields) {
        assert clientFields.name: 'Client name is required'
        assert clientFields.redirectRegexp: 'Client redirectRegexp is required'
        assert clientFields.key: 'Client key is required'
        assert clientFields.domain: 'Client domain is required'
        Client client = new Client()
        client.key = clientFields.key
        client.name = clientFields.name
        client.domain = clientFields.domain
        client.redirectRegexp = clientFields.redirectRegexp

        client.resetUrl = clientFields.resetUrl
        client.resetNotificationId = clientFields.resetNotificationId

        if (clientFields.validationEnabled) {
            client.validationEnabled = new Boolean(clientFields.validationEnabled)
        }

        client.validationUrl = clientFields.validationUrl
        client.validationNotificationId = clientFields.validationNotificationId

        client.changePasswordNotificationId = clientFields.changePasswordNotificationId


        clientRepository.save(client)
    }

    @Description("Creates a new user on the DB. The input parameter is a map contaning the user data.")
    def createUser(userFields) {
        assert userFields.username: 'Username is required'
        assert userFields.password: 'Password is required'
        assert userFields.email: 'Email is required'
        assert userFields.domain: 'Domain is required'
        User user = new User()
        if (userFields.id) {
            user.id = userFields.id
        }
        if (userFields.role) {
            user.setRole(Role.valueOf(userFields.role.toUpperCase()))
        } else {
            user.setRole(Role.USER)
        }
        user.setUsername(userFields.username)
        user.setPassword(userFields.password)
        user.setEmail(userFields.email)
        user.setDomain(userFields.domain)
        try {
            userRepository.save(user)
        } catch (DataIntegrityViolationException ignored) {
            print 'User already registered'
        }
    }

    @Description("Delete a user. The input parameter is a map contaning the username")
    def deleteUser(userFields) {
        assert userFields.username: 'Username is required'
        User user = userRepository.findByUsername(userFields.username)
        if (user) {
            userRepository.delete(user)
        } else {
            print 'User not exists'
        }
    }
}
