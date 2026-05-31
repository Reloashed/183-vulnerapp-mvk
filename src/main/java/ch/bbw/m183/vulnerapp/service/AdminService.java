package ch.bbw.m183.vulnerapp.service;

import java.util.List;
import java.util.stream.Stream;

import ch.bbw.m183.vulnerapp.datamodel.PrivilegeEntity;
import ch.bbw.m183.vulnerapp.datamodel.RoleEntity;
import ch.bbw.m183.vulnerapp.datamodel.UserEntity;
import ch.bbw.m183.vulnerapp.repository.PrivilegeRepository;
import ch.bbw.m183.vulnerapp.repository.RoleRepository;
import ch.bbw.m183.vulnerapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class AdminService {

	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final PrivilegeRepository privilegeRepository;

	public UserEntity createUser(UserEntity newUser) {
		return userRepository.save(newUser);
	}

	public Page<UserEntity> getUsers(Pageable pageable) {
		return userRepository.findAll(pageable);
	}

	public void deleteUser(String username) {
		userRepository.deleteById(username);
	}

	@EventListener(ContextRefreshedEvent.class)
	public void loadTestUsers() {

		// Privileges
		PrivilegeEntity whoami = new PrivilegeEntity()
				.setId(1L)
				.setName("WHOAMI");

		PrivilegeEntity postBlog = new PrivilegeEntity()
				.setId(2L)
				.setName("POST_BLOG");

		privilegeRepository.save(whoami);
		privilegeRepository.save(postBlog);

		// USER role
		RoleEntity userRole = new RoleEntity()
				.setId(1L)
				.setName("ROLE_USER")
				.setPrivileges(List.of(whoami, postBlog));

		// ADMIN role
		RoleEntity adminRole = new RoleEntity()
				.setId(2L)
				.setName("ROLE_ADMIN")
				.setPrivileges(List.of(whoami, postBlog));

		roleRepository.save(userRole);
		roleRepository.save(adminRole);

		// User with USER role
		UserEntity user = new UserEntity()
				.setUsername("user")
				.setFullname("Test User")
				.setPassword("{noop}password")
				.setRoles(List.of(userRole));

		// User with ADMIN role
		UserEntity admin = new UserEntity()
				.setUsername("admin")
				.setFullname("Super Admin")
				.setPassword("{noop}super5ecret")
				.setRoles(List.of(adminRole));

		createUser(user);
		createUser(admin);
	}
}
