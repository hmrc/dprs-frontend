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

package converters.subscription

import connectors.subscription.SubscriptionConnector.{RequestOrResponse => ConnectorRequests}
import converters.BaseConverter
import services.subscription.SubscriptionService.{RequestOrResponse => ServiceRequests}

abstract class SubscriptionConverter[SERVICE_REQUEST, CONNECTOR_REQUEST, CONNECTOR_RESPONSE, SERVICE_RESPONSE]
    extends BaseConverter[SERVICE_REQUEST, CONNECTOR_REQUEST, CONNECTOR_RESPONSE, SERVICE_RESPONSE] {

  protected def convert(contact: ServiceRequests.Contact): ConnectorRequests.Contact =
    contact match {
      case ServiceRequests.Individual(firstName, middleName, lastName, landline, mobile, emailAddress) =>
        ConnectorRequests.Individual("I", firstName, middleName, lastName, landline, mobile, emailAddress)
      case ServiceRequests.Organisation(name, landline, mobile, emailAddress) =>
        ConnectorRequests.Organisation("O", name, landline, mobile, emailAddress)
    }

}
