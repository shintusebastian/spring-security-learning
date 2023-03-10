package com.learning.demospringsecurity.security;

import com.learning.demospringsecurity.auth.ApplicationUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.concurrent.TimeUnit;

import static com.learning.demospringsecurity.security.ApplicationUserRole.STUDENT;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true) //we enabled the annotation based authentication.
public class ApplicationSecurityConfig extends WebSecurityConfigurerAdapter {

    private final PasswordEncoder passwordEncoder;
    private final ApplicationUserService applicationUserService;

    @Autowired
    public ApplicationSecurityConfig(PasswordEncoder passwordEncoder,
                                     ApplicationUserService applicationUserService) {
        this.passwordEncoder = passwordEncoder;
        this.applicationUserService = applicationUserService;
    }

    //here we override the configure method of the WebSecurityConfigurerAdapter class.
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        //here, we are overriding the super method and creating our own.
        http.csrf()
//                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()) //This means that client side
//                //cannot access the token generated by the server.
//                .and()
                //spring security by default will try to protect our apis.
                //  .csrf().disable() //Very important concept.
                .disable() //we are using postman throughout the application, so we have disabled csrf now.

                .authorizeRequests()

                //here we specify the pages we need to whitelist.
                .antMatchers("/", "index", "/css/*", "/js/*").permitAll()
                //we are using a second antMatcher to secure our api so that only student can access it.
                .antMatchers("/api/**").hasRole(STUDENT.name())
                //This is called role-based authentication. After we define role based authentication for student,
                //users having admin role cannot access this api.

                //NOTE: Below, we have defined how to implement permission/authority based authentication.
//                .antMatchers(HttpMethod.DELETE, "/management/api/**").hasAuthority(COURSE_WRITE.getPermission())
//                .antMatchers(HttpMethod.POST, "/management/api/**").hasAuthority(COURSE_WRITE.getPermission())
//                .antMatchers(HttpMethod.PUT, "/management/api/**").hasAuthority(COURSE_WRITE.getPermission())
//                .antMatchers(HttpMethod.GET, "/management/api/**").hasAnyRole(ADMIN.name(), ADMINTRAINEE.name())
                //Now: we have replaced all the above antMatchers with PreAuthorize annotation

                //Here, the management/api/** get requests can be accessed by the users having roles as
                // ADMIN and ADMINTRAINEE. But, the other requests such as Post, Put and Delete requests can be accessed
                //only by the user having authority COURSE_WRITE.
//NOTE: the order of these matchers are very important.

                .anyRequest()
                .authenticated()
                .and()
//                .httpBasic();  Basic Authentication.

                // Form based Authentication. We just changed from basic to form based in a single line.
                .formLogin()

                //customizing the login page generated by spring security.
                .loginPage("/login")
                .permitAll() //without permitting we cannot access the login page.

                //Here, we define that the default success url page should be "/courses". True means that
                //it will be force redirected to the courses page.
                .defaultSuccessUrl("/courses", true)
                .passwordParameter("password")
                .usernameParameter("username")
                //we can extend the session id to 2 weeks by using remember me feature of spring security.
                .and()
                .rememberMe()//defaults to 2 weeks which we can change to any number of days we want.
                .rememberMeParameter("remember-me")
                .tokenValiditySeconds((int) TimeUnit.DAYS.toSeconds(21)) //here we have set the default of 2 weeks
                //into 21 days.
                .key("somethingVerySecured") //this is the key which is used to hash the contents(That is the username and
                //the expiration date). This is the key used to generate the md5. Instead of providing the default key given by
                // spring security, we are using this key.

                //here we are going to customize the logout page.
                .and()
                .logout()
                .logoutUrl("/logout")
                .clearAuthentication(true) //to clear the authentication
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID", "remember-me")//deleting the cookies
                .logoutSuccessUrl("/login");

    }

//    @Override
//    @Bean
//    //This UserDetailsService is how we retrieve our users from the database.
//    protected UserDetailsService userDetailsService() {
//        //ideally, the user details should be coming from the database.
//
//        UserDetails shintuUser = User.builder()
//                .username("shintu")
//                //if we try to log in using this password, it won't work. Because, the password
//                //must be encoded.
//                .password(passwordEncoder.encode("password"))
//                //After we encode the password, we can log in using the password we have provided (ie, "password")
////                .roles(STUDENT.name()) //ROLE_STUDENT
//
//                .authorities(STUDENT.getGrantedAuthorities())
//                .build();
//
//
//        UserDetails joelUser = User.builder()
//                .username("joel")
//                .password(passwordEncoder.encode("password123"))
////                .roles(ADMIN.name())//ROLE_ADMIN
//
//                .authorities(ADMIN.getGrantedAuthorities())
//                .build();
//
//        UserDetails tomUser = User.builder()
//                .username("tom")
//                .password(passwordEncoder.encode("password123"))
////                .roles(ADMINTRAINEE.name())//ROLE_ADMINTRAINEE
//
//                .authorities(ADMINTRAINEE.getGrantedAuthorities())
//                .build();
//
//        //there are 6 classes which implements the UserDetails interface. we choose InMemoryUserDetailsManager this time.
//        return new InMemoryUserDetailsManager(shintuUser, joelUser, tomUser);
//    }

    //Now, we are going to use the UserDetailsService we have created.


    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(daoAuthenticationProvider());
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(passwordEncoder);
        provider.setUserDetailsService(applicationUserService);
        return provider;
    }
}
