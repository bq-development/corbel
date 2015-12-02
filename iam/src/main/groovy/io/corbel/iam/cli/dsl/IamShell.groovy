package io.corbel.iam.cli.dsl

import io.corbel.iam.model.*
import io.corbel.iam.repository.*
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import io.corbel.lib.cli.console.Description
import io.corbel.lib.cli.console.Shell
import net.oauth.jsontoken.JsonToken
import net.oauth.jsontoken.crypto.HmacSHA256Signer
import org.bouncycastle.util.encoders.Base64
import org.joda.time.Instant

import java.util.regex.Pattern

/**
 * @author Alexander De Leon
 *
 */
@Shell("iam")
class IamShell {

    ClientRepository clientRepository
    ScopeRepository scopeRepository
    UserRepository userRepository
    DomainRepository domainRepository
    IdentityRepository identityRepository
    String iamUri
    GroupRepository groupRepository

    public IamShell(ClientRepository clientRepository, ScopeRepository scopeRepository, UserRepository userRepository,
                    DomainRepository domainRepository, String iamUri, IdentityRepository identityRepository, GroupRepository groupRepository) {
        this.clientRepository = clientRepository
        this.scopeRepository = scopeRepository
        this.userRepository = userRepository
        this.domainRepository = domainRepository
        this.iamUri = iamUri
        this.identityRepository = identityRepository
        this.groupRepository = groupRepository
    }

    @Description("Creates a new domain on the DB. The input parameter is a map containing the domain data.")
    def createDomain(domainFields) {
        assert domainFields.id: 'Domain id is required'
        Domain domain = new Domain()
        domain.id = domainFields.id
        domain.scopes = domainFields.scopes
        domain.publicScopes = domainFields.publicScopes
        domain.defaultScopes = domainFields.defaultScopes
        domain.description = domainFields.description
        domain.authConfigurations = domainFields.authConfigurations
        domain.allowedDomains = domainFields.allowedDomains
        if (domainFields.userProfileFields) {
            domain.userProfileFields = new HashSet<>(domainFields.userProfileFields)
        }
        if(domainFields.capabilities) {
            domain.capabilities = domainFields.capabilities
        }
        addTrace(domain)
        domainRepository.save(domain)
    }

    @Description("Creates a new client on the DB. The input parameter is a map containing the client data. The created client is returned.")
    def createClient(clientFields) {
        assert clientFields.name: 'Client name is required'
        assert clientFields.domain: 'Client domain is required'
        assert clientFields.key: 'Client key is required'
        Client client = new Client()
        client.name = clientFields.name
        client.domain = clientFields.domain
        client.key = clientFields.key
        client.version = clientFields.version
        client.clientSideAuthentication = clientFields.clientSideAuthentication
        client.resetNotificationId = clientFields.resetNotificationId

        if (clientFields.resetUrl) {
            client.resetUrl = clientFields.resetUrl
        }

        if (clientFields.scopes) {
            client.scopes = new HashSet<>(clientFields.scopes)
        }
        client.signatureAlgorithm = clientFields.signatureAlgorithm ?: SignatureAlgorithm.HS256
        addTrace(client)
        clientRepository.save(client)
    }

    @Description('Created a new scope or overwrites an existing one. Takes a id, the audience, of the scope (e.g. music:catalog:read) and all the rules to include. Rules are writen as maps that represent the json object')
    def createScope(String scopeId, String audience, Map rule, Map parameters = [:]) {
        assert scopeId: "scopeId is required"
        assert audience: "audience is required"
        assert rule: "rule is required"
        Gson gson = getGson()
        //Parse rule Map
        def element = gson.toJsonTree(rule)
        def uri = element.asJsonObject.get("uri")?.getAsString()
        Pattern pattern = null
        try {
            pattern = Pattern.compile(uri.replaceAll("\\{\\{[\\w\\-:\\.]+\\}\\}", "0"))
        } catch (ignored) {
        }
        assert pattern: "uri with value '${uri}' is wrong"

        def rules = new HashSet()
        rules.add(element)
        def jsonParameters = null
        if (parameters) {
            jsonParameters = gson.toJsonTree(parameters)
        }
        Scope scope = new Scope(scopeId, null, audience, null, rules, jsonParameters)

        scopeRepository.save(scope)
    }

    @Description('Created a new composite scope or overwrites an existing one. Takes a id, and the lists of the scopes to include.')
    def createCompositeScope(String scopeId, String... scopes) {
        assert scopeId: "scopeId is required"
        assert scopes: "scopes is required"
        Scope scope = new Scope(scopeId, Scope.COMPOSITE_SCOPE_TYPE, null, new HashSet<Scope>(scopes.toList()), null, null)
        scopeRepository.save(scope)
    }

