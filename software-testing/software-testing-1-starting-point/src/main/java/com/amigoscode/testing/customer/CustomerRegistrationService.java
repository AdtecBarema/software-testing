package com.amigoscode.testing.customer;

import com.amigoscode.testing.utils.PhoneNumberValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class CustomerRegistrationService {

    private final CustomerRepository customerRepository;
    private final PhoneNumberValidator phoneNumberValidator;

    @Autowired
    public CustomerRegistrationService (CustomerRepository customerRepository, PhoneNumberValidator phoneNumberValidator) {
        this.customerRepository = customerRepository;
        this.phoneNumberValidator = phoneNumberValidator;
    }



    public void registerNewCustomer (CustomerRegistrationRequest request) {
        //1. check if phoneNumber is taken
        //2. if taken lets check if it belongs to the same customer
        //-2.1 if yes return
        //-2.2 throw an exception
        //3. save customer
        String phoneNumber = request.getCustomer ().getPhoneNumber ();
        //TODO: Validate that the phone number is valid
        if(!phoneNumberValidator.test (phoneNumber)){
            throw new IllegalStateException (String.format ("Phone number [%s] is not valid",phoneNumber));}

        Optional <Customer> customerOptional = customerRepository.selectCustomerByPhoneNumber (phoneNumber);


        if (customerOptional.isPresent ()){
            Customer customer=customerOptional.get ();
            if(customer.getName ().equals (request.getCustomer ().getName ())){
                return;
            }
            throw new IllegalStateException (String.format ("phone number [%s] is taken", phoneNumber));
        }

        if(request.getCustomer ().getId ()==null){
            request.getCustomer ().setId (UUID.randomUUID ());
        }

        customerRepository.save (request.getCustomer ());
    }


}