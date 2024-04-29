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

package converters.registration.withId

import connectors.registration.withId.RegistrationWithIdConnector
import converters.registration.BaseRegistrationConverter
import services.registration.withId.BaseRegistrationWithIdService

abstract class BaseRegistrationWithIdConverter[SERVICE_REQUEST, CONNECTOR_REQUEST, CONNECTOR_RESPONSE, SERVICE_RESPONSE]
    extends BaseRegistrationConverter[SERVICE_REQUEST, CONNECTOR_REQUEST, CONNECTOR_RESPONSE, SERVICE_RESPONSE] {

  def convert(address: RegistrationWithIdConnector.Responses.Address): BaseRegistrationWithIdService.Responses.Address =
    BaseRegistrationWithIdService.Responses.Address(
      lineOne = address.lineOne,
      lineTwo = address.lineTwo,
      lineThree = address.lineThree,
      lineFour = address.lineFour,
      postalCode = address.postalCode,
      countryCode = address.countryCode
    )

  def convert(
    contactDetails: RegistrationWithIdConnector.Responses.ContactDetails
  ): BaseRegistrationWithIdService.Responses.ContactDetails = BaseRegistrationWithIdService.Responses.ContactDetails(
    landline = contactDetails.landline,
    mobile = contactDetails.mobile,
    fax = contactDetails.fax,
    emailAddress = contactDetails.emailAddress
  )
}
