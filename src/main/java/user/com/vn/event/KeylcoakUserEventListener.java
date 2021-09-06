package user.com.vn.event;

import java.util.Arrays;
import java.util.List;

import javax.ws.rs.core.Response;

import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RoleScopeResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import user.com.vn.user.entites.Role;
import user.com.vn.user.entites.User;

public class KeylcoakUserEventListener extends AbstractKeycloakEventListener<User> {

	public KeylcoakUserEventListener(Keycloak keycloak, String realm) {
		super(keycloak, realm);
	}

	@Override
	public boolean support(Class<?> type) {
		return type.isAssignableFrom(User.class);
	}

	@Override
	public void postCreate(User entity) {
		UserRepresentation userRepresentation = userRepresentation(entity);
		CredentialRepresentation credentialRepresentation = credentialRepresentation(entity);
		userRepresentation.setCredentials(Arrays.asList(credentialRepresentation));
		Response response = user().create(userRepresentation);
		String userId = CreatedResponseUtil.getCreatedId(response);
		mappingRoleWithUser(entity, userId);
		
	}

	@Override
	public void postUpdate(User entity) {
		String userId = getUsername(entity.getUsername()).getId();
		UserRepresentation userRepresentation = userRepresentation(entity);
		userRepresentation.setCredentials(Arrays.asList(credentialRepresentation(entity)));
		user().get(userId).update(userRepresentation);
		mappingRoleWithUser(entity, userId);
	}

	@Override
	public void postDelete(User entity) {
		String id = getUsername(entity.getUsername()).getId();
		user().delete(id);
	}
	
	private UserRepresentation getUsername(String username) {
		return user().search(username).get(0);
	}
	
	private UserRepresentation userRepresentation(User entity) {
		UserRepresentation userRepresentation = new UserRepresentation();
		userRepresentation.setEnabled(true);
		userRepresentation.setUsername(entity.getUsername());
		return userRepresentation;
	}

	private void mappingRoleWithUser(User entity, String userId) {
		for (Role role : entity.getRoles()) {
			UserResource userResource = user().get(userId);
			RoleScopeResource roleScopeResource = userResource.roles().realmLevel();
			List<RoleRepresentation> roles = roleScopeResource.listAll();
			if (roles != null) {
				roleScopeResource.add(role().list(role.getName(), 0, 1));
			}
		}
	}

	private CredentialRepresentation credentialRepresentation(User entity) {
		CredentialRepresentation credentialRepresentation = new CredentialRepresentation();
		credentialRepresentation.setType(CredentialRepresentation.PASSWORD);
		credentialRepresentation.setValue(entity.getPassword());
		return credentialRepresentation;
	}

}
 