package user.com.vn.user.entites;

import java.io.Serializable;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;
import user.com.vn.entites.BaseEntity;

@Entity
@Table(name = "PRIVILEGE", schema = "USERS")
@Getter
@Setter
public class Privilege extends BaseEntity implements Serializable {
	
	private static final long serialVersionUID = 2332580096507798314L;

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name = "name")
	private String name;
	
	@ManyToMany(mappedBy = "privileges", cascade = CascadeType.ALL)
	private Set<Role> roles;
	
	@ManyToOne
	@JoinColumn(name = "resource_id")
	private Resource resource;
}
