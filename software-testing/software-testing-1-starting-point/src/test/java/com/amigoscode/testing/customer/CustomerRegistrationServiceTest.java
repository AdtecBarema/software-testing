package com.amigoscode.testing.customer;

import com.amigoscode.testing.utils.PhoneNumberValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

class CustomerRegistrationServiceTest {
    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private PhoneNumberValidator phoneNumberValidator;

    @Captor
    private ArgumentCaptor <Customer> customerArgumentCaptor;

    private CustomerRegistrationService underTest;

    @BeforeEach
    void setUp () {
        //Initialize all classes with @Mock annotation in this class
        MockitoAnnotations.initMocks (this);
        underTest = new CustomerRegistrationService (customerRepository, phoneNumberValidator);
    }

    @Test
    void itShouldSaveNewCustomer () {
        //Given a phone number and a customer
        String phoneNumber = "000099";
        Customer customer = new Customer (UUID.randomUUID (), "Maryam", phoneNumber);
        //...a request
        CustomerRegistrationRequest request = new CustomerRegistrationRequest (customer);
        //...an existing a  customer is returned
        given (customerRepository.selectCustomerByPhoneNumber (phoneNumber))
                .willReturn (Optional.empty ());

        //...Valid phone number
        given (phoneNumberValidator.test (phoneNumber)).willReturn (true);

        //When
        underTest.registerNewCustomer (request);

        //Then
        then (customerRepository).should ().save (customerArgumentCaptor.capture ());

        Customer customerArgumentCaptorValue = customerArgumentCaptor.getValue ();
        assertThat (customerArgumentCaptorValue).isEqualToComparingFieldByField (customer);
    }

    @Test
    void itShouldSaveNewCustomerWhenIdIsNull () {
        //Given a phone number and a customer
        String phoneNumber = "000099";
        Customer customer = new Customer (null, "Maryam", phoneNumber);

        //...a request
        CustomerRegistrationRequest request = new CustomerRegistrationRequest (customer);

        //...an existing a  customer is returned
        given (customerRepository.selectCustomerByPhoneNumber (phoneNumber))
                .willReturn (Optional.empty ());
//...Valid phone number
        given (phoneNumberValidator.test (phoneNumber)).willReturn (true);
        //When
        underTest.registerNewCustomer (request);

        //Then
        then (customerRepository).should ().save (customerArgumentCaptor.capture ());
        Customer customerArgumentCaptorValue = customerArgumentCaptor.getValue ();
        assertThat (customerArgumentCaptorValue.getId ()).isNotNull ();
    }

    @Test
    void itShouldNotSaveCustomerWhenCustomerExists () {
        //Given a phone number and a customer
        String phoneNumber = "000099";
        UUID id = UUID.randomUUID ();
        Customer customer = new Customer (id, "Maryam", phoneNumber);
        //...a request
        CustomerRegistrationRequest request = new CustomerRegistrationRequest (customer);
        //...an existing a  customer is returned
        given (customerRepository.selectCustomerByPhoneNumber (phoneNumber))
                .willReturn (Optional.of (customer));

        //...Valid phone number
        given (phoneNumberValidator.test (phoneNumber)).willReturn (true);
        //When
        underTest.registerNewCustomer (request);
        //Then
        then (customerRepository).should (never ()).save (any ());
    }

    @Test
    void itShouldThrowWhenPhoneNumberIsTaken () {
        //Given a phone number and a customer
        String phoneNumber = "000099";
        UUID id = UUID.randomUUID ();
        Customer customer = new Customer (id, "Maryam", phoneNumber);
        Customer customerTwo = new Customer (id, "John", phoneNumber);

        //...a request
        CustomerRegistrationRequest request = new CustomerRegistrationRequest (customer);

        //...an existing a  customer is returned
        given (customerRepository.selectCustomerByPhoneNumber (phoneNumber))
                .willReturn (Optional.of (customerTwo));

        //...Valid phone number
        given (phoneNumberValidator.test (phoneNumber)).willReturn (true);

        //When...together......with //Then
        assertThatThrownBy (() -> underTest.registerNewCustomer (request))
                .isInstanceOf (IllegalStateException.class)
                .hasMessageContaining (String.format ("phone number [%s] is taken", phoneNumber));
        //Finally
        then (customerRepository).should (never ()).save (any (Customer.class));
    }
}