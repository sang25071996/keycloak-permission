package user.com.vn.service;

import user.com.vn.dto.BaseDto;

public interface IBaseService<E extends BaseDto> {
	
	E create(E e);
	
	E edit(E e);
	
	boolean delete(Long id);
	
	E getById(Long id);
}
