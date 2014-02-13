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

import com.paypal.sdk.core.nvp.NVPEncoder
import com.paypal.sdk.core.nvp.NVPDecoder
import com.paypal.sdk.profiles.APIProfile
import com.paypal.sdk.core.nvp.NVPAPICaller
import com.paypal.sdk.profiles.ProfileFactory
import org.codehaus.groovy.grails.commons.ConfigurationHolder

class PayPalProService {

    static final String testEnv = "sandbox"

    static final String devCentral = "developer"

    static final String DEFAULT_USER_NAME = "sdk-three_api1.sdk.com"

    static final String DEFAULT_PASSWORD = "QFZCWN5HZM8VBG7Q"

    static final String DEFAULT_SIGNATURE = "A.d9eRKfd1yVkRrtmMfCFLTqa6M9AyodL0SJkhYztxUi8W9pCXF6.4NI"

    static final String METHOD_DO_DIRECT_PAYMENT = "DoDirectPayment"

    static final String METHOD_CREATE_RECURRING_PAYMENTS_PROFILE = "CreateRecurringPaymentsProfile"

    static final String METHOD_MANAGE_RECURRING_PAYMENTS_PROFILE = "ManageRecurringPaymentsProfileStatus"

    static final String ACTION_RECURRING_CANCEL = "Cancel"

    static final String ACTION_RECURRING_SUSPEND = "Suspend"

    static final String ACTION_RECURRING_REACTIVATE = "Reactivate"

    static final String ACTION_PAYMENT_SALE = "Sale"

    static final String ACTION_PAYMENT_AUTHORIZATION = "Authorization"

    boolean transactional = true

    def createMonthlyRecurringPayment(PayPalPaymentDetailsCommand paymentDetails, PayPalBilling billing, String note = "Create") {

        calculatePaymentStartDate(billing, 1, Calendar.MONTH)
        billing.period = PayPalBillingPeriod.MONTH
        billing.frequency = 1

        if (paymentDetails.validate() && billing.validate()) {
            PaymentRequest paymentRequest = new PaymentRequest()
            paymentRequest.profileNote = note
            paymentRequest.properties = paymentDetails.properties
            paymentRequest.currencyCode = billing.currencyCode

            paymentRequest.amount = billing.amount
            paymentRequest.recurringBillingInitialAmount = billing.initialAmount

            paymentRequest.recurringBillingReference = billing.referenceId
            paymentRequest.recurringBillingDescription = billing.description

            paymentRequest.setBillingMonthly(billing.startDate)

            PaymentResponse paymentResponse = doCreateRecurringPayment(paymentRequest)

            if (paymentResponse.success) {

                if (log.traceEnabled) log.trace("PayPalPro (createMonthlyRecurringPayment) create recurring payments profile SUCCESSFUL: profileId:$paymentResponse.profileId status:$paymentResponse.profileStatus")

                billing.profileId = paymentResponse.profileId
                billing.profileStatus = paymentResponse.profileStatus
                billing.status = PayPalStatus.ACTIVE

                PayPalBillingHistory billingHistory = new PayPalBillingHistory()
                billingHistory.profileId = billing.profileId
                billingHistory.profileStatus = billing.profileStatus
                billingHistory.action = PayPalBillingAction.START
                billingHistory.amount = billing.amount
                billingHistory.currencyCode = billing.currencyCode

                billing.addToBillingHistory(billingHistory)

                if (billing.save()) {
                    return true
                } else {
                    log.error("PayPalPro failed to create PayPalBilling and PayPalBillingHistory")
                    return false
                }

            } else {
                log.warn("PayPalPro create recurring payments profile FAILED. Enable trace log to see errors")

                if (log.traceEnabled) {
                    paymentResponse.errorMessages.each {error ->
                        log.trace "PayPal error msg: code:$error.code severityCode:$error.severityCode longMessage:$error.longMessage shortMessage:$error.shortMessage"
                    }
                }

                paymentDetails.payPalErrorMessages = paymentResponse.errorMessages
                return false
            }

        } else {
            log.warn("PayPalPro (createMonthlyRecurringPayment) domain object validation failed. Enable trace log to see errors")

            if (log.traceEnabled) {
                paymentDetails.errors.allErrors.each {error ->
                    log.trace(error)
                }
                billing.errors.allErrors.each {error ->
                    log.trace(error)
                }

            }
            return false
        }
    }

