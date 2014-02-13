package com.solution51.paypalpro
/* Copyright 2009-2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
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


class PayPalBilling {

    static mapping = {
        table 'pp_billing'
    }

    static hasMany = [billingHistory:PayPalBillingHistory]

    Date dateCreated
    Date lastUpdated
    PayPalStatus status

    String profileId;
    String profileStatus;

    Integer maxFailedPayments = 3
    String referenceId
    String description
    Date startDate
    Double initialAmount
    Double amount;
    String currencyCode;
    PayPalBillingPeriod period
    Integer frequency

    static constraints = {
        description(blank:false)
        referenceId(blank:false)
        currencyCode(blank:false)
        status(nullable:true)
        profileId(nullable:true)
        profileStatus(nullable:true)
    }

    Date calculateNextPaymentDate(){
        Calendar today = getToday()
        Calendar nextDate = Calendar.instance
        nextDate.time = startDate

        if(period == PayPalBillingPeriod.WEEK){
            long todayMilli = today.getTimeInMillis()
            long startDateMilli = nextDate.getTimeInMillis();
            long diffMilli = todayMilli - startDateMilli
            long diffDays = diffMilli / (24 * 60 * 60 * 1000);
            int weeks = diffDays / 7
            nextDate.add(Calendar.WEEK_OF_YEAR,weeks)
        }else{
            if(period == PayPalBillingPeriod.DAY){
                nextDate.set(Calendar.DATE,today.get(Calendar.DATE))
                nextDate.set(Calendar.MONTH,today.get(Calendar.MONTH))
                nextDate.set(Calendar.YEAR,today.get(Calendar.YEAR))
            }else if(period == PayPalBillingPeriod.MONTH){
                nextDate.set(Calendar.MONTH,today.get(Calendar.MONTH))
                nextDate.set(Calendar.YEAR,today.get(Calendar.YEAR))
            }else if(period == PayPalBillingPeriod.YEAR){
                nextDate.set(Calendar.YEAR,today.get(Calendar.YEAR))
            }
        }

        int result = today.compareTo(nextDate)

        if(result > 0){
            // date already passed
            if(period == PayPalBillingPeriod.DAY){
                nextDate.add(Calendar.DATE,frequency)
            }else if(period == PayPalBillingPeriod.WEEK){
                nextDate.add(Calendar.WEEK_OF_YEAR,frequency)
            }else if(period == PayPalBillingPeriod.MONTH){
                nextDate.add(Calendar.MONTH,frequency)
            }else if(period == PayPalBillingPeriod.YEAR){
                nextDate.add(Calendar.YEAR,frequency)
            }

        }
        return nextDate.getTime()
    }

    private java.util.Calendar getToday() {
        return Calendar.instance
    }
}
