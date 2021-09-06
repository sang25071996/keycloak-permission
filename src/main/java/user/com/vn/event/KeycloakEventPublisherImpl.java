package user.com.vn.event;

public class KeycloakEventPublisherImpl implements KeycloakEventPublisher {

	private EventType eventType;
	private Object entity;
	
	@Override
	public void publishEvent(EventType eventType, Object entity) {
		this.eventType = eventType;
		this.entity = entity;
	}

	public EventType getEventType() {
		return eventType;
	}

	public Object getEntity() {
		return entity;
	}
	
}
