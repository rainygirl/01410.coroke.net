package net.coroke.terminal.config

import net.coroke.terminal.handler.FailureHandler
import net.coroke.terminal.handler.LogoutHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain

@Configuration
class SecurityConfig {

    @Bean
    fun filterChain(http: HttpSecurity, logoutHandler: LogoutHandler): SecurityFilterChain {

        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.NEVER).and()
            .authorizeHttpRequests().antMatchers("/**/h2-console/**").permitAll().and()
            .headers().frameOptions().sameOrigin().and()
            .logout()
            .logoutUrl("/login/oauth2/logout")
            .addLogoutHandler(logoutHandler)
            .deleteCookies("SESSION")
            .permitAll().and()
            .oauth2Login {
                it.userInfoEndpoint()
                it.authorizationEndpoint().baseUri("/login/oauth2/authorization")
                it.defaultSuccessUrl("/login/oauth2/callback")
                it.failureUrl("/login/fail")
                it.failureHandler(FailureHandler())
                it.loginPage("/login")
            }
            .formLogin().disable()
            .csrf().disable()
        return http.build()
    }
}
