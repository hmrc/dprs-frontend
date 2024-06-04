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

package converters.subscription.read

import connectors.subscription.SubscriptionConnector.{RequestOrResponse => ConnectorRequests}
import connectors.subscription.read.SubscriptionReadConnector
import converters.subscription.SubscriptionConverter
import services.subscription.SubscriptionService.{RequestOrResponse => ServiceRequests}
import services.subscription.read.SubscriptionReadService

class SubscriptionReadConverter
    extends SubscriptionConverter[SubscriptionReadService.Requests.Request,
                                  SubscriptionReadConnector.Requests.Request,
                                  SubscriptionReadConnector.Responses.Response,
                                  SubscriptionReadService.Responses.Response
    ] {

  override def convertSuccessfulConnectorResponse(
    response: Option[SubscriptionReadConnector.Responses.Response]
  ): Option[SubscriptionReadService.Responses.Response] =
    response.map(response =>
      SubscriptionReadService.Responses.Response(
        response.name,
        response.contacts.map(convert)
      )
    )

  override def convertServiceRequest(request: SubscriptionReadService.Requests.Request): SubscriptionReadConnector.Requests.Request =
    SubscriptionReadConnector.Requests.Request(request.id)

  private def convert(contact: ConnectorRequests.Contact): ServiceRequests.Contact =
    contact match {
      case ConnectorRequests.Individual("I", firstName, middleName, lastName, landline, mobile, emailAddress) =>
        ServiceRequests.Individual(firstName, middleName, lastName, landline, mobile, emailAddress)
      case ConnectorRequests.Organisation("O", name, landline, mobile, emailAddress) =>
        ServiceRequests.Organisation(name, landline, mobile, emailAddress)
    }

}
