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

package connectors.subscription.create

import com.google.inject.{Inject, Singleton}
import config.FrontendAppConfig
import connectors.subscription.create.SubscriptionCreationConnector.Requests.Request
import connectors.subscription.create.SubscriptionCreationConnector.Responses.Response
import connectors.{BaseBackendConnector, BaseConnector}
import play.api.libs.functional.syntax.{toFunctionalBuilderOps, unlift}
import play.api.libs.json.{JsPath, Json, OWrites, Reads}
import play.api.libs.ws.WSClient

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SubscriptionCreationConnector @Inject() (frontendAppConfig: FrontendAppConfig, wsClient: WSClient)
    extends BaseBackendConnector[Request, Response](frontendAppConfig, wsClient) {

  override def connectorPath: String = SubscriptionCreationConnector.connectorPath

  override def call(request: Request)(implicit
    executionContext: ExecutionContext
  ): Future[Either[BaseConnector.Responses.Errors, Response]] =
    post(request)

}

object SubscriptionCreationConnector {

  val connectorPath: String = "/subscriptions"

  object Requests {

    final case class Request(id: Id, name: Option[String], contacts: Seq[Contact])
    /*
     * Why is 'name' an Option[String]?
     * Are we expecting one of two contacts?
     * Can we throw an exception if the Seq is empty or has more than 2 items?
     */

    object Request {
      implicit lazy val writes: OWrites[Request] =
        ((JsPath \ "id").write[Id] and
          (JsPath \ "name").writeNullable[String] and
          (JsPath \ "contacts").write[Seq[Contact]])(unlift(Request.unapply))
    }

    final case class Id(idType: String, value: String)
    /*
     * This is used in other requests.
     * Can you make it part of a base class?
     */

    object Id {
      implicit lazy val writes: OWrites[Id] =
        ((JsPath \ "type").write[String] and
          (JsPath \ "value").write[String])(unlift(Id.unapply))
    }

    sealed trait Contact {

      def typeCode: String

      def landline: Option[String]

      def mobile: Option[String]

      def emailAddress: String
    }

    object Contact {
      implicit lazy val writes: OWrites[Contact] = Json.writes[Contact].transform(jsObject => jsObject - "_type")
    }

    final case class Individual(typeCode: String,
      /*
       * This is in Organisation too.
       * Can't you make it part of Contact?
       */
                                firstName: String,
                                middleName: Option[String],
                                lastName: String,
                                landline: Option[String],
                                mobile: Option[String],
                                emailAddress: String
    ) extends Contact

    object Individual {
      implicit lazy val writes: OWrites[Individual] =
        ((JsPath \ "type").write[String] and
          (JsPath \ "firstName").write[String] and
          (JsPath \ "middleName").writeNullable[String] and
          (JsPath \ "lastName").write[String] and
          (JsPath \ "landline").writeNullable[String] and
          (JsPath \ "mobile").writeNullable[String] and
          (JsPath \ "emailAddress").write[String])(unlift(Individual.unapply))
    }

    final case class Organisation(typeCode: String, name: String, landline: Option[String], mobile: Option[String], emailAddress: String) extends Contact

    object Organisation {
      implicit lazy val writes: OWrites[Organisation] =
        ((JsPath \ "type").write[String] and
          (JsPath \ "name").write[String] and
          (JsPath \ "landline").writeNullable[String] and
          (JsPath \ "mobile").writeNullable[String] and
          (JsPath \ "emailAddress").write[String])(unlift(Organisation.unapply))
    }

  }

  object Responses {

    final case class Response(id: String)

    object Response {
      implicit lazy val reads: Reads[Response] =
        (JsPath \ "id").read[String].map(Response(_))

    }
  }

}
