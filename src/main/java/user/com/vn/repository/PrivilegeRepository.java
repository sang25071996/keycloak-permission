package user.com.vn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import user.com.vn.user.entites.Privilege;
import user.com.vn.user.entites.Role;

@Repository
public interface PrivilegeRepository extends JpaRepository<Privilege, Long> {

	@Query("SELECT p FROM Privilege p WHERE p.id = :id")
	Role findByRoleId(@Param("id") Long id);
	
	@Query("SELECT p FROM Privilege p WHERE p.name = :name")
	Privilege findByName(@Param("name") String name);
}
