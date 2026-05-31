package ch.bbw.m183.vulnerapp.repository;

import ch.bbw.m183.vulnerapp.datamodel.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, String> {
}
