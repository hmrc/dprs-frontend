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
import connectors.BaseConnector
import connectors.subscription.SubscriptionConnector
import connectors.subscription.SubscriptionConnector.RequestOrResponse.Contact
import connectors.subscription.create.SubscriptionCreationConnector.Requests.Request
import connectors.subscription.create.SubscriptionCreationConnector.Responses.Response
import play.api.libs.functional.syntax.{toFunctionalBuilderOps, unlift}
import play.api.libs.json.{JsPath, OWrites, Reads}
import play.api.libs.ws.WSClient

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SubscriptionCreationConnector @Inject() (frontendAppConfig: FrontendAppConfig, wsClient: WSClient)
    extends SubscriptionConnector[Request, Response](frontendAppConfig, wsClient) {

  override def call(request: Request)(implicit
    executionContext: ExecutionContext
  ): Future[Either[BaseConnector.Responses.Errors, Option[Response]]] =
    post(request)

}

object SubscriptionCreationConnector {

  object Requests {

    final case class Request(id: Id, name: Option[String], contacts: Seq[Contact])

    object Request {
      implicit lazy val writes: OWrites[Request] =
        ((JsPath \ "id").write[Id] and
          (JsPath \ "name").writeNullable[String] and
          (JsPath \ "contacts").write[Seq[Contact]])(unlift(Request.unapply))
    }

    final case class Id(idType: String, value: String)

    object Id {
      implicit lazy val writes: OWrites[Id] =
        ((JsPath \ "type").write[String] and
          (JsPath \ "value").write[String])(unlift(Id.unapply))
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
