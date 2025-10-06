package com.example.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsPasswordService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.sql.DataSource;
import java.security.Principal;
import java.util.Map;

import static org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer.authorizationServer;

@SpringBootApplication
public class AuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class, args);
    }

//    @Bean
//    Customizer <HttpSecurity > httpSecurityCustomizer() {
//        return new Customizer<HttpSecurity>() {
//            @Override
//            public void customize(HttpSecurity httpSecurity) {
//                 httpSecurity.authorizeHttpRequests(a -> a.anyRequest().authenticated());
//            }
//        } ;
//    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .with(authorizationServer(), as -> as.oidc(Customizer.withDefaults()))
                .httpBasic(Customizer.withDefaults())
                .formLogin(Customizer.withDefaults())
                .webAuthn(a -> a
                        .rpId("localhost")
                        .rpName("devoxx")
                        .allowedOrigins("http://localhost:9090")
                )
                .oneTimeTokenLogin(ott -> ott
                        .tokenGenerationSuccessHandler((_, response, oneTimeToken) -> {
                            response.getWriter().println("you've got console mail!");
                            response.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE);
                            IO.println("please go to http://localhost:9090/login/ott?token=" + oneTimeToken.getTokenValue());
                        }))
                .authorizeHttpRequests(a -> a.anyRequest().authenticated())
                .build();
    }


    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    UserDetailsPasswordService userDetailsPasswordService(JdbcUserDetailsManager jdbcUserDetailsManager) {
        return (user, newPassword) -> {
            var updated = User.withUserDetails(user).password(newPassword).build();
            jdbcUserDetailsManager.updateUser(updated);
            return updated;
        };
    }

    @Bean
    JdbcUserDetailsManager jdbcUserDetailsManager(DataSource dataSource) {
        return new JdbcUserDetailsManager(dataSource);
    }

//
//    @Bean
//    InMemoryUserDetailsManager inMemoryUserDetailsManager(PasswordEncoder passwordEncoder) {
//        var pw1 = passwordEncoder.encode("pw");
//        var pw2 = passwordEncoder.encode("pw");
//        IO.println(pw1 + System.lineSeparator() + pw2);
//        var users = Set.of(
//                User.withUsername("josh").password(pw1).roles("USER").build(),
//                User.withUsername("james").password(pw2).roles("ADMIN", "USER").build()
//        );
//        return new InMemoryUserDetailsManager(users);
//    }

}

// authentication
// authorization


@Controller
@ResponseBody
class MeController {

    @GetMapping("/")
    Map<String, String> me(Principal principal) {
        return Map.of("name", principal.getName());
    }
}