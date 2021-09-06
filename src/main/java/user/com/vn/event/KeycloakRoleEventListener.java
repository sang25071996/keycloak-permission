package user.com.vn.event;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.RoleRepresentation;

import user.com.vn.user.entites.Role;

public class KeycloakRoleEventListener extends AbstractKeycloakEventListener<Role> {

	public KeycloakRoleEventListener(Keycloak keycloak, String realm) {
		super(keycloak, realm);
	}

	@Override
	public boolean support(Class<?> type) {
		return type.isAssignableFrom(Role.class);
	}

	@Override
	public void postCreate(Role role) {
		role().create(roleRepresentation(role));
		
	}

	@Override
	public void postUpdate(Role role) {
		role().get(role.getName()).update(roleRepresentation(role));
		
	}

	@Override
	public void postDelete(Role role) {
		role().deleteRole(role.getName());
		
	}

	private RoleRepresentation roleRepresentation(Role role) {
		RoleRepresentation roleRepresentation = new RoleRepresentation();
		roleRepresentation.setName(role.getName());
		return roleRepresentation;
	}
}
