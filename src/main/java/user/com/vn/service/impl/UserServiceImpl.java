package user.com.vn.service.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.transaction.Transactional;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import user.com.vn.common.dto.ErrorParam;
import user.com.vn.common.dto.SysError;
import user.com.vn.common.service.BaseService;
import user.com.vn.constant.Constants;
import user.com.vn.dto.RoleDto;
import user.com.vn.dto.UserDto;
import user.com.vn.entites.Authorizer;
import user.com.vn.event.EventType;
import user.com.vn.event.KeycloakEventPublisher;
import user.com.vn.exception.NotFoundException;
import user.com.vn.mapper.UserMapper;
import user.com.vn.repository.UserRepository;
import user.com.vn.service.UserService;
import user.com.vn.user.entites.Role;
import user.com.vn.user.entites.User;

@Service
public class UserServiceImpl extends BaseService implements UserService, UserDetailsService {
	
	@Autowired
	UserRepository userRepository;
	
	private UserMapper userMapper;
	
	@Autowired
	private KeycloakEventPublisher keycloakEventPublisher;
	
	public UserServiceImpl() {
		this.userMapper = getInstanceMappger(UserMapper.class);
	}
	
	@Override
	public Authorizer loadUserByUsername(String username) {
		User user = userRepository.findByUsername(username);
		if (ObjectUtils.isEmpty(user)) {
			throw new UsernameNotFoundException(username);
		}
		return new Authorizer(user);
	}
	
	/**
	 * 
	 * <p>
	 * create user
	 * </p>
	 * Nov 13, 2020 -------------------
	 * 
	 * @author macbook
	 *
	 */
	@Transactional(rollbackOn = Exception.class)
	@Override
	public UserDto create(UserDto userDto) {
		
		User user = new User();
		user.setUsername(userDto.getUsername());
		user.setPassword(passwordEncode(userDto.getPassword()));
		Set<Role> roles = new HashSet<>();
		for (RoleDto roleDto : userDto.getRoles()) {
			roles.add(new Role(roleDto.getId(), roleDto.getName()));
		}
		user.setRoles(roles);
		if (ObjectUtils.isEmpty(user)) {
			throw new NotFoundException(new SysError(new ErrorParam(Constants.ID_STR)));
		}
		
		setCreateInfo(user);
		userRepository.save(user);
		keycloakEventPublisher.publishEvent(EventType.CREATE, user);
		return userMapper.userToUserDto(user);
	}
	
	/**
	 * 
	 * <p>
	 * find All User
	 * </p>
	 * Nov 28, 2020 -------------------
	 * 
	 * @author macbook
	 *
	 */
	@Override
	public List<UserDto> getUsers() {
		
		List<UserDto> userDtos = new ArrayList<>();
		List<User> users = userRepository.getAllUsers();
		
		users.stream().forEach(user -> userDtos.add(userMapper.userToUserDto(user)));
		return userDtos;
	}
	
	/**
	 * 
	 * <p>
	 * password Encode
	 * </p>
	 * <p>
	 * Nov 28, 2020
	 * </p>
	 * -------------------
	 * 
	 * @author macbook
	 * @param password
	 * @return password encode
	 */
	private String passwordEncode(String password) {
		return DigestUtils.md5Hex(password);
	}
	
	@Override
	public UserDto edit(UserDto e) {
		keycloakEventPublisher.publishEvent(EventType.UPDATE, e);
		return null;
	}
	
	@Override
	public boolean delete(Long id) {
		
		User user = this.userRepository.getById(id);
		if (ObjectUtils.isEmpty(user)) {
			throw new NotFoundException("User not Found in database");
		}
		
		this.userRepository.delete(user);
		keycloakEventPublisher.publishEvent(EventType.DELETE, user);
		return true;
	}
	
//	@PreAuthorize("hasRole('ROLE_ADMIN')")//SecurityExpressionRoot
	@PostAuthorize("hasPermission(@privilegeServiceImpl.getPrivileges(),'USER_READ')")
	@Override
	public UserDto getById(Long id) {
		User user = this.userRepository.getById(id);
		return userMapper.userToUserDto(user);
	}
	
}
