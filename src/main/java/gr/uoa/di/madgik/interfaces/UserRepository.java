package gr.uoa.di.madgik.interfaces;

import gr.uoa.di.madgik.model.Person;
import gr.uoa.di.madgik.repository.UserSpecs;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends CrudRepository<Person, String>, JpaSpecificationExecutor {

    Optional<Person> findById(Long id);

    Optional<Person> findByEmail(String email);

    Optional<Person> findByEmailAndPassword(String email, String password);


}
