package user.com.vn.event;

public interface KeycloakEventListener<T> {

	boolean support(Class<?> type);
	
	void preUpdate(T entity);
	
	void postCreate(T entity);
	
	void postUpdate(T entity);
	
	void postDelete(T entity);
}
