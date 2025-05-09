package com.siemens.internship.model;

import com.siemens.internship.validation.EmailConstraint;
import com.siemens.internship.validation.EmailValidator;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String name;
    private String description;
    private String status;

    // Could've used the jpa built in JPA @Email validator annotation
    // but by using a custom validator we can add more logic to validations
    // (for example checking if the email is unique in the database, check
    // if it has a valid domain, etc.)
    @EmailConstraint
    private String email;
}