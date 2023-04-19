package io.quarkiverse.zeebe.examples.panache;

import java.time.LocalDate;
import java.time.Period;
import java.util.Map;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import io.quarkiverse.zeebe.JobWorker;
import io.quarkiverse.zeebe.Variable;
import io.quarkiverse.zeebe.VariablesAsType;

public class PersonJobWorker {

    @Inject
    PersonRepository repository;

    @JobWorker(type = "createPerson")
    @Transactional
    public Person createPerson(@Variable("_name") String name, @Variable("_birth") LocalDate birth) {
        Person person = new Person();
        person.birth = birth;
        person.name = name;
        repository.persistAndFlush(person);
        return person;
    }

    @JobWorker(type = "calculatePersonAge")
    public Map<String, Object> createPerson(@VariablesAsType Person person) {
        int age = Period.between(person.birth, LocalDate.now()).getYears();
        return Map.of("_age", age);
    }

    @JobWorker(type = "updatePersonAge")
    @Transactional
    public Person updatePersonPairedYear(@Variable("_age") int age, @VariablesAsType Person person) {
        Person tmp = repository.findById(person.id);
        tmp.age = age;
        repository.persistAndFlush(tmp);
        return tmp;
    }

}
