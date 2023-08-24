package com.tfg.service;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.tfg.service.models.service.impl.UserDetailsServiceImpl;

@Configuration("kieServerSecurity")
@EnableWebSecurity
public class DefaultWebSecurityConfig extends WebSecurityConfigurerAdapter {
	
	@Autowired
	private UserDetailsServiceImpl userDetailsService;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .cors().and()
                .csrf().disable()
                .authorizeRequests()
                .antMatchers("/rest/*").authenticated().and()
                .httpBasic().and()
                .headers().frameOptions().disable();
    	
    	http
			.authorizeRequests()
//				.antMatchers("/", "/login/**", "/signup/**", "/rest/**").permitAll()
				.antMatchers("/", "/login/**", "/signup/**").permitAll()
				.antMatchers("/rest/*").authenticated()
				.anyRequest().authenticated()
				.and()
			.formLogin()
				.loginPage("/login")
				.defaultSuccessUrl("/private/", true)
				.failureUrl("/login?error")
				.loginProcessingUrl("/loginUser").permitAll()
				.and()
			.headers().frameOptions().disable().and()
			.csrf().disable()
			.cors();
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {

//        auth.inMemoryAuthentication().withUser("user").password("user").roles("kie-server");
//        auth.inMemoryAuthentication().withUser("wbadmin").password("wbadmin").roles("admin");
//        auth.inMemoryAuthentication().withUser("kieserver").password("kieserver1!").roles("kie-server");
    	
    	auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
//    	auth.inMemoryAuthentication().withUser("12345678a").password("1234").roles("Practitioner");
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowedOrigins(Arrays.asList("*"));
        corsConfiguration.setAllowCredentials(true);
        corsConfiguration.setAllowedMethods(Arrays.asList(HttpMethod.GET.name(), HttpMethod.HEAD.name(),
                                                          HttpMethod.POST.name(), HttpMethod.DELETE.name(), HttpMethod.PUT.name()));
        corsConfiguration.applyPermitDefaultValues();
        source.registerCorsConfiguration("/**", corsConfiguration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }
}
