package cz.demo.usermanagement.service;

import cz.demo.usermanagement.service.domain.User;

import java.util.List;

/**
 * BUS vrstva manipulace s uzivatelem
 */
public interface UserService {

    /**
     * Zalozeni noveho uzivatele se zakodovanym heslem
     *
     * @param user uziv.
     * @return novy uziv.
     */
    User createUser(User user);

    /**
     * Update uzivatele s kontrolou opravneni - majitel zaznamu ci admin
     *
     * @param user k aktualizaci
     * @param loggedUserName prihlaseny uziv.
     */
    void updateUser( User user, String loggedUserName);

    /**
     * Vrati vsechny uziv.
     *
     * @return all
     */
    List<User> getAllUsers();

    /**
     * Nalezne uzivatele
     *
     * @param userId id
     * @return uziv.
     */
    User getUserById(Long userId);

    /**
     * Vymaze uzivatele
     *
     * @param id id
     */
    void deleteUser(Long id);

}
