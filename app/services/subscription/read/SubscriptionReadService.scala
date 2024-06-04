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

package services.subscription.read

import com.google.inject.Inject
import connectors.subscription.read.SubscriptionReadConnector
import connectors.subscription.read.SubscriptionReadConnector.{Requests => ConnectorRequest, Responses => ConnectorResponse}
import converters.subscription.read.SubscriptionReadConverter
import services.BaseService
import services.subscription.SubscriptionService.RequestOrResponse.Contact
import services.subscription.read.SubscriptionReadService.{Requests => ServiceRequest, Responses => ServiceResponse}

class SubscriptionReadService @Inject() (connector: SubscriptionReadConnector, converter: SubscriptionReadConverter)
    extends BaseService[ServiceRequest.Request, ServiceResponse.Response, ConnectorRequest.Request, ConnectorResponse.Response](
      connector,
      converter
    )

object SubscriptionReadService {

  object Requests {

    final case class Request(id: String)

  }

  object Responses {

    final case class Response(
      name: String,
      contacts: Seq[Contact]
    )
  }
}
