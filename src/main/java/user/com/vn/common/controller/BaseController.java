package user.com.vn.common.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import user.com.vn.common.dto.ResponJson;
import user.com.vn.common.dto.UploadFile;
import user.com.vn.common.service.FileStorageService;
import user.com.vn.constant.Constants;

/**
 * <p>
 * Base Controller
 * </p>
 * 
 * @author macbook Nov 13, 2020
 */
@Controller
public class BaseController {
	
	@Autowired
	private FileStorageService fileStorageService;
	
	
	/**
	 * 
	 * <p>
	 * set value return in ResponseEntity
	 * </p>
	 * Nov 9, 2020
	 * 
	 * @author macbook
	 * @param <T>
	 * @param object
	 * @return ResponseEntity<ResponJson>
	 */
	public <T> ResponseEntity<ResponJson<T>> getResponseEntity(T object) {
		ResponJson<T> responJson = new ResponJson<>(object,HttpStatus.OK, Constants.SUCCESS);
		return ResponseEntity.ok(responJson);
	}
	
	@GetMapping(path = "/dowload")
	public void dowloadFile(HttpServletResponse response, @RequestParam String fileName) {
		this.fileStorageService.dowloadFile(response, fileName);
	}
	
	@PostMapping("/uploadFile")
	public ResponseEntity<UploadFile> uploadFile(@RequestParam("file") MultipartFile multipartFile) throws IOException {
		
		return ResponseEntity.ok(this.fileStorageService.uploadFile(multipartFile));
	}
}
