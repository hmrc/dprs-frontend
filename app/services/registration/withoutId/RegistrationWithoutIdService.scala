/*
 * Copyright 2024 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package services.registration.withoutId

import connectors.registration.RegistrationConnector
import services.registration.RegistrationService

object RegistrationWithoutIdService {

  object Requests {

    final case class Address(lineOne: String,
                             lineTwo: Option[String],
                             lineThree: Option[String],
                             lineFour: Option[String],
                             postalCode: String,
                             countryCode: String
    )

    final case class ContactDetails(landline: Option[String], mobile: Option[String], fax: Option[String], emailAddress: Option[String])

  }

  abstract class Converter extends RegistrationService.Converter {

    def convert(response: RegistrationConnector.Responses.Response): RegistrationService.Responses.Response =
      RegistrationService.Responses.Response(ids = response.ids.map(convert))

    protected def convert(address: RegistrationWithoutIdService.Requests.Address): RegistrationConnector.Requests.Address =
      RegistrationConnector.Requests.Address(
        lineOne = address.lineOne,
        lineTwo = address.lineTwo,
        lineThree = address.lineThree,
        lineFour = address.lineFour,
        postalCode = address.postalCode,
        countryCode = address.countryCode
      )

    protected def convert(contactDetails: RegistrationWithoutIdService.Requests.ContactDetails): RegistrationConnector.Requests.ContactDetails =
      RegistrationConnector.Requests.ContactDetails(landline = contactDetails.landline,
                                                    mobile = contactDetails.mobile,
                                                    fax = contactDetails.fax,
                                                    emailAddress = contactDetails.emailAddress
      )

    protected def convert(contactDetails: RegistrationConnector.Requests.ContactDetails): RegistrationConnector.Requests.ContactDetails =
      RegistrationConnector.Requests.ContactDetails(landline = contactDetails.landline,
                                                    mobile = contactDetails.mobile,
                                                    fax = contactDetails.fax,
                                                    emailAddress = contactDetails.emailAddress
      )

  }

}
