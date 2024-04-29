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

package services.subscription.create

import com.google.inject.{Inject, Singleton}
import connectors.subscription.create.SubscriptionCreationConnector
import connectors.subscription.create.SubscriptionCreationConnector.{Requests => ConnectorRequests, Responses => ConnectorResponses}
import converters.subscription.create.SubscriptionCreationConverter
import services.BaseService
import services.subscription.create.SubscriptionCreationService.{Requests => ServiceRequests, Responses => ServiceResponses}

@Singleton
class SubscriptionCreationService @Inject() (connector: SubscriptionCreationConnector, converter: SubscriptionCreationConverter)
    extends BaseService[ServiceRequests.Request, ServiceResponses.Response, ConnectorRequests.Request, ConnectorResponses.Response](
      connector,
      converter
    )

object SubscriptionCreationService {

  object Requests {

    final case class Request(id: Id, name: Option[String], contacts: Seq[Contact])

    final case class Id(idType: IdType, value: String)

    sealed trait IdType

    object IdType {

      val all: Set[IdType] = Set(NINO, SAFE, UTR)

      case object NINO extends IdType

      case object UTR extends IdType

      case object SAFE extends IdType

    }

    sealed trait Contact {
      def landline: Option[String]

      def mobile: Option[String]

      def emailAddress: String
    }

    final case class Individual(firstName: String,
                                middleName: Option[String],
                                lastName: String,
                                landline: Option[String],
                                mobile: Option[String],
                                emailAddress: String
    ) extends Contact

    final case class Organisation(name: String, landline: Option[String], mobile: Option[String], emailAddress: String) extends Contact

  }

  object Responses {

    final case class Response(id: String)

  }

}