    def cancelMonthlyRecurringPayment(PayPalBilling billing, String changeNote = "Cancel") {
        PaymentRequest paymentRequest = new PaymentRequest()
        paymentRequest.profileId = billing.profileId
        paymentRequest.profileNote = changeNote
        PaymentResponse paymentResponse = doCancelRecurringPayment(paymentRequest)

        if (paymentResponse.success) {

            billing.status = PayPalStatus.CANCELLED
            PayPalBillingHistory billingHistory = new PayPalBillingHistory()
            billingHistory.profileId = billing.profileId
            billingHistory.profileStatus = "CancelledProfile"
            billingHistory.action = PayPalBillingAction.CANCEL
            billing.addToBillingHistory(billingHistory)

            return billing.save()

        } else {
            if (log.traceEnabled) {
                paymentResponse.errorMessages.each {error ->
                    log.trace "PayPal error msg: code:$error.code severityCode:$error.severityCode longMessage:$error.longMessage shortMessage:$error.shortMessage"
                }
            }
        }

    }

    def doSinglePaymentSale(PayPalPaymentDetailsCommand paymentDetails, PayPalPayment payment) {

        if (paymentDetails.validate() && payment.validate()) {
            PaymentRequest paymentRequest = new PaymentRequest()
            paymentRequest.properties = paymentDetails.properties
            paymentRequest.amount = payment.amount
            paymentRequest.currencyCode = payment.currencyCode
            PaymentResponse paymentResponse = doPaymentSale(paymentRequest)

            if (paymentResponse.success) {

                if (log.traceEnabled) log.trace("PayPalPro (doSinglePaymentSale) do payment SUCCESSFUL: transactionId:$paymentResponse.transactionId")

                payment.transactionId = paymentResponse.transactionId
                payment.status = PayPalStatus.ACTIVE
                return payment.save()
            } else {

                log.warn("PayPal (doSinglePaymentSale) do payment. Enable trace log to see errors")

                if (log.traceEnabled) {
                    paymentResponse.errorMessages.each {error ->
                        log.trace "PayPal error msg: code:$error.code severityCode:$error.severityCode longMessage:$error.longMessage shortMessage:$error.shortMessage"
                    }
                }

                paymentDetails.payPalErrorMessages = paymentResponse.errorMessages
                return false
            }

        } else {
            log.warn("PayPalPro (doSinglePaymentSale) domain object validation failed. Enable trace log to see errors")

            if (log.traceEnabled) {
                paymentDetails.errors.allErrors.each {error ->
                    log.trace(error)
                }
                payment.errors.allErrors.each {error ->
                    log.trace(error)
                }

            }
            return false
        }
    }

    PaymentResponse doPaymentSale(PaymentRequest paymentRequest) {
        return doPayment(METHOD_DO_DIRECT_PAYMENT, ACTION_PAYMENT_SALE, paymentRequest)
    }

    PaymentResponse doPaymentAuthorization(PaymentRequest paymentRequest) {
        return doPayment(METHOD_DO_DIRECT_PAYMENT, ACTION_PAYMENT_AUTHORIZATION, paymentRequest)
    }

    PaymentResponse doCreateRecurringPayment(PaymentRequest paymentRequest) {
        return doPayment(METHOD_CREATE_RECURRING_PAYMENTS_PROFILE, null, paymentRequest)
    }

    PaymentResponse doCancelRecurringPayment(PaymentRequest paymentRequest) {
        return doPayment(METHOD_MANAGE_RECURRING_PAYMENTS_PROFILE, ACTION_RECURRING_CANCEL, paymentRequest)
    }

    private def calculatePaymentStartDate(PayPalBilling billing, Integer amount, Integer unit) {
        Calendar calendar = Calendar.instance
        calendar.setTime(new Date())
        calendar.add(unit, amount)
        billing.startDate = calendar.time
    }

