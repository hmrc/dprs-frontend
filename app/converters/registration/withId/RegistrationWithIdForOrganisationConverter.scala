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
import connectors.registration.withId.RegistrationWithIdForOrganisationConnector.{Requests => ConnectorRequests, Responses => ConnectorResponses}
import services.registration.withId.RegistrationWithIdForOrganisationService.{Requests => ServiceRequests, Responses => ServiceResponses}

class RegistrationWithIdForOrganisationConverter
    extends BaseRegistrationWithIdConverter[ServiceRequests.Request, ConnectorRequests.Request, ConnectorResponses.Response, ServiceResponses.Response] {

  private val responseTypes = Map(
    "NotSpecified"                -> ServiceResponses.Type.NotSpecified,
    "Partnership"                 -> ServiceResponses.Type.Partnership,
    "LimitedLiabilityPartnership" -> ServiceResponses.Type.LimitedLiabilityPartnership,
    "CorporateBody"               -> ServiceResponses.Type.CorporateBody,
    "UnincorporatedBody"          -> ServiceResponses.Type.UnincorporatedBody
  )

  override def convertServiceRequest(request: ServiceRequests.Request): ConnectorRequests.Request =
    ConnectorRequests.Request(id = convert(request.id), name = request.name, _type = convert(request._type))

  override def convertSuccessfulConnectorResponse(responseOpt: Option[ConnectorResponses.Response]): Option[ServiceResponses.Response] =
    responseOpt.map(response =>
      ServiceResponses.Response(
        ids = response.ids.map(convert),
        name = response.name,
        _type = convertResponseType(response._type),
        address = convert(response.address),
        contactDetails = convert(response.contactDetails)
      )
    )

  private def convert(id: ServiceRequests.Id): RegistrationWithIdConnector.Requests.Id =
    RegistrationWithIdConnector.Requests.Id(idType = convert(id.idType), value = id.value)

  private def convert(idType: ServiceRequests.IdType): String =
    idType match {
      case ServiceRequests.IdType.EORI => "EORI"
      case ServiceRequests.IdType.UTR  => "UTR"
    }

  private def convert(_type: ServiceRequests.Type): String =
    _type match {
      case ServiceRequests.Type.NotSpecified                => "NotSpecified"
      case ServiceRequests.Type.Partnership                 => "Partnership"
      case ServiceRequests.Type.LimitedLiabilityPartnership => "LimitedLiabilityPartnership"
      case ServiceRequests.Type.CorporateBody               => "CorporateBody"
      case ServiceRequests.Type.UnincorporatedBody          => "UnincorporatedBody"
    }

  private def convertResponseType(_type: String): ServiceResponses.Type =
    responseTypes.getOrElse(_type, ServiceResponses.Type.Unknown)

}
