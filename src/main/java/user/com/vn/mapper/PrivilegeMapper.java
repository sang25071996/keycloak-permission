package user.com.vn.mapper;

import java.util.List;
import java.util.Set;

import org.mapstruct.Mapper;

import user.com.vn.dto.PrivilegeDto;
import user.com.vn.user.entites.Privilege;

@Mapper(componentModel = "spring")
public interface PrivilegeMapper {
	
	PrivilegeDto toDto(Privilege privilege);
	
	List<PrivilegeDto> toDto(Set<Privilege> set);
	
	Privilege toEntity(PrivilegeDto privilegeDto);
	
	Set<Privilege> toEntity(Set<PrivilegeDto> privilegeDtos);
}
