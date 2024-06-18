package com.example.notes.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Value("${app.dev-mode:false}")
    private boolean devMode;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        if (devMode) {
            http.authorizeRequests().anyRequest().permitAll();
        } else {
            http
                .authorizeRequests()
                .antMatchers("/", "/login**")
                .permitAll()
                .antMatchers("/notes/**")
                .authenticated()
                .and()
                .oauth2Login()
                .loginPage("/login")
                .defaultSuccessUrl("/notes");
        }
    }
}
