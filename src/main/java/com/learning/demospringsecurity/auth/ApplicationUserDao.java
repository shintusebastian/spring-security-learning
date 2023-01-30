package com.learning.demospringsecurity.auth;


import java.util.Optional;

//This interface is to load users from any data source.
public interface ApplicationUserDao {
    Optional<ApplicationUser> selectApplicationUserByUsername(String username);
}
