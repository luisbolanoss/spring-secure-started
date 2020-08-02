package com.labs.spring.secure.started.jdbc;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.security.Principal;

@SpringBootApplication
public class SpringSecureStartedJdbcApp {
	@Bean
	UserDetailsManager userDetailsManager(DataSource dataSource) {
		JdbcUserDetailsManager jdbcUserDetailsManager = new JdbcUserDetailsManager();
		jdbcUserDetailsManager.setDataSource(dataSource);

		return jdbcUserDetailsManager;
	}

	@Bean
	InitializingBean initializer(UserDetailsManager userDetailsManager) {
		return () -> {
			String joshUsername = "josh";

			if (!userDetailsManager.userExists(joshUsername)) {
				UserDetails josh = User.withDefaultPasswordEncoder()
						.username(joshUsername)
						.password("password")
						.roles("USER")
						.build();

				userDetailsManager.createUser(josh);
			}
		};
	}

	public static void main(String[] args) {
		SpringApplication.run(SpringSecureStartedJdbcApp.class, args);
	}
}

@RestController
class GreetingRestController {
	public @GetMapping("/greeting")
	String greeting(Principal principal) {
		return "hello, " + principal.getName();
	}
}

@Configuration
@EnableWebSecurity
class JdbcSecurityConfiguration extends WebSecurityConfigurerAdapter {
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.httpBasic();

		http.authorizeRequests()
			.antMatchers("/", "/h2-console/**").permitAll()
			.anyRequest().authenticated()
			.and()
		.formLogin()
			.loginPage("/login")
			.permitAll()
			.and()
		.logout().permitAll();
	}
}

