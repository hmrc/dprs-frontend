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

package connectors.subscription.read

import com.google.inject.Inject
import config.FrontendAppConfig
import connectors.BaseConnector
import connectors.subscription.SubscriptionConnector
import connectors.subscription.SubscriptionConnector.RequestOrResponse.Contact
import connectors.subscription.read.SubscriptionReadConnector.Requests.Request
import connectors.subscription.read.SubscriptionReadConnector.Responses.Response
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{JsPath, Reads}
import play.api.libs.ws.WSClient

import scala.concurrent.{ExecutionContext, Future}

class SubscriptionReadConnector @Inject() (frontendAppConfig: FrontendAppConfig, wsClient: WSClient)
    extends SubscriptionConnector[Request, Response](frontendAppConfig, wsClient) {

  override def call(request: Request)(implicit
    executionContext: ExecutionContext
  ): Future[Either[BaseConnector.Responses.Errors, Option[Response]]] =
    get(baseUrl().append(request.id))
}

object SubscriptionReadConnector {

  object Requests {

    final case class Request(id: String)

  }

  object Responses {

    final case class Response(
      id: String,
      name: String,
      contacts: Seq[Contact]
    )

    object Response {
      implicit lazy val reads: Reads[Response] =
        ((JsPath \ "id").read[String] and
          (JsPath \ "name").read[String] and
          (JsPath \ "contacts").read[Seq[Contact]])(Response.apply _)
    }
  }
}
