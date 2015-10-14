package io.corbel.oauth.repository.decorator;

import java.util.List;

import io.corbel.oauth.model.User;
import io.corbel.oauth.repository.UserRepository;

/**
 * @author Francisco Sanchez
 */
public class LowerCaseDecorator extends UserRepositoryDecorator {

    public LowerCaseDecorator(UserRepository decoratedUserRepository) {
        super(decoratedUserRepository);
    }

    @Override
    public User save(User data) {
        return super.save(userToLowerCase(data));
    }

    @Override
    public <S extends User> List<S> save(Iterable<S> ses) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public User findOne(String s) {
        return super.findOne(s.toLowerCase());
    }

    @Override
    public Iterable<User> findAll(Iterable<String> strings) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public User findByUsername(String username) {
        return super.findByUsername(username.toLowerCase());
    }

    @Override
    public User findByEmail(String email) {
        return super.findByEmail(email.toLowerCase());
    }

    @Override
    public User findById(String id) {
        return super.findById(id);
    }

    @Override
    public User findByEmailAndDomain(String email, String domain) {
        return super.findByEmailAndDomain(email.toLowerCase(), domain);
    }

    @Override
    public User findByUsernameAndDomain(String username, String domain) {
        return super.findByUsernameAndDomain(username.toLowerCase(), domain);
    }

    @Override
    public boolean exists(String s) {
        return super.exists(s.toLowerCase());
    }

    @Override
    public void delete(String s) {
        super.delete(s.toLowerCase());
    }

    @Override
    public void delete(User data) {
        super.delete(userToLowerCase(data));
    }

    @Override
    public void delete(Iterable<? extends User> users) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public boolean patch(String id, User data, String... fieldsToDelete) {
        return super.patch(id, userToLowerCase(data), fieldsToDelete);
    }

    @Override
    public boolean patch(User data, String... fieldsToDelete) {
        return super.patch(userToLowerCase(data), fieldsToDelete);
    }

    @Override
    public boolean upsert(String id, User data) {
        return super.upsert(id, userToLowerCase(data));
    }

    private User userToLowerCase(User data) {
        if (data.getUsername() != null) {
            data.setUsername(data.getUsername().toLowerCase());
        }
        if (data.getEmail() != null) {
            data.setEmail(data.getEmail().toLowerCase());
        }
        return data;
    }

}
