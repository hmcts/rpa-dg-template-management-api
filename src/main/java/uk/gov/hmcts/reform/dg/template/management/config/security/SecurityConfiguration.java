package uk.gov.hmcts.reform.dg.template.management.config.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import uk.gov.hmcts.reform.auth.checker.core.RequestAuthorizer;
import uk.gov.hmcts.reform.auth.checker.core.service.Service;
import uk.gov.hmcts.reform.auth.checker.core.user.User;
import uk.gov.hmcts.reform.auth.checker.spring.serviceanduser.AuthCheckerServiceAndUserFilter;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    private final AuthCheckerServiceAndUserFilter authCheckerFilter;

    public SecurityConfiguration(final RequestAuthorizer<User> userRequestAuthorizer,
                                 final RequestAuthorizer<Service> serviceRequestAuthorizer,
                                 final AuthenticationManager authenticationManager) {
        this.authCheckerFilter = new AuthCheckerServiceAndUserFilter(serviceRequestAuthorizer, userRequestAuthorizer);
        this.authCheckerFilter.setAuthenticationManager(authenticationManager);
    }

    @Override
    public void configure(WebSecurity web) {
        web.ignoring().antMatchers("/swagger-ui.html",
            "/webjars/springfox-swagger-ui/**",
            "/swagger-resources/**",
            "/v2/**",
            "/health",
            "/status/health",
            "/loggers/**",
            "/");
    }

    @Override
    protected void configure(final HttpSecurity http) throws Exception {
        // Don't erase user credentials as this is needed for the user profile
        final ProviderManager authenticationManager = (ProviderManager) authenticationManager();
        authenticationManager.setEraseCredentialsAfterAuthentication(false);
        authCheckerFilter.setAuthenticationManager(authenticationManager());

        http.antMatcher("/api/**")
            .addFilter(authCheckerFilter)
            .sessionManagement().sessionCreationPolicy(STATELESS).and()
            .csrf().disable()
            .formLogin().disable()
            .logout().disable()
            .authorizeRequests()
            .anyRequest()
            .authenticated();
    }
}
