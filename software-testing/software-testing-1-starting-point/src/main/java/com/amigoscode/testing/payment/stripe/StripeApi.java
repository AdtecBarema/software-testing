package com.amigoscode.testing.payment.stripe;

import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import com.stripe.net.RequestOptions;
import org.springframework.stereotype.Service;

import java.util.Map;
//This service class is created to mock static method call to the Charge.creat() method
//By doing so, we can avoid using an other framework, powermock which is know for handling static method mocking

@Service
public class StripeApi {
   public Charge create(Map <String,Object> reuestMap, RequestOptions options) throws StripeException {
      return  Charge.create (reuestMap,options);
   }
}