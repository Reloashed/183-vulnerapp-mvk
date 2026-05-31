package ch.bbw.m183.vulnerapp.service;

import ch.bbw.m183.vulnerapp.datamodel.PrivilegeEntity;
import ch.bbw.m183.vulnerapp.datamodel.RoleEntity;
import ch.bbw.m183.vulnerapp.repository.RoleRepository;
import ch.bbw.m183.vulnerapp.repository.UserRepository;
import jakarta.persistence.EntityManager;

import ch.bbw.m183.vulnerapp.datamodel.UserEntity;
import lombok.RequiredArgsConstructor;
import lombok.experimental.StandardException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private MessageSource messageSource;

	@Autowired
	private RoleRepository roleRepository;

	private final EntityManager entityManager;

	public UserEntity whoami(String username, String password) {
		// native queries are more performant!!1 :P
		var user = (UserEntity) entityManager.createNativeQuery("SELECT * from users where username='" + username + "'", UserEntity.class)
			.getSingleResult();
		if (password.equals(user.getPassword())) {
			return user;
		}
		throw new InvalidPasswordException("invalid password for user " + user.getUsername());
	}

	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	@StandardException
	public static class InvalidPasswordException extends RuntimeException {

	}

	@Override
	public UserDetails loadUserByUsername(String name)
			throws UsernameNotFoundException {

		Optional<UserEntity> user = userRepository.findById(name);
		if (user.isPresent()) {
			return new org.springframework.security.core.userdetails.User(
					" ", " ", true, true, true, true,
					getAuthorities(Arrays.asList(
							roleRepository.findByName("ROLE_USER"))));
		}

		return new org.springframework.security.core.userdetails.User(
				user.getEmail(), user.getPassword(), user.isEnabled(), true, true,
				true, getAuthorities(user.getRoles()));
	}

	private Collection<? extends GrantedAuthority> getAuthorities(
			Collection<RoleEntity> roles) {

		return getGrantedAuthorities(getPrivileges(roles));
	}

	private List<String> getPrivileges(Collection<RoleEntity> roles) {

		List<String> privileges = new ArrayList<>();
		List<PrivilegeEntity> collection = new ArrayList<>();
		for (RoleEntity role : roles) {
			privileges.add(role.getName());
			collection.addAll(role.getPrivileges());
		}
		for (PrivilegeEntity item : collection) {
			privileges.add(item.getName());
		}
		return privileges;
	}

	private List<GrantedAuthority> getGrantedAuthorities(List<String> privileges) {
		List<GrantedAuthority> authorities = new ArrayList<>();
		for (String privilege : privileges) {
			authorities.add(new SimpleGrantedAuthority(privilege));
		}
		return authorities;
	}
}
