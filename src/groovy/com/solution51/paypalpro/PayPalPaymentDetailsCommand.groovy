package com.solution51.paypalpro

import grails.validation.Validateable

/* Copyright 2009-2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0  the "License" ;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * For more information please visit www.solution51.com
 * or email info@solution51.com
 * Author: Peter Delahunty
 * Email: peter.delahunty@solution51.com
 * Date: 26-May-2009
*/

@Validateable
public class PayPalPaymentDetailsCommand {

    String cardType
    String cardNumber

    String cardEndMonth
    String cardEndYear

    String cardIssueNumber

    String cardStartMonth
    String cardStartYear

    String cardVerificationValue

    String firstName
    String lastName

    String street
    String city
    String state
    String zip
    String countryCode

    String email
    String amount
    String currencyCode
    String description

    String ipAddress

    def payPalErrorMessages

    static constraints = {
        cardType blank: false
        cardNumber blank: false 
        cardEndMonth blank: false 
        cardEndYear blank: false 
        cardVerificationValue blank: false 

        firstName blank: false 
        lastName blank: false 
        street blank: false 
        city blank: false 
        zip blank: false 
        countryCode blank: false 
    }

}
