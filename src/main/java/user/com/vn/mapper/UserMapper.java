package user.com.vn.mapper;

import org.mapstruct.Mapper;

import user.com.vn.dto.UserDto;
import user.com.vn.user.entites.User;

@Mapper
public interface UserMapper {
	
	UserDto userToUserDto(User user);
	
	User userDtoToUser(UserDto userDto);
}
