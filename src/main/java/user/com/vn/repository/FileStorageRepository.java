package user.com.vn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import user.com.vn.entites.FileStorage;

/**
 * 
 * <p>File Storage Repository</p>
 * <p>Jan 7, 2021</p>
 *-------------------
 * @author macbook
 */
public interface FileStorageRepository extends JpaRepository<FileStorage, Long> {
	
	@Query("SELECT file FROM FileStorage file WHERE fileName = :fileName")
	FileStorage findByFileName(String fileName);
}
