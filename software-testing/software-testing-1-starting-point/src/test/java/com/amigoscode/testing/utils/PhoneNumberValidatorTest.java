package com.amigoscode.testing.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

public class PhoneNumberValidatorTest {
    private PhoneNumberValidator underTest;

    @BeforeEach
    void setUp () {
        underTest = new PhoneNumberValidator();
    }

    @ParameterizedTest
    @CsvSource ( {"+447000000000,true",
            "+447000000000000,false",
            "+427000000000,false"
    })
    void itShouldValidatePhoneNumber (String phoneNumber, boolean expected) {
        //GIVEN
       // String phoneNumber="+447000000000";

        //WHEN
        boolean isValid=underTest.test (phoneNumber);
        //THEN
        assertThat(isValid).isEqualTo(expected);
    }


    @Test
    @DisplayName ("Should fail when length greater than 13")
    void itShouldValidatePhoneNumberWhenIncorrectAndHasLengthGreaterThan13 () {
        //GIVEN
        String phoneNumber="+447000000000000";

        //WHEN
        boolean isValid=underTest.test (phoneNumber);

        //THEN
        assertThat(isValid).isFalse ();
    }

    @Test
    @DisplayName ("Should fail when doesn't start with +44")
    void itShouldValidatePhoneNumberWhenIncorrectAndDoesNotStartWith44() {
        //GIVEN
        String phoneNumber="+427000000000";

        //WHEN
        boolean isValid=underTest.test (phoneNumber);

        //THEN
        assertThat(isValid).isFalse ();
    }
}