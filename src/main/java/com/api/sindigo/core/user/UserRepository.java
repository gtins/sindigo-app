package com.api.sindigo.core.user;


import com.api.sindigo.core.user.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {


}
