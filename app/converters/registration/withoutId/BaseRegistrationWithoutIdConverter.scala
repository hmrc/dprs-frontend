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

package converters.registration.withoutId

import connectors.registration.RegistrationConnector.{Requests => ConnectorRequests, Responses => ConnectorResponses}
import services.registration.BaseRegistrationService.{Responses => ServiceResponses}
import services.registration.withoutId.BaseRegistrationWithoutIdService.{Requests => ServiceRequests}

abstract class BaseRegistrationWithoutIdConverter[SERVICE_REQUEST, CONNECTOR_REQUEST]
    extends converters.registration.BaseRegistrationConverter[SERVICE_REQUEST, CONNECTOR_REQUEST, ConnectorResponses.Response, ServiceResponses.Response] {

  override def convertSuccessfulConnectorResponse(response: ConnectorResponses.Response): ServiceResponses.Response =
    ServiceResponses.Response(ids = response.ids.map(convert))

  protected def convert(address: ServiceRequests.Address): ConnectorRequests.Address =
    ConnectorRequests.Address(
      lineOne = address.lineOne,
      lineTwo = address.lineTwo,
      lineThree = address.lineThree,
      lineFour = address.lineFour,
      postalCode = address.postalCode,
      countryCode = address.countryCode
    )

  protected def convert(
    contactDetails: ServiceRequests.ContactDetails
  ): ConnectorRequests.ContactDetails =
    ConnectorRequests.ContactDetails(landline = contactDetails.landline,
                                     mobile = contactDetails.mobile,
                                     fax = contactDetails.fax,
                                     emailAddress = contactDetails.emailAddress
    )

}
