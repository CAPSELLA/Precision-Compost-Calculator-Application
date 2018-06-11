package gr.uoa.di.madgik.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import io.swagger.annotations.ApiModel;
import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.GenericGenerator;

import java.io.Serializable;
import java.util.List;

import javax.naming.Name;
import javax.persistence.*;

@ApiModel(value="person", description="Sample metada  model for the documentation")
@Table(name="person")
@Entity
@NamedQuery(name="Person.findAll", query="SELECT p FROM Person p")
public class Person implements Serializable{

	private static final long serialVersionUID = -6053040681013124894L;

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "serial")
    private Long id;

	@Column
    private String email;

	@Column
    private String password;

    @Column(name = "facebook")
    private String facebook;

    @Column(name = "google")
    private String google;


	public Long getId() {
		return id;
	}

	public void setId(Long uid) {
		this.id = uid;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}


    public String getFacebook() {
        return facebook;
    }

    public void setFacebook(String facebook) {
        this.facebook = facebook;
    }

    public String getGoogle() {
        return google;
    }

    public void setGoogle(String google) {
        this.google = google;
    }

    public enum Provider {
        FACEBOOK("facebook"), GOOGLE("google");

        String name;

        Provider(final String name) {
            this.name = name;
        }

        public String getName() {
            return this.name;
        }

        public String capitalize() {
            return StringUtils.capitalize(this.name);
        }
    }
    public void setProviderId(final Provider provider, final String value) {
        switch (provider) {
            case FACEBOOK:
                this.facebook = value;
                break;
            case GOOGLE:
                this.google = value;
                break;
            default:
                throw new IllegalArgumentException();
        }
    }

}
