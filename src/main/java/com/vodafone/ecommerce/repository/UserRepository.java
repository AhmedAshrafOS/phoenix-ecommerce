package com.vodafone.ecommerce.repository;

import com.vodafone.ecommerce.model.entity.User;
import com.vodafone.ecommerce.model.enums.Role;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends BaseRepository<User, Long> {

    boolean existsByUsernameIgnoreCase(String username);

    boolean existsByEmailIgnoreCase(String email);

    @Query("SELECT u FROM User u WHERE LOWER(u.username) = LOWER(:usernameOrEmail) OR LOWER(u.email) = LOWER(:usernameOrEmail)")
    Optional<User> findByUsernameOrEmailIgnoreCase(String usernameOrEmail);

    User findByEmailIgnoreCase(String emailId);

    List<User> findAllByRoleIn(List<Role> roles);
}
