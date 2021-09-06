package user.com.vn.service.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

import user.com.vn.common.dto.ErrorParam;
import user.com.vn.common.dto.RequestPagingBuilder;
import user.com.vn.common.dto.SysError;
import user.com.vn.common.service.BaseService;
import user.com.vn.constant.Constants;
import user.com.vn.dto.PrivilegeDto;
import user.com.vn.dto.RoleDto;
import user.com.vn.dto.SystemPermission;
import user.com.vn.event.EventType;
import user.com.vn.event.KeycloakEventPublisher;
import user.com.vn.exception.BadRequestException;
import user.com.vn.exception.NotFoundException;
import user.com.vn.mapper.PrivilegeMapper;
import user.com.vn.mapper.RoleMapper;
import user.com.vn.repository.PrivilegeRepository;
import user.com.vn.repository.RoleRepository;
import user.com.vn.service.RoleService;
import user.com.vn.user.entites.Privilege;
import user.com.vn.user.entites.Role;

@Service
public class RoleServiceImpl extends BaseService implements RoleService {
	

	@Autowired
	private RoleRepository roleRepository;
	@Autowired
	private RoleMapper roleMapper;
	@Autowired
	private PrivilegeMapper privilegeMapper;
	@Autowired
	private PrivilegeRepository privilegeRepository;
	
	@Autowired
	private KeycloakEventPublisher keycloakEventPublisher;
	
	@Transactional(rollbackOn = Exception.class)
	@Override
	public RoleDto create(RoleDto roleDto) {
		Role role = new Role();
		role.setName(roleDto.getName());
		setCreateInfo(role);
		Set<Privilege> privileges = roleDto.getPrivileges().stream().map(privilege -> privilegeRepository.getById(privilege.getId())).collect(Collectors.toSet());
		role.setPrivileges(privileges);
		role = roleRepository.save(role);
		keycloakEventPublisher.publishEvent(EventType.CREATE, role);
		return this.roleMapper.roleToRoleDto(role);
	}
	
	/**
	 * 
	 * <p>
	 * get By Id
	 * </p>
	 * <p>
	 * Mar 1, 2021
	 * </p>
	 * -------------------
	 * 
	 * @author macbook
	 * @param id Long
	 * @return RoleDto
	 */
	
	@Cacheable(key = "#id", value = "RoleDto")
	@Override
	public RoleDto getById(Long id) {
		
		Role role = roleRepository.findByRoleId(id);
		if (ObjectUtils.isEmpty(role)) {
			throw new NotFoundException(new SysError(new ErrorParam(Constants.ID_STR)));
		}
		return this.roleMapper.roleToRoleDto(role);
		
	}
	
	/**
	 * 
	 * <p>
	 * edit
	 * </p>
	 * <p>
	 * Mar 1, 2021
	 * </p>
	 * -------------------
	 * 
	 * @author macbook
	 * @param roleDto RoleDto
	 * @return RoleDto
	 */
	@Override
	public RoleDto edit(RoleDto roleDto) {
		
		Role role;
		if (ObjectUtils.isEmpty(roleDto.getId())) {
			throw new NotFoundException(new SysError(new ErrorParam(Constants.ID_STR)));
		}
		
		role = roleRepository.findByRoleId(roleDto.getId());
		if (ObjectUtils.isEmpty(role)) {
			throw new NotFoundException(new SysError(new ErrorParam(Constants.ID_STR)));
			
		}
		this.keycloakEventPublisher.publishEvent(EventType.PRE_UPDATE, role);

		role.setName(roleDto.getName());
		setUpdateInfo(role);
		
		roleRepository.save(role);
		
		this.keycloakEventPublisher.publishEvent(EventType.UPDATE, role);
		
		return this.roleMapper.roleToRoleDto(role);
	}
	
