package com.learning.demospringsecurity.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class PasswordConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        //BCryptPasswordEncoder is the most popular password encoder out there.
        return new BCryptPasswordEncoder(10);

        //without BCrypt, if we try to run the application, it will show
        //Encoded password does not look like BCrypt in the console.
    }
}
