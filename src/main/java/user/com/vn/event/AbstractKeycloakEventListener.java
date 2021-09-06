package user.com.vn.event;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.ClientsResource;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.admin.client.resource.UsersResource;

public abstract class AbstractKeycloakEventListener<T> implements KeycloakEventListener<T> {

	Keycloak keycloak;
	
	String realm;
	
	private UsersResource usersResource;
	private RolesResource rolesResource;
	private ClientsResource clientsResource;
	
	protected AbstractKeycloakEventListener(Keycloak keycloak, String realm) {
		this.keycloak = keycloak;
		this.realm = realm;
	}
	
	public UsersResource user() {
		if (usersResource == null) {
			usersResource = keycloak.realm(realm).users();
		}
		return usersResource;
	}
	
	public RolesResource role() {
		if (rolesResource == null) {
			rolesResource = keycloak.realm(realm).roles();
		}
		return rolesResource;
	}
	
	public ClientsResource client() {
		if (clientsResource == null) {
			clientsResource = keycloak.realm(realm).clients();
		}
		return clientsResource;
	}
	
	@Override
	public void preUpdate(T entity) {

	}

	@Override
	public void postCreate(T entity) {

	}

	@Override
	public void postUpdate(T entity) {

	}

	@Override
	public void postDelete(T entity) {

	}

}
