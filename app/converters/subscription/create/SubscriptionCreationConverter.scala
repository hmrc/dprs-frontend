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

import connectors.subscription.SubscriptionConnector.{Responses => ConnectorResponses}
import connectors.subscription.create.SubscriptionCreationConnector.{Requests => ConnectorCreateRequests}
import converters.subscription.SubscriptionConverter
import services.subscription.SubscriptionService.{Responses => ServiceResponses}
import services.subscription.create.SubscriptionCreationService.{Requests => ServiceCreateRequests}

class SubscriptionCreationConverter extends SubscriptionConverter[ServiceCreateRequests.Request, ConnectorCreateRequests.Request] {

  override def convertServiceRequest(request: ServiceCreateRequests.Request): ConnectorCreateRequests.Request =
    ConnectorCreateRequests.Request(id = convert(request.id), name = request.name, contacts = request.contacts.map(convert))

  override def convertSuccessfulConnectorResponse(response: ConnectorResponses.Response): ServiceResponses.Response =
    ServiceResponses.Response(response.id)

  private def convert(id: ServiceCreateRequests.Id): ConnectorCreateRequests.Id =
    ConnectorCreateRequests.Id(convert(id.idType), id.value)

  private def convert(idType: ServiceCreateRequests.IdType): String = idType match {
    case ServiceCreateRequests.IdType.NINO => "NINO"
    case ServiceCreateRequests.IdType.SAFE => "SAFE"
    case ServiceCreateRequests.IdType.UTR  => "UTR"
  }
}
