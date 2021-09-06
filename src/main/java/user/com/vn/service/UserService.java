package user.com.vn.service;

import java.util.List;

import user.com.vn.dto.UserDto;

public interface UserService extends IBaseService<UserDto> {
	
	List<UserDto> getUsers();
}
