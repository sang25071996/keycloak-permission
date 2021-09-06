package user.com.vn.user.entites;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "RESOURCE", schema = "USERS")
@Getter
@Setter
public class Resource implements Serializable {

	private static final long serialVersionUID = -5639681670398091260L;

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	@Column(name = "code")
	private String code;
	
	@Column(name = "name")
	private String name;
	
	@Column(name = "client_id")
	private String clientId;
	
	@OneToMany(mappedBy = "resource", fetch = FetchType.LAZY)
	private List<Privilege> privileges = new ArrayList<>();
}
