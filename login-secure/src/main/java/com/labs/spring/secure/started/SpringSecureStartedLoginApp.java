package com.labs.spring.secure.started;

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
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@SpringBootApplication
public class SpringSecureStartedLoginApp {
	@Bean
	UserDetailsManager userDetailsManager() {
		return new InMemoryUserDetailsManager();
	}

	@Bean
	InitializingBean initializer(UserDetailsManager userDetailsManager) {
		return () -> {
			String joshUsername = "josh";

			UserDetails josh = User.withDefaultPasswordEncoder()
					.username(joshUsername)
					.password("password")
					.roles("USER")
					.build();

			userDetailsManager.createUser(josh);

		};
	}

	public static void main(String[] args) {
		SpringApplication.run(SpringSecureStartedLoginApp.class, args);
	}
}

@ControllerAdvice
class PrincipalControllerAdvice {
	@ModelAttribute("currentUser")
	Principal principal(Principal p) {
		return p;
	}
}

@Controller
class LoginController {
	@GetMapping("/")
	public String index() {
		return "hidden";
	}

	@GetMapping("/login")
	public String login() {
		return "login";
	}

	@GetMapping("/logout-success")
	public String logout() {
		return "logout";
	}
}

@Configuration
@EnableWebSecurity
class LoginSecurityConfiguration extends WebSecurityConfigurerAdapter {
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.authorizeRequests().anyRequest().authenticated();

		http.formLogin().loginPage("/login").permitAll();

		http.logout().logoutUrl("/logout").logoutSuccessUrl("/logout-success").permitAll();

//		http.logout().logoutUrl("/logout").logoutSuccessHandler(new LogoutSuccessHandler() {
//			@Override
//			public void onLogoutSuccess(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Authentication authentication) throws IOException, ServletException {
//				// Process after logout
//			}
//		});
	}
}