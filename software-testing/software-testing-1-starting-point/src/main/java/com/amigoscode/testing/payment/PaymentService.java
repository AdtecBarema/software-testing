package com.amigoscode.testing.payment;

import com.amigoscode.testing.customer.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class PaymentService {
    private static final List <Currency> ACCEPTED_CURRENCIES = List.of (Currency.GPB, Currency.USD);

    private final CustomerRepository customerRepository;
    private final PaymentRepository paymentRepository;
    private final CardPaymentCharger cardPaymentCharger;

    @Autowired
    public PaymentService (CustomerRepository customerRepository,
                           PaymentRepository paymentRepository,
                           CardPaymentCharger cardPaymentCharger) {
        this.customerRepository = customerRepository;
        this.paymentRepository = paymentRepository;
        this.cardPaymentCharger = cardPaymentCharger;
    }

    void chargeCard (UUID customerId, PaymentRequest paymentRequest) {
        Payment payment = paymentRequest.getPayment ();

        boolean isCustomerFond = customerRepository.findById (customerId).isPresent ();

        //1. Does customer exists if not throw
        if (!isCustomerFond) {
            throw new IllegalStateException (String.format ("Customer with id [%s], doesn't exist", customerId));
        }

        //2. Do we support the currency, if not throw


        boolean isCurrencySupported = ACCEPTED_CURRENCIES.stream ()
                .anyMatch (c -> c.equals (paymentRequest.getPayment ().getCurrency ()));

        if (!isCurrencySupported) {
            String message = String.format ("We dont support currency [%s]", paymentRequest.getPayment ().getCurrency ());
            throw new IllegalStateException (message);
        }

        //3.Charge card
       CardPaymentCharge cardPaymentCharge= cardPaymentCharger.chargeCard (
                                payment.getSource (),
                                payment.getAmount (),
                                payment.getCurrency (),
                                payment.getDescription ()
        );

        //4. If not debited throw an exception
        if(!cardPaymentCharge.isCardDebited ())
        {
            throw  new IllegalStateException (String.format ("Card not debited for customer %s", customerId));
        }

       //5. Insert payment
        paymentRequest.getPayment ().setCustomerId (customerId);
        paymentRepository.save (paymentRequest.getPayment ());

        //6. TODO: send sms
    }
}