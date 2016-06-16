package net.nemerosa.ontrack.boot.support;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.rememberme.InMemoryTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenBasedRememberMeServices;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private APIBasicAuthenticationEntryPoint apiBasicAuthenticationEntryPoint;

    @Autowired
    private AuthenticationManager authenticationManager;

    /**
     * By default, all queries are accessible anonymously. Security is enforced at service level.
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // FIXME Gets a secure random key
        String rememberBeKey = "...";
        // @formatter:off
        http.antMatcher("/**")
            // Only BASIC authentication
            .httpBasic()
                .authenticationEntryPoint(apiBasicAuthenticationEntryPoint)
                .realmName("ontrack")
                .and()
            // Logout set-up
            .logout()
                .logoutUrl("/user/logout")
                .logoutSuccessUrl("/user/logged-out")
                .and()
            // FIXME CSRF protection for a stateless API?
            //.csrf().requireCsrfProtectionMatcher(new CSRFRequestMatcher()).and()
            .csrf().disable()
            // Allows all at Web level
            .authorizeRequests()
                .antMatchers("/**").permitAll()
                .anyRequest().authenticated()
            // Remember be authentication token
            .and().rememberMe()
                .rememberMeServices(rememberMeServices(rememberBeKey))
                .key(rememberBeKey)
                .tokenValiditySeconds(604800)
            // Cache enabled
            .and().headers().cacheControl().disable()
        ;
        // @formatter:on
    }

    @Bean
    public RememberMeServices rememberMeServices(String rememberBeKey) {
        InMemoryTokenRepositoryImpl rememberMeTokenRepository = new InMemoryTokenRepositoryImpl();
        PersistentTokenBasedRememberMeServices services = new PersistentTokenBasedRememberMeServices(
                rememberBeKey,
                basicRememberMeUserDetailsService(),
                rememberMeTokenRepository
        );
        // FIXME No, this should be requested by a parameter at login time
        services.setAlwaysRemember(true);
        return services;
    }

    @Bean
    public BasicRememberMeUserDetailsService basicRememberMeUserDetailsService() {
        return new BasicRememberMeUserDetailsService();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.parentAuthenticationManager(authenticationManager);
    }

}
