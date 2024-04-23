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
import connectors.registration.withId.RegistrationWithIdForIndividualConnector.{Requests => ConnectorRequests, Responses => ConnectorResponses}
import services.registration.withId.RegistrationWithIdForIndividualService.{Requests => ServiceRequests, Responses => ServiceResponses}

class RegistrationWithIdForIndividualConverter
    extends BaseRegistrationWithIdConverter[ServiceRequests.Request, ConnectorRequests.Request, ConnectorResponses.Response, ServiceResponses.Response] {

  override def convertServiceRequest(request: ServiceRequests.Request): ConnectorRequests.Request =
    ConnectorRequests.Request(id = convert(request.id),
                              firstName = request.firstName,
                              middleName = request.middleName,
                              lastName = request.lastName,
                              dateOfBirth = request.dateOfBirth
    )

  override def convertSuccessfulConnectorResponse(response: ConnectorResponses.Response): ServiceResponses.Response =
    ServiceResponses.Response(
      ids = response.ids.map(convert),
      firstName = response.firstName,
      middleName = response.middleName,
      lastName = response.lastName,
      dateOfBirth = response.dateOfBirth,
      address = convert(response.address),
      contactDetails = convert(response.contactDetails)
    )

  private def convert(id: ServiceRequests.Id): RegistrationWithIdConnector.Requests.Id =
    RegistrationWithIdConnector.Requests.Id(idType = convert(id.idType), value = id.value)

  private def convert(idType: ServiceRequests.IdType): String = idType match {
    case ServiceRequests.IdType.EORI => "EORI"
    case ServiceRequests.IdType.NINO => "NINO"
    case ServiceRequests.IdType.UTR  => "UTR"
  }
}
