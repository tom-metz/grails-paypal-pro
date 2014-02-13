package com.solution51.paypalpro

/* Copyright 2009-2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the 'License')
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
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

import org.codehaus.groovy.grails.web.binding.DataBindingUtils

public class PaymentRequest {

    String profileId = ''

    String profileNote = ''

    Double amount = 0

    String cardType = ''

    String cardNumber = ''

    String cardIssueNumber = ''

    String cardStartMonth = ''

    String cardStartYear = ''

    String cardEndMonth = ''

    String cardEndYear = ''

    String cardVerificationValue = ''

    String email = ''

    String firstName = ''

    String lastName = ''

    String street = ''

    String city = ''

    String state = ''

    String zip = ''

    String countryCode = ''

    String currencyCode = ''

    String ipAddress = ''

    Date recurringBillingStartDate

    String recurringBillingPeriod = ''

    String recurringBillingFreqency = ''

    String recurringBillingDescription = ''

    String recurringBillingReference = ''

    Integer recurringBillingMaxFailedPayments = 3

    Double recurringBillingInitialAmount = 0

    String recurringBillingInitialAmountFailAction = 'CancelOnFailure'

    public setProperties(Object o) {
        DataBindingUtils.bindObjectToInstance(this, o)
    }

    public void setBillingDaily(Date startDate) {
        setBilling('Day', '1', startDate)
    }

    public void setBillingMonthly(Date startDate) {
        setBilling('Month', '1', startDate)
    }

    public void setBillingWeekly(Date startDate) {
        setBilling('Week', '1', startDate)
    }

    public void setBillingAnnually(Date startDate) {
        setBilling('Year', '1', startDate)
    }

    private Date setBilling(String period, String frequency, Date startDate) {
        recurringBillingPeriod = period
        recurringBillingFreqency = frequency
        recurringBillingStartDate = startDate
    }

}
