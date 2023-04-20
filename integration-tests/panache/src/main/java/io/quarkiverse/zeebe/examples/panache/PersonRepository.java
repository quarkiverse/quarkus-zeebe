package io.quarkiverse.zeebe.examples.panache;

import jakarta.enterprise.context.ApplicationScoped;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;

@ApplicationScoped
public class PersonRepository implements PanacheRepositoryBase<Person, String> {

    public Person findByName(String name) {
        return find("name", name).firstResult();
    }

}
