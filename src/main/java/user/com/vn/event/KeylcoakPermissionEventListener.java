package user.com.vn.event;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.ResourceResource;
import org.keycloak.admin.client.resource.ResourceScopesResource;
import org.keycloak.admin.client.resource.ResourcesResource;
import org.keycloak.admin.client.resource.RolePoliciesResource;
import org.keycloak.admin.client.resource.ScopePermissionResource;
import org.keycloak.admin.client.resource.ScopePermissionsResource;
import org.keycloak.representations.idm.authorization.DecisionStrategy;
import org.keycloak.representations.idm.authorization.PolicyRepresentation;
import org.keycloak.representations.idm.authorization.ResourceRepresentation;
import org.keycloak.representations.idm.authorization.RolePolicyRepresentation;
import org.keycloak.representations.idm.authorization.ScopePermissionRepresentation;
import org.keycloak.representations.idm.authorization.ScopeRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.NamedThreadLocal;

import user.com.vn.exception.BadRequestException;
import user.com.vn.exception.ServiceRunTimeException;
import user.com.vn.repository.PrivilegeRepository;
import user.com.vn.user.entites.Privilege;
import user.com.vn.user.entites.Resource;
import user.com.vn.user.entites.Role;

public class KeylcoakPermissionEventListener extends AbstractKeycloakEventListener<Role> {

	private static final Logger LOG = LoggerFactory.getLogger(KeylcoakPermissionEventListener.class);
	
	private ThreadLocal<Set<String>> permissionsLocal = new NamedThreadLocal<>("permissions before update"); 
	
	@Autowired
	private PrivilegeRepository privilegeRepository;
	
	public KeylcoakPermissionEventListener(Keycloak keycloak, String realm) {
		super(keycloak, realm);
	}

	@Override
	public boolean support(Class<?> type) {
		return type.isAssignableFrom(Role.class);
	}

	@Override
	public void preUpdate(Role entity) {
		Set<String> permissionIds = entity.getPrivileges().stream().map(Privilege::getName).collect(Collectors.toSet()); 
		permissionsLocal.set(permissionIds); 
	}

	@Override
	public void postCreate(Role entity) {
		for (Privilege privilege : entity.getPrivileges()) {
			create(entity, privilege.getName());
		}
	}

	private void create(Role entity, String permissionCode) {
		String clientId = findByClientId(permissionCode);
		RolePoliciesResource rolePoliciesResource = client().get(clientId).authorization().policies().role();
		RolePolicyRepresentation rolePolicyRepresentation = new RolePolicyRepresentation();
		rolePolicyRepresentation.setName(buildNameRolepolicyRepresentation(entity.getName(), permissionCode.toUpperCase()));
		String idRole = role().get(entity.getName()).toRepresentation().getId();
		rolePolicyRepresentation.addRole(idRole);
		Response response = rolePoliciesResource.create(rolePolicyRepresentation);
		String policyId = getCreated(response);
		Privilege privilege = privilegeRepository.findByName(permissionCode);
		applyToPermission(clientId, policyId, privilege);
	}
	
	private void remove(Role entity, String permissionCode) { 
		Privilege permission = privilegeRepository.findByName(permissionCode);
		String clientId = client().findByClientId(permission.getResource().getCode()).get(0).getId(); 
		String policyName = buildNameRolepolicyRepresentation(entity.getName(), permission.getName());
		String policyId = rolePoliciesResource(clientId).findByName(policyName).getId();
		rolePoliciesResource(clientId).findById(policyId).remove(); 
	}

	/**
	 * <pre>apply to permission</pre>
	 * @param clientId
	 * @param policyId
	 * @param privilege
	 */
	private void applyToPermission(String clientId, String policyId, Privilege privilege) {
		createScopePermissionIfNotExists(clientId, privilege);
		ScopePermissionRepresentation scopePermissionRepresentation = scopePermissionsResource(clientId)
				.findByName(privilege.getName());
		ScopePermissionResource scopePermissionResource = scopePermissionsResource(clientId)
				.findById(scopePermissionRepresentation.getId());
		involvePermissionRepresentation(scopePermissionRepresentation, scopePermissionResource);
		scopePermissionRepresentation.addPolicy(policyId);
		scopePermissionResource.update(scopePermissionRepresentation);
	}
	
	/**
	 * <pre>create scope permission if not exists</pre>
	 * @param clientId
	 * @param privilege
	 */
	private void createScopePermissionIfNotExists(String clientId, Privilege privilege) {
		createScopesIfNotExists(clientId, privilege);
		ScopePermissionsResource scopePermissionsResource = scopePermissionsResource(clientId);
		if (scopePermissionsResource.findByName(privilege.getName()) == null) {
			ScopePermissionRepresentation scopePermissionRepresentation = new ScopePermissionRepresentation();
			scopePermissionRepresentation.setName(privilege.getName());
			String resourceId = resourceResource(clientId).findByName(privilege.getResource().getCode()).get(0).getId();
			scopePermissionRepresentation.setResources(new HashSet<>(Arrays.asList(resourceId)));
			String scopeId = resourceScopesResource(clientId).findByName(privilege.getName()).getId();
			scopePermissionRepresentation.setScopes(new HashSet<>(Arrays.asList(scopeId)));
			scopePermissionRepresentation.setDecisionStrategy(DecisionStrategy.AFFIRMATIVE);
			scopePermissionsResource.create(scopePermissionRepresentation);
		}
	}
	