    private PaymentResponse doPayment(String method, String paymentAction, PaymentRequest paymentRequest) {

        NVPAPICaller caller = null
        try {
            caller = getNVPAPICaller()
            //NVPEncoder object is created and all the name value pairs are loaded into it.
            String NVPString = getNVPEncodedString(method, paymentAction, paymentRequest)

            if (log.traceEnabled) log.trace("PayPalPro REQUEST STRING: $NVPString")

            //call method will send the request to the server and return the response NVPString
            String ppresponse = (String)caller.call(NVPString)

            if (log.traceEnabled) log.trace("PayPalPro RESPONSE STRING: $ppresponse")

            //NVPDecoder object is created
            NVPDecoder resultValues = new NVPDecoder()

            //decode method of NVPDecoder will parse the request and decode the name and value pair
            resultValues.decode(ppresponse)

            PaymentResponse paymentResponse = getPaymentResponse(method, resultValues)
            return paymentResponse
        } catch (Exception e) {
            throw new PaymentException(e)
        }

//        PaymentResponse paymentResponse = new PaymentResponse()
//        paymentResponse.success = true
//        paymentResponse.profileId = "1"
//        paymentResponse.profileStatus = "teststatus"
//        return paymentResponse
    }

    private String getNVPEncodedString(String method, String paymentAction, PaymentRequest paymentRequest) {
        NVPEncoder encoder = new NVPEncoder()

        addMethod(method, encoder)
        addPaymentAction(paymentAction, encoder)

        if (METHOD_MANAGE_RECURRING_PAYMENTS_PROFILE == method) {
            addManageBillingInfo(paymentRequest, encoder)
        } else {

            addIpAddress(paymentRequest, encoder)
            addCardInfo(paymentRequest, encoder)
            addBillingAddressInfo(paymentRequest, encoder)
            addCurrency(paymentRequest, encoder)

            if (METHOD_CREATE_RECURRING_PAYMENTS_PROFILE == method) {
                addRecurringBillingInfo(paymentRequest, encoder)
            }

        }

        //encode method will encode the name and value and form NVP string for the request
        String NVPString = encoder.encode()
        return NVPString
    }

    private def addPaymentAction(String paymentAction, NVPEncoder encoder) {
        encoder.add("PAYMENTACTION", paymentAction)
    }

    private def addMethod(String method, NVPEncoder encoder) {
        encoder.add("METHOD", method)
    }

    private def addIpAddress(PaymentRequest paymentRequest, NVPEncoder encoder) {
        encoder.add("IPADDRESS", paymentRequest.ipAddress)
    }

    void addManageBillingInfo(PaymentRequest paymentRequest, NVPEncoder encoder) {
        encoder.add("PROFILEID", paymentRequest.profileId)
        encoder.add("NOTE", paymentRequest.profileNote)
    }

    private void addRecurringBillingInfo(PaymentRequest paymentRequest, NVPEncoder encoder) {

        encoder.add("PROFILEREFERENCE", paymentRequest.recurringBillingReference)

        encoder.add("INITAMT", paymentRequest.recurringBillingInitialAmount.toString())
        encoder.add("FAILEDINITAMTACTION", paymentRequest.recurringBillingInitialAmountFailAction)
        encoder.add("MAXFAILEDPAYMENTS", paymentRequest.recurringBillingMaxFailedPayments.toString())

        encoder.add("PROFILESTARTDATE", paymentRequest.recurringBillingStartDate.format("yyyy-MM-dd'T'HH:mm:ss") + "Z")
        encoder.add("BILLINGPERIOD", paymentRequest.recurringBillingPeriod)
        encoder.add("BILLINGFREQUENCY", paymentRequest.recurringBillingFreqency)
        encoder.add("DESC", paymentRequest.recurringBillingDescription)
        encoder.add("NOTE", paymentRequest.profileNote)
    }

    private void addCardInfo(PaymentRequest paymentRequest, NVPEncoder encoder) {
        encoder.add("AMT", paymentRequest.amount.toString())
        encoder.add("CREDITCARDTYPE", paymentRequest.cardType)
        encoder.add("ACCT", paymentRequest.cardNumber)
        encoder.add("EXPDATE", paymentRequest.cardEndMonth + paymentRequest.cardEndYear)
        encoder.add("CVV2", paymentRequest.cardVerificationValue)
    }

    private void addCurrency(PaymentRequest paymentRequest, NVPEncoder encoder) {
        encoder.add("CURRENCYCODE", paymentRequest.currencyCode)
    }

