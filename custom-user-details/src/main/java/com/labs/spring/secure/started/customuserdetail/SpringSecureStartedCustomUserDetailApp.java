package com.labs.spring.secure.started.customuserdetail;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsPasswordService;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.MessageDigestPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SpringBootApplication
public class SpringSecureStartedCustomUserDetailApp {
	@Bean
	PasswordEncoder passwordEncoder() {
		return PasswordEncoderFactories.createDelegatingPasswordEncoder();
	}

	@Bean
	PasswordEncoder oldPasswordEncode() {
		String md5 = "MD5";
		return new DelegatingPasswordEncoder(md5, Collections.singletonMap(md5, new MessageDigestPasswordEncoder(md5)));
	}

	@Bean
	CustomUserDetailsService customUserDetailsService() {
		Collection<UserDetails> users = Arrays.asList(
			new CustomUserDetails("ben", oldPasswordEncode().encode("benspassword"), true, "USER")
		);
		return new CustomUserDetailsService(users);
	}

	public static void main(String[] args) { SpringApplication.run(SpringSecureStartedCustomUserDetailApp.class, args);
	}
}

@RestController
class GreetingRestController {
	@GetMapping("/greeting")
	public String greeting(Principal principal) {
		return "hello, " + principal.getName();
	}
}

@Configuration
@EnableWebSecurity
class CustomUserDetailsSecurityConfiguration extends WebSecurityConfigurerAdapter {
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.httpBasic();

		http.authorizeRequests().anyRequest().authenticated();
	}
}

class CustomUserDetailsService implements UserDetailsService, UserDetailsPasswordService {
	private final Map<String, UserDetails> users = new ConcurrentHashMap<>();

	public CustomUserDetailsService(Collection<UserDetails> seedUsers) {
		seedUsers.forEach(user -> {
			this.users.put(user.getUsername(), user);
		});
	}


	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		if (this.users.containsKey(username)) {
			return this.users.get(username);
		}

		throw new UsernameNotFoundException("no found username");
	}

	@Override
	public UserDetails updatePassword(UserDetails user, String newPassword) {
		System.out.println("====>" + newPassword);
		this.users.put(user.getUsername(), new CustomUserDetails(
			user.getUsername(),
			newPassword,
			user.isEnabled(),
			user.getAuthorities().stream().map(GrantedAuthority::getAuthority).toArray(String[]::new)
		));

		return this.loadUserByUsername(user.getUsername());
	}
}

class CustomUserDetails implements UserDetails {
	String username;
	String password;
	boolean active;
	private final Set<SimpleGrantedAuthority> authorities;

	public CustomUserDetails(String username, String password, boolean active, String ...authorities) {
		this.username = username;
		this.password = password;
		this.active = active;
		this.authorities = Stream.of(authorities).map(SimpleGrantedAuthority::new).collect(Collectors.toSet());
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return this.authorities;
	}

	@Override
	public String getPassword() {
		return this.password;
	}

	@Override
	public String getUsername() {
		return this.username;
	}

	@Override
	public boolean isAccountNonExpired() {
		return this.active;
	}

	@Override
	public boolean isAccountNonLocked() {
		return this.active;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return active;
	}

	@Override
	public boolean isEnabled() {
		return active;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

}
