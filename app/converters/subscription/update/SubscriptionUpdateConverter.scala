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

package converters.subscription.update

import connectors.subscription.SubscriptionConnector.{Requests => ConnectorDataRequests, Responses => ConnectorResponses}
import connectors.subscription.update.SubscriptionUpdateConnector.{Requests => ConnectorUpdateRequests}
import converters.BaseConverter
import services.subscription.SubscriptionService.{Requests => ServiceDataRequests, Responses => ServiceResponses}
import services.subscription.update.SubscriptionUpdateService.{Requests => ServiceCreateRequests}

class SubscriptionUpdateConverter
    extends BaseConverter[ServiceCreateRequests.Request, ConnectorUpdateRequests.Request, ConnectorResponses.Response, ServiceResponses.Response] {

  override def convertServiceRequest(request: ServiceCreateRequests.Request): ConnectorUpdateRequests.Request =
    ConnectorUpdateRequests.Request(id = request.id, name = request.name, contacts = request.contacts.map(convert))

  override def convertSuccessfulConnectorResponse(response: ConnectorResponses.Response): ServiceResponses.Response =
    ServiceResponses.Response("OK")

  private def convert(contact: ServiceDataRequests.Contact): ConnectorDataRequests.Contact =
    contact match {
      case ServiceDataRequests.Individual(firstName, middleName, lastName, landline, mobile, emailAddress) =>
        ConnectorDataRequests.Individual("I", firstName, middleName, lastName, landline, mobile, emailAddress)
      case ServiceDataRequests.Organisation(name, landline, mobile, emailAddress) =>
        ConnectorDataRequests.Organisation("O", name, landline, mobile, emailAddress)
    }

}
