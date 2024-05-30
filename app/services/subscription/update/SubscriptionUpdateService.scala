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

package services.subscription.update

import com.google.inject.{Inject, Singleton}
import connectors.BaseConnector
import connectors.subscription.update.SubscriptionUpdateConnector
import connectors.subscription.update.SubscriptionUpdateConnector.{Requests => ConnectorRequests}
import converters.subscription.update.SubscriptionUpdateConverter
import services.BaseService
import services.subscription.SubscriptionService.Requests.Contact
import services.subscription.update.SubscriptionUpdateService.{Requests => ServiceRequests}

@Singleton
class SubscriptionUpdateService @Inject() (connector: SubscriptionUpdateConnector, converter: SubscriptionUpdateConverter)
    extends BaseService[ServiceRequests.Request, BaseService.Responses.EmptyResponse, ConnectorRequests.Request, BaseConnector.Responses.EmptyResponse](
      connector,
      converter
    )

object SubscriptionUpdateService {

  object Requests {
    final case class Request(id: String, name: Option[String], contacts: Seq[Contact])
  }
}