	private void involvePermissionRepresentation(ScopePermissionRepresentation scopePermissionRepresentation, ScopePermissionResource scopePermissionResource) { 
		scopePermissionRepresentation.setPolicies(scopePermissionResource.associatedPolicies().stream().map(PolicyRepresentation::getId).collect(Collectors.toSet())); 
		scopePermissionRepresentation.setResources(scopePermissionResource.resources().stream().map(ResourceRepresentation::getId).collect(Collectors.toSet())); 
		scopePermissionRepresentation.setScopes(scopePermissionResource.scopes().stream().map(ScopeRepresentation::getId).collect(Collectors.toSet())); 
	}
	
	/**
	 * <pre>create scopes if not exists</pre>
	 * @param clientId
	 * @param privilege
	 */
	private void createScopesIfNotExists(String clientId, Privilege privilege) {
		createResourceIfNotExist(clientId, privilege);
		ResourceScopesResource resourceScopesResource = resourceScopesResource(clientId);
		if (resourceScopesResource.findByName(privilege.getName()) == null) {
			Response response = null;
			try {
				response = resourceScopesResource.create(new ScopeRepresentation(privilege.getName()));
				addResourceToScopes(clientId, privilege, response.readEntity(ScopeRepresentation.class));
			} catch (ServiceRunTimeException e) {
				LOG.error(e.getMessage());
			} finally {
				if (response != null) {
					response.close();
				}
			}
			
		}
	}
	
	/**
	 * <pre>add resource to scopes</pre>
	 * @param clientId
	 * @param privilege
	 * @param scopeRepresentation
	 */
	private void addResourceToScopes(String clientId, Privilege privilege, ScopeRepresentation scopeRepresentation) {
		ResourcesResource resourcesResource = resourceResource(clientId);
		Resource resource = privilege.getResource();
		ResourceRepresentation resourceRepresentation = resourcesResource.findByName(resource.getCode()).get(0);
		resourceRepresentation.addScope(scopeRepresentation);
		ResourceResource resourceResource = resourcesResource.resource(resourceRepresentation.getId());
		resourceResource.update(resourceRepresentation);
	}
	
	/**
	 * <pre>find by client ID</pre>
	 * @param privilege {@link Privilege}
	 * @return String
	 */
	private String findByClientId(String clientId) {
		return client().findByClientId(clientId).get(0).getId();
	}
	
	/**
	 * <pre>create resource if not exist</pre>
	 * @param Resource {@link Resource}
	 */
	private void createResourceIfNotExist(String clientId, Privilege privilege) {
		ResourcesResource resourcesResource = resourceResource(clientId);
		Resource resource = privilege.getResource();
		if (resourcesResource.findByName(resource.getCode()).isEmpty()) {
			resourcesResource.create(resourceRepresentation(resource));
		}
	}

	/**
	 * <pre>resource representation</pre>
	 * @param resource {@link Resource}
	 * @return ResourceRepresentation {@link ResourceRepresentation}
	 */
	private ResourceRepresentation resourceRepresentation(Resource resource) {
		ResourceRepresentation resourceRepresentation = new ResourceRepresentation();
		resourceRepresentation.setName(resource.getCode());
		resourceRepresentation.setDisplayName(resource.getName());
		resourceRepresentation.setUris(setUris("/*"));
		return resourceRepresentation;
	}
	
	/**
	 * <pre>set uris</pre>
	 * @param str String
	 * @return Set<String>
	 */
	private Set<String> setUris(String str) {
		if (StringUtils.isNotBlank(str)) {
			String[] uris = str.trim().split(",");
			Set<String> set = new HashSet<>();
			for (String uri : uris) {
				set.add(uri.trim());
			}
			return set;
		}
		return new HashSet<>();
	}
	
	private ScopePermissionsResource scopePermissionsResource(String clientId) {
		return client().get(clientId).authorization().permissions().scope();
	}
	
	/**
	 * <pre>resource resource</pre>
	 * @param clientId
	 * @return ResourcesResource
	 */
	private ResourcesResource resourceResource(String clientId) {
		return client().get(clientId).authorization().resources();
	}

	/**
	 * <pre>resource scopes resource</pre>
	 * @param clientId
	 * @return ResourceScopesResource
	 */
	private ResourceScopesResource resourceScopesResource(String clientId) {
		return client().get(clientId).authorization().scopes();
	}
	
	private RolePoliciesResource rolePoliciesResource(String clientId) {
		return client().get(clientId).authorization().policies().role();
	}
	
	/**
	 * <pre>get created</pre>
	 * @param response
	 * @return String
	 */
	@SuppressWarnings("unchecked")
	private String getCreated(Response response) {
		if (response.getStatus() != 201) {
			throw new BadRequestException("error created policy");
		}
		Map<String, String> map = response.readEntity(Map.class);
		return map.get("id");
	}
	
	/**
	 * <pre>build name role policy representation</pre>
	 * @param roleName
	 * @param privilegeCode
	 * @return
	 */
	private String buildNameRolepolicyRepresentation(String roleName, String privilegeCode) {
		StringBuilder builder = new StringBuilder();
		builder.append(roleName);
		builder.append("_");
		builder.append(privilegeCode);
		return builder.toString();
	}

	@Override
	public void postUpdate(Role entity) {
		Set<String> currentIds = entity.getPrivileges().stream().map(Privilege::getName).collect(Collectors.toSet());
		for (String id : permissionsLocal.get()) {
			if (!currentIds.contains(id))
				remove(entity, id);
		}
		for (String id : currentIds) {
			if (!permissionsLocal.get().contains(id)) {
				create(entity, id);
			}
		}
		permissionsLocal.remove();
	}

}
