package gr.uoa.di.madgik.interfaces;

import gr.uoa.di.madgik.model.Person;
import gr.uoa.di.madgik.model.PersonHasShapes;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PersonHasShapesRepository extends CrudRepository<PersonHasShapes,String>{


    List<PersonHasShapes> findShapeNameByPersonOrPrivacy(Person person,String privacy);

}
