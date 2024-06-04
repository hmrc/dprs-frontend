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

import connectors.BaseConnector
import connectors.subscription.update.SubscriptionUpdateConnector.{Requests => ConnectorRequests}
import converters.subscription.SubscriptionConverter
import services.BaseService
import services.subscription.update.SubscriptionUpdateService.{Requests => ServiceRequests}

class SubscriptionUpdateConverter
    extends SubscriptionConverter[ServiceRequests.Request,
                                  ConnectorRequests.Request,
                                  BaseConnector.Responses.EmptyResponse,
                                  BaseService.Responses.EmptyResponse
    ] {

  override def convertSuccessfulConnectorResponse(response: Option[BaseConnector.Responses.EmptyResponse]): Option[BaseService.Responses.EmptyResponse] = None

  override def convertServiceRequest(request: ServiceRequests.Request): ConnectorRequests.Request =
    ConnectorRequests.Request(id = request.id, name = request.name, contacts = request.contacts.map(convert))

}
