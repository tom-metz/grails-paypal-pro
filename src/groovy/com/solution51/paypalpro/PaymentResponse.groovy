package com.solution51.paypalpro

/* Copyright 2009-2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
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

public class PaymentResponse {

    public PaymentResponse(Boolean success) {
        this.success = success
    }

    private String profileId
    private String profileStatus
    private String transactionId
    private String amount
    private String avsCode
    private String cardVerificationValueMatch

    private Boolean success

    private Collection<PaymentError> errorMessages

}
