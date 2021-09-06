package user.com.vn.event;

public interface KeycloakEventPublisher {
	
	void publishEvent(EventType eventType, Object entity);
}
