package user.com.vn.service.impl;

import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import user.com.vn.common.dto.ErrorParam;
import user.com.vn.common.dto.SysError;
import user.com.vn.common.service.BaseService;
import user.com.vn.constant.Constants;
import user.com.vn.dto.PrivilegeDto;
import user.com.vn.exception.BadRequestException;
import user.com.vn.mapper.PrivilegeMapper;
import user.com.vn.repository.PrivilegeRepository;
import user.com.vn.service.PrivilegeService;
import user.com.vn.user.entites.Privilege;

@Service(value = "privilegeServiceImpl")
public class PrivilegeServiceImpl extends BaseService implements PrivilegeService {

	private static final String PRIVILEGE = "Privilege";
	@Autowired
	private PrivilegeRepository privilegeRepository;
	@Autowired
	private PrivilegeMapper privilegeMapper;
	
	@Transactional
	@Override
	public PrivilegeDto create(PrivilegeDto privilegeDto) {
		
		if (ObjectUtils.isEmpty(privilegeDto)) {
			throw new BadRequestException(new SysError(Constants.ERROR_DATA_EMPTY, new ErrorParam("PrivilegeDto")));
		}
		
		if (StringUtils.isBlank(privilegeDto.getName())) {
			throw new BadRequestException(new SysError(Constants.ERROR_DATA_EMPTY, new ErrorParam("name")));
		}
		Privilege privilege = new Privilege();
		privilege.setName(privilegeDto.getName());
		setCreateInfo(privilege);
		
		privilegeRepository.save(privilege);
		return this.privilegeMapper.toDto(privilege);
	}

	@Transactional
	@Override
	public PrivilegeDto edit(PrivilegeDto privilegeDto) {

		if (ObjectUtils.isEmpty(privilegeDto)) {
			throw new BadRequestException(new SysError(Constants.ERROR_DATA_EMPTY, new ErrorParam("PrivilegeDto")));
		}
		
		if (StringUtils.isBlank(privilegeDto.getName())) {
			throw new BadRequestException(new SysError(Constants.ERROR_DATA_EMPTY, new ErrorParam("name")));
		}
		Optional<Privilege> optionalPrivilege = this.privilegeRepository.findById(privilegeDto.getId());
		if (!optionalPrivilege.isPresent()) {
			throw new BadRequestException(new SysError(Constants.ERROR_DATA_NULL, new ErrorParam(PRIVILEGE)));
		}
		
		Privilege privilege = optionalPrivilege.get();
		privilege.setName(privilegeDto.getName());
		setCreateInfo(privilege);
		
		privilegeRepository.save(privilege);
		return this.privilegeMapper.toDto(privilege);
	}

	@Transactional
	@Override
	public boolean delete(Long id) {

		Optional<Privilege> optionalPrivilege = this.privilegeRepository.findById(id);
		
		if (!optionalPrivilege.isPresent()) {
			throw new BadRequestException(new SysError(Constants.ERROR_DATA_NULL, new ErrorParam(PRIVILEGE)));
		}
		
		this.privilegeRepository.delete(optionalPrivilege.get());
		return true;
	}

	@Override
	public PrivilegeDto getById(Long id) {
		
		Optional<Privilege> optionalPrivilege = this.privilegeRepository.findById(id);
		if (!optionalPrivilege.isPresent()) {
			throw new BadRequestException(new SysError(Constants.ERROR_DATA_NULL, new ErrorParam(PRIVILEGE)));
		}
		
		return this.privilegeMapper.toDto(optionalPrivilege.get());
	}
	
	public List<Privilege> getPrivileges() {
		return this.privilegeRepository.findAll();
	}
	
}
