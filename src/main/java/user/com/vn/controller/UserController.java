package user.com.vn.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import user.com.vn.common.controller.BaseController;
import user.com.vn.common.dto.ResponJson;
import user.com.vn.constant.Constants;
import user.com.vn.dto.UserDto;
import user.com.vn.service.RoleService;
import user.com.vn.service.UserService;

@RestController
@RequestMapping(Constants.ApiURL.API_USER)
public class UserController extends BaseController {

	@Autowired
	private UserService userService;

	@Autowired
	RoleService roleService;
	@PostMapping()
	public ResponseEntity<ResponJson<UserDto>> createUser(@RequestBody UserDto userDto) {
		return getResponseEntity(userService.create(userDto));
	}
	
	@GetMapping("/{id}")
	public ResponseEntity<ResponJson<UserDto>> getById(@PathVariable Long id) {
		return getResponseEntity(userService.getById(id));
	}
	
	@DeleteMapping()
	public ResponseEntity<ResponJson<Boolean>> delete(@PathVariable Long id) {
		return getResponseEntity(userService.delete(id));
	}

	@GetMapping()
	public ResponseEntity<ResponJson<List<UserDto>>> getUsers() {
		
		return getResponseEntity(userService.getUsers());
	}
}
