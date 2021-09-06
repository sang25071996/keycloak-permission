package user.com.vn.mapper;

import org.mapstruct.Mapper;

import user.com.vn.dto.RoleDto;
import user.com.vn.user.entites.Role;

@Mapper(componentModel = "spring")
public interface RoleMapper {
	
	RoleDto roleToRoleDto(Role role);
	
	Role roleDtoToRole(RoleDto roleDto);
}
