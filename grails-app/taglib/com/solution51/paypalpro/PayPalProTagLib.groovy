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

class PayPalProTagLib {

    static namespace = 'paypalpro'

    def hasPaymentErrors = {attrs, body ->
        def bean = attrs.bean
        if (bean?.payPalErrorMessages) {
            out << body()
        }
    }

    def renderPaymentErrors = {attrs, body ->

        out << "<ul>"

        def bean = attrs.bean

        if (bean?.payPalErrorMessages) {
            bean.payPalErrorMessages.each {error ->
                out << "<li>${message(code: "paypalpro.$error.code", default: error.longMessage, encodeAs: "HTML")}</li>"
            }
        }

        out << "</ul>"

    }

    def generateAutoHideJavascript = {attrs,body ->

        def debitClass = attrs.debitClass?:'debit'
        def cardType = attrs.debitClass?:'cardType'
        def countryCode = attrs.debitClass?:'countryCode'
        def useEmbeddedJquery = attrs.useEmbeddedJquery?Boolean.parseBoolean(attrs.useEmbeddedJquery).booleanValue():true

        if (useEmbeddedJquery) {
            def jsUtilSrc = createPluginResourceSrc("js/jquery/jquery-1.3.1.min.js")
            out << "<script type=\"text/javascript\" src=\"$jsUtilSrc\"></script>\n"
        }

        out << '<script type="text/javascript">\n'
        out << '//<![CDATA[\n'
        out << '$(document).ready(function() {\n'
        out << '$(' + "'.$debitClass').hide()\n"
        out << '$(' + "'#$cardType').change(function(){\n"
        out << '    var cardType = $(' + "'#$cardType').val()\n"
        out << '    if(cardType == "Maestro" || cardType == "Solo"){\n'
        out << '        $(' + "'.$debitClass').show()\n"
        out << '        $(' + "'.$countryCode').show()\n"

        out << '    }else{\n'
        out << '        $(' + "'.$debitClass').hide()\n"
        out << '    }\n'
        out << '})\n'
        out << '})\n'
        out << '//]]>\n'
        out << '</script>\n'
    }

    def createPluginResourceSrc(fileName) {
        return g.createLinkTo(dir: pluginContextPath, file: fileName)
    }

    def cardTypeSelect = {attrs, body ->

        def cardTypesList = attrs["cardTypes"]

        def cardTypes = []
        if(cardTypesList){
            cardTypes = cardTypesList.collect {
                CardType cardType = CardType.parsePayPalCode(it)
                if(cardType)return cardType
            }
        }else{
            cardTypes = CardType.values()
        }

        attrs.from = cardTypes
        attrs.optionValue = 'displayName'
        attrs.optionKey = 'paypalCode'
        out << g.select(attrs)
    }

    def monthSelect = {attrs, body ->
        def months = []
        for (m in 1..9) {
            months << "0$m"
        }
        months << '10'
        months << '11'
        months << '12'

        attrs.from = months
        out << g.select(attrs)
    }

    def startYearSelect = {attrs, body ->

        Calendar today = Calendar.instance
        def year = today.get(Calendar.YEAR)

        def years = []

        for (y in (year - 5)..year) {
            years << y
        }

        attrs.from = years
        out << g.select(attrs)
    }

    def expireYearSelect = {attrs, body ->

        Calendar today = Calendar.instance
        def year = today.get(Calendar.YEAR)

        def years = []

        for (y in year..year + 5) {
            years << y
        }

        attrs.from = years
        out << g.select(attrs)
    }

    def countrySelect = {attrs ->
        if (!attrs['from']) {
            attrs['from'] = COUNTRY_CODES_BY_NAME_ORDER
        }
        def valuePrefix = attrs.remove('valueMessagePrefix')
        attrs['optionValue'] = { valuePrefix ? "${valuePrefix}.${it}" : COUNTRY_CODES[it] }
        if (!attrs['value']) {
            attrs['value'] = attrs.remove('default')
        }
        out << select(attrs)
    }

