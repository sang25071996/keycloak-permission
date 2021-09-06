package user.com.vn.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import user.com.vn.common.controller.BaseController;
import user.com.vn.common.dto.ResponJson;
import user.com.vn.constant.Constants;
import user.com.vn.dto.PrivilegeDto;
import user.com.vn.service.PrivilegeService;

@RestController
@RequestMapping(Constants.ApiURL.API_PRIVILEGE)
public class PrivilegeController extends BaseController {

	@Autowired
	private PrivilegeService privilegeService;

	public PrivilegeDto getById(Long id) {
		return privilegeService.getById(id);
	}

	public PrivilegeDto create(PrivilegeDto privilegeDto) {
		return privilegeService.create(privilegeDto);
	}

	@PostMapping()
	public ResponseEntity<ResponJson<PrivilegeDto>> save(@Valid @RequestBody PrivilegeDto privilegeDto) {
		return getResponseEntity(privilegeService.create(privilegeDto));
	}
	
	@GetMapping("/{id}")
	public ResponseEntity<ResponJson<PrivilegeDto>> getRoleById(@PathVariable Long id) {
		return getResponseEntity(privilegeService.getById(id));
	}

	@PutMapping()
	public ResponseEntity<ResponJson<PrivilegeDto>> edit(@RequestBody PrivilegeDto privilegeDto) {
		return getResponseEntity(privilegeService.edit(privilegeDto));
	}
	
	@DeleteMapping()
	public ResponseEntity<ResponJson<Boolean>> delete(@PathVariable Long id) {
		return getResponseEntity(privilegeService.delete(id));
	}

}
