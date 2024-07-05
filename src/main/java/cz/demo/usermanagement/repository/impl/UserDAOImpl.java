package cz.demo.usermanagement.repository.impl;

import cz.demo.usermanagement.repository.UserDAO;
import cz.demo.usermanagement.model.UserEntity;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

@Repository
public class UserDAOImpl implements UserDAO {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public void save(UserEntity user) {
        entityManager.persist(user);
    }

    @Override
    @Transactional
    public void update(UserEntity user) {
        entityManager.merge(user);
    }

    @Override
    @Transactional
    public void delete(int id) {
        UserEntity user = entityManager.find(UserEntity.class, id);
        if (user != null) {
            entityManager.remove(user);
        }
    }

    @Override
    public UserEntity findById(int id) {
        return entityManager.find(UserEntity.class, id);
    }

    @Override
    public List<UserEntity> findAll() {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        CriteriaQuery<UserEntity> cq = cb.createQuery(UserEntity.class);

        Root<UserEntity> root = cq.from(UserEntity.class);
        cq.select(root);

        return entityManager.createQuery(cq).getResultList();
    }

}