    static final COUNTRY_CODES = [
            "AF": "AFGHANISTAN",
            "AX": "ALAND ISLANDS",
            "AL": "ALBANIA",
            "DZ": "ALGERIA",
            "AS": "AMERICAN SAMOA",
            "AD": "ANDORRA",
            "AO": "ANGOLA",
            "AI": "ANGUILLA",
            "AQ": "ANTARCTICA",
            "AG": "ANTIGUA AND BARBUDA",
            "AR": "ARGENTINA",
            "AM": "ARMENIA",
            "AW": "ARUBA",
            "AU": "AUSTRALIA",
            "AT": "AUSTRIA",
            "AZ": "AZERBAIJAN",
            "BS": "BAHAMAS",
            "BH": "BAHRAIN",
            "BD": "BANGLADESH",
            "BB": "BARBADOS",
            "BY": "BELARUS",
            "BE": "BELGIUM",
            "BZ": "BELIZE",
            "BJ": "BENIN",
            "BM": "BERMUDA",
            "BT": "BHUTAN",
            "BO": "BOLIVIA",
            "BA": "BOSNIA AND HERZEGOVINA",
            "BW": "BOTSWANA",
            "BV": "BOUVET ISLAND",
            "BR": "BRAZIL",
            "IO": "BRITISH INDIAN OCEAN TERRITORY",
            "BN": "BRUNEI DARUSSALAM",
            "BG": "BULGARIA",
            "BF": "BURKINA FASO",
            "BI": "BURUNDI",
            "KH": "CAMBODIA",
            "CM": "CAMEROON",
            "CA": "CANADA",
            "CV": "CAPE VERDE",
            "KY": "CAYMAN ISLANDS",
            "CF": "CENTRAL AFRICAN REPUBLIC",
            "TD": "CHAD",
            "CL": "CHILE",
            "CN": "CHINA",
            "CX": "CHRISTMAS ISLAND",
            "CC": "COCOS (KEELING) ISLANDS",
            "CO": "COLOMBIA",
            "KM": "COMOROS",
            "CG": "CONGO",
            "CD": "CONGO, THE DEMOCRATIC REPUBLIC OF THE",
            "CK": "COOK ISLANDS",
            "CR": "COSTA RICA",
            "CI": "COTE D'IVOIRE",
            "HR": "CROATIA",
            "CU": "CUBA",
            "CY": "CYPRUS",
            "CZ": "CZECH REPUBLIC",
            "DK": "DENMARK",
            "DJ": "DJIBOUTI",
            "DM": "DOMINICA",
            "DO": "DOMINICAN REPUBLIC",
            "EC": "ECUADOR",
            "EG": "EGYPT",
            "SV": "EL SALVADOR",
            "GQ": "EQUATORIAL GUINEA",
            "ER": "ERITREA",
            "EE": "ESTONIA",
            "ET": "ETHIOPIA",
            "FK": "FALKLAND ISLANDS (MALVINAS)",
            "FO": "FAROE ISLANDS",
            "FJ": "FIJI",
            "FI": "FINLAND",
            "FR": "FRANCE",
            "GF": "FRENCH GUIANA",
            "PF": "FRENCH POLYNESIA",
            "TF": "FRENCH SOUTHERN TERRITORIES",
            "GA": "GABON",
            "GM": "GAMBIA",
            "GE": "GEORGIA",
            "DE": "GERMANY",
            "GH": "GHANA",
            "GI": "GIBRALTAR",
            "GR": "GREECE",
            "GL": "GREENLAND",
            "GD": "GRENADA",
            "GP": "GUADELOUPE",
            "GU": "GUAM",
            "GT": "GUATEMALA",
            "GG": "GUERNSEY",
            "GN": "GUINEA",
            "GW": "GUINEA-BISSAU",
            "GY": "GUYANA",
            "HT": "HAITI",
            "HM": "HEARD ISLAND AND MCDONALD ISLANDS",
            "VA": "HOLY SEE (VATICAN CITY STATE)",
            "HN": "HONDURAS",
            "HK": "HONG KONG",
            "HU": "HUNGARY",
            "IS": "ICELAND",
            "IN": "INDIA",
            "ID": "INDONESIA",
            "IR": "IRAN, ISLAMIC REPUBLIC OF",
            "IQ": "IRAQ",
            "IE": "IRELAND",
            "IM": "ISLE OF MAN",
            "IL": "ISRAEL",
            "IT": "ITALY",
            "JM": "JAMAICA",
            "JP": "JAPAN",
            "JE": "JERSEY",
            "JO": "JORDAN",
            "KZ": "KAZAKHSTAN",
            "KE": "KENYA",
            "KI": "KIRIBATI",
            "KP": "KOREA, DEMOCRATIC PEOPLE'S REPUBLIC OF",
            "KR": "KOREA, REPUBLIC OF",
            "KW": "KUWAIT",
            "KG": "KYRGYZSTAN",
            "LA": "LAO PEOPLE'S DEMOCRATIC REPUBLIC",
            "LV": "LATVIA",
            "LB": "LEBANON",
            "LS": "LESOTHO",
            "LR": "LIBERIA",
            "LY": "LIBYAN ARAB JAMAHIRIYA",
            "LI": "LIECHTENSTEIN",
            "LT": "LITHUANIA",
            "LU": "LUXEMBOURG",
            "MO": "MACAO",
            "MK": "MACEDONIA, THE FORMER YUGOSLAV REPUBLIC OF",
            "MG": "MADAGASCAR",
            "MW": "MALAWI",
            "MY": "MALAYSIA",
            "MV": "MALDIVES",
            "ML": "MALI",
            "MT": "MALTA",
            "MH": "MARSHALL ISLANDS",
            "MQ": "MARTINIQUE",
            "MR": "MAURITANIA",
            "MU": "MAURITIUS",
            "YT": "MAYOTTE",
            "MX": "MEXICO",
            "FM": "MICRONESIA, FEDERATED STATES OF",
            "MD": "MOLDOVA, REPUBLIC OF",
            "MC": "MONACO",
            "MN": "MONGOLIA",
            "MS": "MONTSERRAT",
            "MA": "MOROCCO",
            "MZ": "MOZAMBIQUE",
            "MM": "MYANMAR",
            "NA": "NAMIBIA",
            "NR": "NAURU",
            "NP": "NEPAL",
            "NL": "NETHERLANDS",
            "AN": "NETHERLANDS ANTILLES",
            "NC": "NEW CALEDONIA",
            "NZ": "NEW ZEALAND",
            "NI": "NICARAGUA",
            "NE": "NIGER",
            "NG": "NIGERIA",
            "NU": "NIUE",
            "NF": "NORFOLK ISLAND",
            "MP": "NORTHERN MARIANA ISLANDS",
            "NO": "NORWAY",
            "OM": "OMAN",
            "PK": "PAKISTAN",
            "PW": "PALAU",
            "PS": "PALESTINIAN TERRITORY, OCCUPIED",
            "PA": "PANAMA",
            "PG": "PAPUA NEW GUINEA",
            "PY": "PARAGUAY",
            "PE": "PERU",
            "PH": "PHILIPPINES",
            "PN": "PITCAIRN",
            "PL": "POLAND",
            "PT": "PORTUGAL",
            "PR": "PUERTO RICO",
            "QA": "QATAR",
            "RE": "REUNION",
            "RO": "ROMANIA",
            "RU": "RUSSIAN FEDERATION",
            "RW": "RWANDA",
            "SH": "SAINT HELENA",
            "KN": "SAINT KITTS AND NEVIS",
            "LC": "SAINT LUCIA",
            "PM": "SAINT PIERRE AND MIQUELON",
            "VC": "SAINT VINCENT AND THE GRENADINES",
            "WS": "SAMOA",
            "SM": "SAN MARINO",
            "ST": "SAO TOME AND PRINCIPE",
            "SA": "SAUDI ARABIA",
            "SN": "SENEGAL",
            "CS": "SERBIA AND MONTENEGRO",
            "SC": "SEYCHELLES",
            "SL": "SIERRA LEONE",
            "SG": "SINGAPORE",
            "SK": "SLOVAKIA",
            "SI": "SLOVENIA",
            "SB": "SOLOMON ISLANDS",
            "SO": "SOMALIA",
            "ZA": "SOUTH AFRICA",
            "GS": "SOUTH GEORGIA AND THE SOUTH SANDWICH ISLANDS",
            "ES": "SPAIN",
            "LK": "SRI LANKA",
            "SD": "SUDAN",
            "SR": "SURINAME",
            "SJ": "SVALBARD AND JAN MAYEN",
            "SZ": "SWAZILAND",
            "SE": "SWEDEN",
            "CH": "SWITZERLAND",
            "SY": "SYRIAN ARAB REPUBLIC",
            "TW": "TAIWAN, PROVINCE OF CHINA",
            "TJ": "TAJIKISTAN",
            "TZ": "TANZANIA, UNITED REPUBLIC OF",
            "TH": "THAILAND",
            "TL": "TIMOR-LESTE",
            "TG": "TOGO",
            "TK": "TOKELAU",
            "TO": "TONGA",
            "TT": "TRINIDAD AND TOBAGO",
            "TN": "TUNISIA",
            "TR": "TURKEY",
            "TM": "TURKMENISTAN",
            "TC": "TURKS AND CAICOS ISLANDS",
            "TV": "TUVALU",
            "UG": "UGANDA",
            "UA": "UKRAINE",
            "AE": "UNITED ARAB EMIRATES",
            "GB": "UNITED KINGDOM",
            "US": "UNITED STATES",
            "UM": "UNITED STATES MINOR OUTLYING ISLANDS",
            "UY": "URUGUAY",
            "UZ": "UZBEKISTAN",
            "VU": "VANUATU",
            "VE": "VENEZUELA",
            "VN": "VIET NAM",
            "VG": "VIRGIN ISLANDS, BRITISH",
            "VI": "VIRGIN ISLANDS, U.S.",
            "WF": "WALLIS AND FUTUNA",
            "EH": "WESTERN SAHARA",
            "YE": "YEMEN",
            "ZM": "ZAMBIA",
            "ZW": "ZIMBABWE"
    ]

    static final COUNTRY_CODES_BY_NAME_ORDER =
    COUNTRY_CODES.entrySet().sort({a, b -> a.value.compareTo(b.value) }).collect() { it.key }

}
