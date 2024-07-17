package cz.demo.usermanagement.repository.impl;

import cz.demo.usermanagement.repository.UserDAO;
import cz.demo.usermanagement.repository.entity.User;
import cz.demo.usermanagement.repository.enums.ROLE;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Root;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.Optional;


@Repository
@Slf4j
public class UserDAOImpl implements UserDAO {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public User save(User user) {
        log.info("Started save user {} in opened transaction: {} ", user, TransactionSynchronizationManager.isActualTransactionActive());

        entityManager.persist(user);
        return user;
    }

    @Override
    public User update(User user) {
        return entityManager.merge(user);
    }

    @Override
    public void deleteById(int id) {
        User user = entityManager.find(User.class, id);
        if (user != null) {
            entityManager.remove(user);
        }
    }

    @Override
    public Optional<User> findById(int id) {
        return Optional.ofNullable(entityManager.find(User.class, id));
    }

    @Override
    public List<User> findAll() {

        CriteriaQuery<User> criteria = entityManager.getCriteriaBuilder().createQuery(User.class);

        Root<User> root = criteria.from(User.class);
        criteria.select(root);

        return entityManager.createQuery(criteria).getResultList();
    }

    @Override
    public Optional<User> findByUserName(String userName) {
        return findByUserName(userName, false);
    }


    @Override
    public Optional<User> findByUserName(String userName, boolean fetchRoles) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();

        CriteriaQuery<User> criteria = builder.createQuery(User.class);

        Root<User> from = criteria.from(User.class);

        if (fetchRoles) {
            // Perform a fetch join to load roles along with the user
            from.fetch("roles", JoinType.LEFT);
        }

        criteria.select(from);

        criteria.where(builder.equal(from.get("userName"), userName));

        try {
            return Optional.ofNullable(entityManager.createQuery(criteria).getSingleResult());
        }
        catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<User> findByRole(ROLE role) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<User> criteria = builder.createQuery(User.class);
        Root<User> root = criteria.from(User.class);

        criteria.select(root)
                .where(builder.equal(root.join("roles").get("name"), role));

        return entityManager.createQuery(criteria).getResultList();
    }

}