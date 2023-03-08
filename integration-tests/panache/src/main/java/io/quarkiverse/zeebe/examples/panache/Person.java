package io.quarkiverse.zeebe.examples.panache;

import java.time.LocalDate;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Person {
    @Id
    @Column(name = "ID")
    public String id = UUID.randomUUID().toString();
    @Column(name = "NAME")
    public String name;
    @Column(name = "BIRTH")
    public LocalDate birth;
    @Column(name = "AGE")
    public Integer age;

}
