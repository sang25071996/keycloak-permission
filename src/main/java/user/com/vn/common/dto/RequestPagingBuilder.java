package user.com.vn.common.dto;

import java.io.Serializable;

import org.springframework.data.domain.Sort;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class RequestPagingBuilder<T>  implements Serializable{
	
	private String[] fieldsOrderBy;
	private transient T filters;
	private Sort.Direction sortBy;
	private int page;
	private int size;
}
