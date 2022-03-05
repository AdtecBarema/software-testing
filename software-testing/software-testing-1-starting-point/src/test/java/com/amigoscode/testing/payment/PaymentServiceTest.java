package com.amigoscode.testing.payment;

import com.amigoscode.testing.customer.Customer;
import com.amigoscode.testing.customer.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

class PaymentServiceTest {

    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private CardPaymentCharger cardPaymentCharger;

    private PaymentService underTest;

    @BeforeEach
    void setUp () {
        MockitoAnnotations.initMocks (this);
        underTest = new PaymentService (customerRepository, paymentRepository, cardPaymentCharger);
    }

    @Test
    void itShouldChargeCardSuccessfully () {
        //Given
        UUID customerId = UUID.randomUUID ();

        //Customer exists
        given (customerRepository.findById (customerId)).willReturn (Optional.of (mock (Customer.class)));

        //Payment request
        PaymentRequest paymentRequest = new PaymentRequest (
                new Payment (
                        null,
                        customerId,
                        new BigDecimal ("100.00"),
                        Currency.USD,
                        "card123xxx",
                        "Donation"
                )
        );

        Payment payment = paymentRequest.getPayment ();

        //...Card is charged successfully
        given (cardPaymentCharger.chargeCard (
                payment.getSource (),
                payment.getAmount (),
                payment.getCurrency (),
                payment.getDescription ()
        )).willReturn (new CardPaymentCharge (true));

        //When
        underTest.chargeCard (customerId, paymentRequest);

        //Then
        ArgumentCaptor <Payment> paymentArgumentCaptor =
                ArgumentCaptor.forClass (Payment.class);

        then (paymentRepository).should ().save (paymentArgumentCaptor.capture ());

        Payment paymentArgumentCaptorValue = paymentArgumentCaptor.getValue ();

        assertThat (paymentArgumentCaptorValue)
                .isEqualToIgnoringGivenFields (
                        paymentRequest.getPayment (),
                        "customerId"
                );
        assertThat (paymentArgumentCaptorValue.getCustomerId ()).isEqualTo (customerId);
    }

    @Test
    void itShouldThrowWhenCardIsNotCharged () {
        //Given
        UUID customerId = UUID.randomUUID ();
        //Customer exists
        // given (customerRepository.findById (customerId)).willReturn (Optional.of (mock (Customer.class)));
        given (customerRepository.findById (customerId)).willReturn (Optional.of (mock (Customer.class)));

        //Payment request
        PaymentRequest paymentRequest = new PaymentRequest (
                new Payment (
                        null,
                        customerId,
                        new BigDecimal ("100.00"),
                        Currency.USD,
                        "card123xxx",
                        "Donation"
                )
        );

        //...Card is charged successfully
        given (cardPaymentCharger.chargeCard (
                paymentRequest.getPayment ().getSource (),
                paymentRequest.getPayment ().getAmount (),
                paymentRequest.getPayment ().getCurrency (),
                paymentRequest.getPayment ().getDescription ()
        )).willReturn (new CardPaymentCharge (false));

        //When
        //Then
        assertThatThrownBy (() -> underTest.chargeCard (customerId, paymentRequest))
                .isInstanceOf (IllegalStateException.class)
                .hasMessageContaining (String.format ("Card not debited for customer %s", customerId));

        //...Not interaction with paymentRepository
        then (paymentRepository).shouldHaveNoInteractions ();

    }

    @Test
    void itShouldNotChargeAndThrowWhenCurrencyNotSupported () {

        //Given
        UUID customerId = UUID.randomUUID ();

        //Customer exists
        given (customerRepository.findById (customerId)).willReturn (Optional.of (mock (Customer.class)));

        //Payment request
        PaymentRequest paymentRequest = new PaymentRequest (
                new Payment (
                        null,
                        customerId,
                        new BigDecimal ("100.00"),
                        Currency.EUR,
                        "card123xxx",
                        "Donation"
                )
        );

        String message = String.format ("We dont support currency [%s]", paymentRequest.getPayment ().getCurrency ());
        //When
        assertThatThrownBy (() -> underTest.chargeCard (customerId, paymentRequest))
                .isInstanceOf (IllegalStateException.class)
                .hasMessageContaining (message);

        //Then
        //...No interaction with CardPaymentCharger nor with PaymentRepository
        then (cardPaymentCharger).shouldHaveNoInteractions ();

        then (paymentRepository).shouldHaveNoInteractions ();
    }

    @Test
    void itShouldNotChargeAndThrowWhenCustomerNotFound () {

        //GIVEN
        UUID customerId = UUID.randomUUID ();
        //Customer not found in DB
        given (customerRepository.findById (customerId)).willReturn (Optional.empty ());

        //WHEN
        //THEN
        assertThatThrownBy (() -> underTest.chargeCard (customerId, new PaymentRequest (new Payment ())))
                .isInstanceOf (IllegalStateException.class)
                .hasMessageContaining (String.format ("Customer with id [%s], doesn't exist", customerId));

        //...No interaction with CardPaymentCharger nor with PaymentRepository
        then (cardPaymentCharger).shouldHaveNoInteractions ();
        then (paymentRepository).shouldHaveNoInteractions ();
    }
}