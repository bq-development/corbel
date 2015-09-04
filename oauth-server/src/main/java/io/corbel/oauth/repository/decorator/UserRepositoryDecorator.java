package io.corbel.oauth.repository.decorator;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import io.corbel.oauth.model.User;
import io.corbel.oauth.repository.UserRepository;

/**
 * @author Francisco Sanchez
 */
public abstract class UserRepositoryDecorator implements UserRepository {

    protected UserRepository decoratedUserRepository;

    public UserRepositoryDecorator(UserRepository decoratedUserRepository) {
        this.decoratedUserRepository = decoratedUserRepository;
    }

    @Override
    public User findByUsername(String username) {
        return decoratedUserRepository.findByUsername(username);
    }

    @Override
    public User findByEmail(String email) {
        return decoratedUserRepository.findByEmail(email);
    }

    @Override
    public User findById(String id) {
        return decoratedUserRepository.findById(id);
    }

    @Override
    public User save(User s) {
        return decoratedUserRepository.save(s);
    }

    @Override
    public <S extends User> List<S> save(Iterable<S> ses) {
        return decoratedUserRepository.save(ses);
    }

    @Override
    public User findOne(String s) {
        return decoratedUserRepository.findOne(s);
    }

    @Override
    public boolean exists(String s) {
        return decoratedUserRepository.exists(s);
    }

    @Override
    public List<User> findAll() {
        return decoratedUserRepository.findAll();
    }

    @Override
    public Iterable<User> findAll(Iterable<String> strings) {
        return decoratedUserRepository.findAll(strings);
    }

    @Override
    public long count() {
        return decoratedUserRepository.count();
    }

    @Override
    public void delete(String s) {
        decoratedUserRepository.delete(s);
    }

    @Override
    public void delete(User user) {
        decoratedUserRepository.delete(user);
    }

    @Override
    public void delete(Iterable<? extends User> users) {
        decoratedUserRepository.delete(users);
    }

    @Override
    public void deleteAll() {
        decoratedUserRepository.deleteAll();
    }

    @Override
    public User findByEmailAndDomain(String email, String domain) {
        return decoratedUserRepository.findByEmailAndDomain(email, domain);
    }

    @Override
    public User findByUsernameAndDomain(String username, String domain) {
        return decoratedUserRepository.findByUsernameAndDomain(username, domain);
    }

    @Override
    public boolean patch(String id, User data, String... fieldsToDelete) {
        return decoratedUserRepository.patch(id, data, fieldsToDelete);
    }

    @Override
    public boolean patch(User data, String... fieldsToDelete) {
        return decoratedUserRepository.patch(data, fieldsToDelete);
    }

    @Override
    public Page<User> findAll(Pageable pageable) {
        return decoratedUserRepository.findAll(pageable);
    }

    @Override
    public List<User> findAll(Sort sort) {
        return decoratedUserRepository.findAll(sort);
    }

}
