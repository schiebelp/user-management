package cz.demo.usermanagement.repository.impl;

import cz.demo.usermanagement.repository.UserDAO;
import cz.demo.usermanagement.repository.entity.UserEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
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
    public UserEntity save(UserEntity user) {
        log.info("Started save user {} in opened transaction: {} ", user, TransactionSynchronizationManager.isActualTransactionActive());

        entityManager.persist(user);
        return user;
    }

    @Override
    public UserEntity update(UserEntity user) {
        return entityManager.merge(user);
    }

    @Override
    public void deleteById(int id) {
        UserEntity user = entityManager.find(UserEntity.class, id);
        if (user != null) {
            entityManager.remove(user);
        }
    }

    @Override
    public Optional<UserEntity> findById(int id) {
        return Optional.ofNullable(entityManager.find(UserEntity.class, id));
    }

    @Override
    public List<UserEntity> findAll() {

        CriteriaQuery<UserEntity> criteria = entityManager.getCriteriaBuilder().createQuery(UserEntity.class);

        Root<UserEntity> root = criteria.from(UserEntity.class);
        criteria.select(root);

        return entityManager.createQuery(criteria).getResultList();
    }

    @Override
    public Optional<UserEntity> findByUserName(String userName) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();

        CriteriaQuery<UserEntity> criteria = builder.createQuery(UserEntity.class);

        Root<UserEntity> from = criteria.from(UserEntity.class);

        criteria.select(from);

        criteria.where(builder.equal(from.get("userName"), userName));

        try {
            return Optional.ofNullable(entityManager.createQuery(criteria).getSingleResult());
        }
        catch (NoResultException e) {
            return Optional.empty();
        }
    }

}