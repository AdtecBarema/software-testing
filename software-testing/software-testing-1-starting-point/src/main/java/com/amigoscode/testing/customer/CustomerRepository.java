package com.amigoscode.testing.customer;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerRepository extends CrudRepository<Customer,UUID> {
    //create a customer method which will check if a phone number is already in the DB
    @Query(value = "select id, name,  phone_number from customer where phone_number=:phone_number ", nativeQuery = true)
    Optional<Customer> selectCustomerByPhoneNumber(@Param ("phone_number") String phonenumber);
}