	/**
	 * 
	 * <p>
	 * get Roles
	 * </p>
	 * <p>
	 * Mar 1, 2021
	 * </p>
	 * -------------------
	 * 
	 * @author macbook
	 *
	 */
	@Override
	public List<RoleDto> getRoles() {
		List<RoleDto> roleDtos = new ArrayList<>();
		List<Role> roles = roleRepository.findAll();
		roles.forEach(role -> roleDtos.add(this.roleMapper.roleToRoleDto(role)));
		return roleDtos;
	}
	
	/**
	 * 
	 * <p>
	 * delete
	 * </p>
	 * <p>
	 * Mar 1, 2021
	 * </p>
	 * -------------------
	 * 
	 * @author macbook
	 * @param id Long
	 */
	@Override
	public boolean delete(Long id) {
		
		Role role = this.roleRepository.findByRoleId(id);
		if (ObjectUtils.isEmpty(role)) {
			throw new NotFoundException("Role not found in database");
		}
		
		this.roleRepository.delete(role);
		this.keycloakEventPublisher.publishEvent(EventType.DELETE, role);
		return true;
	}
	
	@Override
	public Page<RoleDto> filterPaging(RequestPagingBuilder<RoleDto> requestPagingBuilder) {
		
		String name = "";
		String[] fields = { "id" };
		
		name = defaultIfNotBlank(requestPagingBuilder.getFilters().getName(), name);
		
		if (ArrayUtils.isEmpty(requestPagingBuilder.getFieldsOrderBy())) {
			fields = requestPagingBuilder.getFieldsOrderBy();
		}
		
		Sort.Direction sortDirection;
		sortDirection = Direction.ASC;
		if (StringUtils.isNotEmpty(requestPagingBuilder.getSortBy().name())) {
			sortDirection = requestPagingBuilder.getSortBy();
		}
		
		Pageable pageable = PageRequest.of(requestPagingBuilder.getPage(), requestPagingBuilder.getSize(),
				Sort.by(sortDirection, fields));
		
		Page<Role> page = this.roleRepository.filterPaging(name, pageable);
		
		return page.map(content -> this.roleMapper.roleToRoleDto(content));
	}
	
	@Transactional
	public SystemPermission setManagementPermission(SystemPermission systemPermission) {
		
		Optional<Role> optionalRole = this.roleRepository.findById(systemPermission.getRoleDto().getId());
		if (!optionalRole.isPresent()) {
			throw new BadRequestException(new SysError(Constants.ERROR_DATA_NULL, new ErrorParam("Role")));
		}
		List<Privilege> privileges = new ArrayList<>();
		for (PrivilegeDto privilegeDto : systemPermission.getPrivilegeDtos()) {
			Optional<Privilege> optionalPrivilege = this.privilegeRepository.findById(privilegeDto.getId());
			if (!optionalPrivilege.isPresent()) {
				throw new BadRequestException(new SysError(Constants.ERROR_DATA_NULL, new ErrorParam("Privilege")));
			}
			privileges.add(optionalPrivilege.get());
		}
		
		Role role = optionalRole.get();
		Set<Privilege> set = new HashSet<>(privileges);
		role.setPrivileges(set);
		
		this.roleRepository.save(role);
		SystemPermission sysPermission = new SystemPermission();
		sysPermission.setPrivilegeDtos(this.privilegeMapper.toDto(role.getPrivileges()));
		sysPermission.setRoleDto(this.roleMapper.roleToRoleDto(role));
		return sysPermission;
	}
	
	public List<SystemPermission> getManagementPermission() {
		
		List<Role> roles = this.roleRepository.getPrivileges();
		SystemPermission systemPermission;
		List<SystemPermission> systemPermissions = new ArrayList<>();
		for (Role role : roles) {
			
			systemPermission = new SystemPermission();
			systemPermission.setRoleDto(this.roleMapper.roleToRoleDto(role));
			systemPermission.setPrivilegeDtos(this.privilegeMapper.toDto(role.getPrivileges()));
			systemPermissions.add(systemPermission);
		}
		return systemPermissions;
	}
}
