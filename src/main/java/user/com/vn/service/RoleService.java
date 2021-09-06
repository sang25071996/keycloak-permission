package user.com.vn.service;

import java.util.List;

import org.springframework.data.domain.Page;

import user.com.vn.common.dto.RequestPagingBuilder;
import user.com.vn.dto.RoleDto;

public interface RoleService extends IBaseService<RoleDto> {
	
	List<RoleDto> getRoles();
	
	Page<RoleDto> filterPaging(RequestPagingBuilder<RoleDto> requestPagingBuilder);
}
