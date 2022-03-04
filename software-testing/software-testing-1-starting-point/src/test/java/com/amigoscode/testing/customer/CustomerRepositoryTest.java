package com.amigoscode.testing.customer;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
class CustomerRepositoryTest {

    @Autowired
    private CustomerRepository underTest;


    @Test
    void itShouldSelectCustomerByPhoneNumber () {
        //Given

        //WHEN
        //THEN

    }

    @Test
    void itShouldSaveCustomer () {

        //given
        UUID id = UUID.randomUUID ();
        Customer customer = new Customer (id, "Abel", "00000");

        //When
        underTest.save (customer);

        //Then
        Optional <Customer> optionalCustomer = underTest.findById (id);
        assertNotNull (underTest);
        assertThat (optionalCustomer)
                .isPresent ()
                .hasValueSatisfying (c -> {
//                    assertThat (c.getId ()).isEqualTo (id);
//                    assertThat (c.getName ()).isEqualToIgnoringCase ("Abel");
//                    assertThat (c.getPhoneNumber ()).isEqualToIgnoringCase ("00000");
                    //Alternatively, instead of checking each field, we can use one wrapper method to check fields
                    assertThat (c).isEqualToComparingFieldByField (customer);
                });
    }
}