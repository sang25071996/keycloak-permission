package user.com.vn.configuration;

import java.util.ArrayList;
import java.util.List;

import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.keycloak.OAuth2Constants;
import org.keycloak.adapters.springboot.KeycloakSpringBootProperties;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import user.com.vn.event.EventType;
import user.com.vn.event.KeycloakEventListener;
import user.com.vn.event.KeycloakEventPublisher;
import user.com.vn.event.KeycloakRoleEventListener;
import user.com.vn.event.KeylcoakPermissionEventListener;
import user.com.vn.event.KeylcoakUserEventListener;

@Configuration
@EnableConfigurationProperties(KeycloakSpringBootProperties.class)
public class KeycloakConfig {
	
	@Value("${keycloak.realm}")
	private String realm;
	@Value("${keycloak.resource}")
	private String clientId;
	
	@Autowired
	private KeycloakSpringBootProperties props;
	
	@Bean
	public Keycloak keycloak() {
		String clientSecret = props.getCredentials().get("secret").toString();
		return KeycloakBuilder.builder().serverUrl(props.getAuthServerUrl())
				.realm(realm)
				.clientId(clientId)
				.grantType(OAuth2Constants.CLIENT_CREDENTIALS)
				.clientSecret(clientSecret)
				.resteasyClient(new ResteasyClientBuilder().connectionPoolSize(20).build())
				.build();
	}
	
	@Bean
	@Order(1)
	public KeycloakRoleEventListener keycloakRoleEventListener() {
		return new KeycloakRoleEventListener(keycloak(), realm);
	}
	
	@Bean
	@Order(2)
	public KeylcoakUserEventListener keylcoakUserEventListener() {
		return new KeylcoakUserEventListener(keycloak(), realm);
	}
	
	@Bean
	@Order(3)
	public KeylcoakPermissionEventListener keylcoakPermissionEventListener() {
		return new KeylcoakPermissionEventListener(keycloak(), realm);
	}
	
	@SuppressWarnings({"rawtypes", "unchecked"})
	@Bean
	public KeycloakEventPublisher keycloakEventPublisher(KeycloakEventListener[] keycloakEventListeners) {
		return new KeycloakEventPublisher() {

			@Override
			public void publishEvent(EventType eventType, Object entity) {
				
				Class<?> entityType = entity.getClass();
				List<KeycloakEventListener> list = new ArrayList<>();
				for(KeycloakEventListener keycloakEventListener : keycloakEventListeners) {
					if (keycloakEventListener.support(entityType)) {
						list.add(keycloakEventListener);
					}
				}
				
				switch (eventType) {
				case CREATE:
					for (KeycloakEventListener event : list) {
						event.postCreate(entity);
					}
					break;
				case UPDATE:
					for (KeycloakEventListener event : list) {
						event.postUpdate(entity);
					}
					break;
				case PRE_UPDATE:
					for (KeycloakEventListener event : list) {
						event.preUpdate(entity);
					}
					break;
				default:
					break;
				}
			}
			
		};
	}
}
