package gr.uoa.di.madgik.model;

import io.swagger.annotations.ApiModel;

import javax.persistence.*;

@ApiModel(value="person_has_shapes", description="Sample metada  model for the documentation")
@Table(name="person_has_shapes")
@Entity
@NamedQuery(name="PersonHasShapes.findAll", query="SELECT p FROM PersonHasShapes p")
public class PersonHasShapes {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="shape_id",columnDefinition = "serial")
    private Long shapeId;

    @ManyToOne
    @JoinColumn(name="person_id")//(name = "id") //,foreignKey=@ForeignKey(name="person_id"),
    private Person person;

    @Column(name="shape_name")
    private String shapeName;

    @Column
    private String privacy;

    public Long getShapeId() {
        return shapeId;
    }

    public void setShapeId(Long shapeId) {
        this.shapeId = shapeId;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public String getShapeName() {
        return shapeName;
    }

    public void setShapeName(String shapeName) {
        this.shapeName = shapeName;
    }

    public String getPrivacy() {
        return privacy;
    }

    public void setPrivacy(String privacy) {
        this.privacy = privacy;
    }
}