    @Description('Created a new user or overwrites an existing one. The input parameter is a map contaning the user data. The created user is returned.')
    def createUser(userFields) {
        assert userFields.username: "username is required"
        assert userFields.domain: "domain is required"
        assert userFields.email: "email is required"

        User userUsername = userRepository.findByUsernameAndDomain(userFields.username, userFields.domain)
        User userEmail = userRepository.findByDomainAndEmail(userFields.domain, userFields.email)

        User user
        if (!userUsername && !userEmail) {
            user = new User()
            user.setId(userFields.id)
        } else {
            user = userUsername ? userUsername : userEmail
        }

        user.setDomain(userFields.domain)
        user.setEmail(userFields.email)
        user.setFirstName(userFields.firstName)
        user.setLastName(userFields.lastName)
        user.setPhoneNumber(userFields.phoneNumber)
        user.setProfileUrl(userFields.profileUrl)
        user.setProperties(userFields.properties)
        user.setScopes(userFields.scopes)
        user.setUsername(userFields.username)
        user.setPassword(userFields.password)
        addTrace(user)
        userRepository.save(user)
    }

    @Description('Adds the specified scopes to the specified client')
    def addScopeToClient(String client, String... scopes) {
        clientRepository.addScopes(client, scopes)
    }

    @Description('Remove the specified scopes to the specified client')
    def removeScopeFromClient(String client, String... scopes) {
        clientRepository.removeScopes(client, scopes)
    }

    @Description('Adds the specified scopes to the specified user')
    def addScopeToUser(String user, String... scopes) {
        userRepository.addScopes(user, scopes)
    }

    @Description('Remove the specified scopes to the specified user')
    def removeScopeFromUser(String user, String... scopes) {
        userRepository.removeScopes(user, scopes)
    }

    @Description('Adds the specified scopes to the specified domain')
    def addScopeToDomain(String domain, String... scopes) {
        domainRepository.addScopes(domain, scopes)
    }

    @Description('Remove the specified scopes to the specified domain')
    def removeScopeFromDomain(String domain, String... scopes) {
        domainRepository.removeScopes(domain, scopes)
    }

    @Description('Adds the specified default scopes to the specified domain')
    def addDefaultScopesToDomain(String domain, String... scopes) {
        domainRepository.addDefaultScopes(domain, scopes)
    }

    @Description('Remove the specified default scopes to the specified domain')
    def removeDefaultScopesFromDomain(String domain, String... scopes) {
        domainRepository.removeDefaultScopes(domain, scopes)
    }

    @Description('Adds the specified public scopes to the specified domain')
    def addPublicScopesToDomain(String domain, String... scopes) {
        domainRepository.addPublicScopes(domain, scopes)
    }

    @Description('Remove the specified public scopes to the specified domain')
    def removePublicScopesFromDomain(String domain, String... scopes) {
        domainRepository.removePublicScopes(domain, scopes)
    }

    @Description("Generate a JWT for the specified client. Optionally a userId can be specified if the client intention is to act on behalf of a user.")
    def jwt(String clientId, List<String> scopes) {
        Client client = clientRepository.findOne(clientId)
        JsonToken jwt = new JsonToken(new HmacSHA256Signer(clientId, null, base64Decode(client.key)))
        jwt.setAudience(iamUri)
        jwt.setParam(JsonToken.ISSUER, clientId)
        jwt.setExpiration(Instant.now().withDurationAdded(3600000, 1))
        jwt.setParam("scope", scopes.join(" "))
        jwt.serializeAndSign()
    }

    @Description('Create a new identity or overwrites an existing one. The input parameter is a map contaning the identity data. The created identity is returned.')
    def createIdentity(identityFields) {
        assert identityFields.domain: "domain is required"
        assert identityFields.userId: "userId is required"
        assert identityFields.oauthService: "oauthService is required"
        assert identityFields.oauthId: "oauthId is required"

        Identity identity = identityRepository.findByOauthIdAndDomainAndOauthService(identityFields.oauthId, identityFields.domain, identityFields.oauthService)
        if (!identity) {
            identity = new Identity()
        }

        identity.setDomain(identityFields.domain)
        identity.setUserId(identityFields.userId)
        identity.setOauthService(identityFields.oauthService)
        identity.setOauthId(identityFields.oauthId)
        identityRepository.save(identity)
    }

    @Description('Create a new users group')
    def createGroup(groupFields) {
        assert groupFields.name: 'name is required'
        assert groupFields.domain: 'domain is required'

        groupRepository.save(new Group(null, groupFields.name, groupFields.domain, groupFields.scopes))
    }

    @Description('Remove a users group')
    def removeGroup(String id) {
        groupRepository.delete(id)
    }

    @Description('Add scopes to a group')
    def addScopeToGroup(String id, String... scopes) {
        groupRepository.addScopes(id, scopes)
    }

    @Description('Remove scopes to a group')
    def removeScopeFromGroup(String id, String... scopes) {
        groupRepository.removeScopes(id, scopes)
    }

    def scope(String id) {
        scopeRepository.findOne(id)
    }

    @Description("Decode the specified BASE64 string into bytes")
    def base64Decode(String string) {
        Base64.decode(string)
    }

    private Gson getGson() {
        GsonBuilder builder = new GsonBuilder()
        builder.create()
    }

    private void addTrace(entity) {
        entity.createdBy = "IamSell on ${Inet4Address.getLocalHost().getHostName()}"
        entity.createdDate = new Date()
    }
}

