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

package converters.subscription.create

import connectors.subscription.create.SubscriptionCreationConnector.Responses.Response
import connectors.subscription.create.SubscriptionCreationConnector.{Requests => ConnectorCreateRequests, Responses => ConnectorResponses}
import converters.subscription.SubscriptionConverter
import services.subscription.create.SubscriptionCreationService.{Requests => ServiceRequests, Responses => ServiceResponses}

class SubscriptionCreationConverter
    extends SubscriptionConverter[ServiceRequests.Request, ConnectorCreateRequests.Request, ConnectorResponses.Response, ServiceResponses.Response] {

  override def convertServiceRequest(request: ServiceRequests.Request): ConnectorCreateRequests.Request =
    ConnectorCreateRequests.Request(id = convert(request.id), name = request.name, contacts = request.contacts.map(convert))

  override def convertSuccessfulConnectorResponse(responseOpt: Option[Response]): Option[ServiceResponses.Response] =
    responseOpt.map(response => ServiceResponses.Response(response.id))

  private def convert(id: ServiceRequests.Id): ConnectorCreateRequests.Id =
    ConnectorCreateRequests.Id(convert(id.idType), id.value)

  private def convert(idType: ServiceRequests.IdType): String = idType match {
    case ServiceRequests.IdType.NINO => "NINO"
    case ServiceRequests.IdType.SAFE => "SAFE"
    case ServiceRequests.IdType.UTR  => "UTR"
  }
}
