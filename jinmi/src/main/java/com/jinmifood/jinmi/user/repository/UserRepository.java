package com.jinmifood.jinmi.user.repository;

import com.jinmifood.jinmi.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
    Optional<User> findByEmail(String email);
    boolean existsByDisplayName(String displayName);
    Optional<User> findByPhoneNumber(String phoneNumber);



}