    private void addBillingAddressInfo(PaymentRequest paymentRequest, NVPEncoder encoder) {
        encoder.add("FIRSTNAME", paymentRequest.firstName)
        encoder.add("LASTNAME", paymentRequest.lastName)
        encoder.add("EMAIL", paymentRequest.email)
        encoder.add("STREET", paymentRequest.street)
        encoder.add("CITY", paymentRequest.city)
        encoder.add("STATE", paymentRequest.state)
        encoder.add("ZIP", paymentRequest.zip)
        encoder.add("COUNTRYCODE", paymentRequest.countryCode)
    }

    private NVPAPICaller getNVPAPICaller() {
        APIProfile profile = ProfileFactory.createSignatureAPIProfile()

        def config = getPaypalConfig()

        if (config) {
            def username = config.username
            def password = config.password
            def signature = config.signature
            def environment = config.environment

            if (log.traceEnabled) log.trace("PayPalPro using paypal username:$config.username")

            if (username && password && signature) {
                profile.setAPIUsername(username)
                profile.setAPIPassword(password)
                profile.setSignature(signature)
                profile.setEnvironment(environment)
            } else {
                throw new PaymentException("You must specify paypal username,password and signature in your grails conf. See PayPalPro Docs")
            }

        } else {
            log.warn("##### WARNING NO PAYPALPRO CONFIGURATION FROUND USING DEFAULT CREDENTIALS #####")
            profile.setAPIUsername(DEFAULT_USER_NAME)
            profile.setAPIPassword(DEFAULT_PASSWORD)
            profile.setSignature(DEFAULT_SIGNATURE)
            profile.setEnvironment(testEnv)
        }

        NVPAPICaller caller = new NVPAPICaller()
        caller.setupConnection(profile)
        return caller
    }

    Object getPaypalConfig() {
        return ConfigurationHolder.config?.paypalPro
    }

    private PaymentResponse getPaymentResponse(String method, NVPDecoder nvpDecoder) {

        String strAck = nvpDecoder.get("ACK")
        if (strAck != null && !(strAck.equals("Success") || strAck.equals("SuccessWithWarning"))) {

            PaymentResponse paymentResponse = new PaymentResponse(false)
            paymentResponse.transactionId = (String)nvpDecoder.get("TRANSACTIONID")
            paymentResponse.amount = (String)nvpDecoder.get("AMT")
            paymentResponse.avsCode = (String)nvpDecoder.get("AVSCODE")
            paymentResponse.cardVerificationValueMatch = (String)nvpDecoder.get("CVV2MATCH")

            // create error collection
            Collection<PaymentError> errorMessages = new ArrayList<PaymentError>()
            int i = 0
            while (nvpDecoder.get("L_LONGMESSAGE" + i) != null && !nvpDecoder.get("L_LONGMESSAGE" + i).equals("")) {

                PaymentError paymentError = new PaymentError()
                paymentError.code = nvpDecoder.get("L_ERRORCODE" + i)
                paymentError.shortMessage = nvpDecoder.get("L_SHORTMESSAGE" + i)
                paymentError.longMessage = nvpDecoder.get("L_LONGMESSAGE" + i)
                paymentError.severityCode = nvpDecoder.get("L_SEVERITYCODE" + i)

                errorMessages << paymentError
                i++
            }
            paymentResponse.errorMessages = errorMessages
            return paymentResponse

        } else {
            // successful
            PaymentResponse paymentResponse = new PaymentResponse(true)

            if (METHOD_CREATE_RECURRING_PAYMENTS_PROFILE == method) {
                paymentResponse.profileId = (String)nvpDecoder.get("PROFILEID")
                paymentResponse.profileStatus = (String)nvpDecoder.get("PROFILESTATUS")
            } else {
                paymentResponse.transactionId = (String)nvpDecoder.get("TRANSACTIONID")
                paymentResponse.amount = (String)nvpDecoder.get("AMT")
                paymentResponse.avsCode = (String)nvpDecoder.get("AVSCODE")
                paymentResponse.cardVerificationValueMatch = (String)nvpDecoder.get("CVV2MATCH")
            }
            return paymentResponse
        }
    }

}
