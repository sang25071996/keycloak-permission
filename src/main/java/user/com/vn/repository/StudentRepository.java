package user.com.vn.repository;

import org.springframework.data.repository.CrudRepository;

import user.com.vn.entites.Student;

//@Repository
public interface StudentRepository extends CrudRepository<Student, Long> {
	
}
