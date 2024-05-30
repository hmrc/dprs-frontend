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

package connectors.subscription.update

import com.google.inject.Inject
import config.FrontendAppConfig
import connectors.subscription.SubscriptionConnector.Requests.Contact
import connectors.subscription.update.SubscriptionUpdateConnector.Requests.Request
import connectors.{BaseBackendConnector, BaseConnector}
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{JsPath, OWrites}
import play.api.libs.ws.WSClient

import java.net.URL
import scala.concurrent.{ExecutionContext, Future}

class SubscriptionUpdateConnector @Inject() (frontendAppConfig: FrontendAppConfig, wsClient: WSClient)
    extends BaseBackendConnector[Request, BaseConnector.Responses.EmptyResponse](frontendAppConfig, wsClient) {

  override def connectorPath: String = SubscriptionUpdateConnector.connectorPath

  override def call(request: Request)(implicit
    executionContext: ExecutionContext
  ): Future[Either[BaseConnector.Responses.Errors, Option[BaseConnector.Responses.EmptyResponse]]] =
    post(fullUrl(request), request)

  private def fullUrl(request: Request) =
    new URL(s"${baseUrl().toString}/${request.id}")
}

object SubscriptionUpdateConnector {

  val connectorPath: String = "/subscriptions"

  object Requests {

    final case class Request(
      id: String,
      name: Option[String],
      contacts: Seq[Contact]
    )

    object Request {
      implicit lazy val writes: OWrites[Request] =
        ((JsPath \ "name").writeNullable[String] and
          (JsPath \ "contacts").write[Seq[Contact]])(r => (r.name, r.contacts))
    }
  }

}
