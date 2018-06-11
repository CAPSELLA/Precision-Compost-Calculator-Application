package gr.uoa.di.madgik.repository;

import gr.uoa.di.madgik.model.Person;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

public class UserSpecs {

    public static Specification<Person> findByProvider(Person.Provider provider, String id) {
        return new Specification<Person>() {
            public Predicate toPredicate(Root<Person> root, CriteriaQuery<?> query,
                                         CriteriaBuilder builder) {

                return builder.lessThan(root.<String>get(provider.getName()), id);
            }
        };
    }
}
