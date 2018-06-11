package gr.uoa.di.madgik.service;

import gr.uoa.di.madgik.interfaces.PersonHasShapesRepository;
import gr.uoa.di.madgik.interfaces.UserRepository;
import gr.uoa.di.madgik.model.PersonHasShapes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import gr.uoa.di.madgik.model.Person;

import java.util.List;
import java.util.Optional;


@Service
public class UserService {

	private UserRepository userRepository;

	private PersonHasShapesRepository personHasShapesRepository;

	@Autowired
	public UserService() {

	}

	@Autowired
	public void setUserRepository(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Autowired
	public void setPersonHasShapesRepository(PersonHasShapesRepository personHasShapesRepository) {
		this.personHasShapesRepository = personHasShapesRepository;
	}



	public  Optional<Person> isUserAuthorized(String email, String password){
		final Optional<Person> foundUser = userRepository.findByEmail(email);
		if (foundUser.isPresent()
				&& PasswordService.checkPassword(password, foundUser.get().getPassword())) {
//			final Token token = AuthUtils.createToken(request.getRemoteHost(), String.valueOf(foundUser.get().getId()));
			return foundUser;
		}
		
		return null;
	}

	public Person findOneByUsername(String username, Object pass3) {

		return null;
	}

	public List<PersonHasShapes> findLayersByPerson(Person person){
		return personHasShapesRepository.findShapeNameByPersonOrPrivacy(person,"public");
	}

	public PersonHasShapes savePersonHasShapes( String email, String shapeName, String privacy){
		Optional<Person> foundUser = userRepository.findByEmail(email);
		if (foundUser.isPresent()) {
			System.out.println("user id:"+foundUser.get().getId());
			PersonHasShapes personHasShapes = new PersonHasShapes();
			personHasShapes.setPerson(foundUser.get());
			personHasShapes.setShapeName(shapeName);
			personHasShapes.setPrivacy(privacy);
			return personHasShapesRepository.save(personHasShapes);
		}
		return null;
	}

	
}
