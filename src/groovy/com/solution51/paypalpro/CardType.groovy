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

public enum CardType {

    VISA("Visa", "Visa"),
    MASTERCARD("MasterCard", "MasterCard"),
    DISCOVER("Discover", "Discover"),
    AMERICAN_EXPRESS("American Express", "Amex"),
    MAESTRO("Maestro", "Maestro"),
    SOLO("Solo", "Solo")

    private String displayName
    private String paypalCode

    CardType(String displayName, String paypalCode) {
        this.displayName = displayName
        this.paypalCode = paypalCode
    }

    String getDisplayName() {
        return displayName
    }

    String getPaypalCode() {
        return paypalCode
    }

    static CardType parsePayPalCode(String name) {
        if (name == VISA.paypalCode) {
            return CardType.VISA
        } else if (name == MASTERCARD.paypalCode) {
            return CardType.MASTERCARD
        } else if (name == DISCOVER.paypalCode) {
            return CardType.DISCOVER
        } else if (name == AMERICAN_EXPRESS.paypalCode) {
            return CardType.AMERICAN_EXPRESS
        } else if (name == MAESTRO.paypalCode) {
            return CardType.MAESTRO
        } else if (name == SOLO.paypalCode) {
            return CardType.SOLO
        }
        return null
    }

}
