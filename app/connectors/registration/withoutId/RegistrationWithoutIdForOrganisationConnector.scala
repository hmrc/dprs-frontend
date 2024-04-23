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

package connectors.registration.withoutId

import com.google.inject.{Inject, Singleton}
import config.FrontendAppConfig
import connectors.BaseConnector
import connectors.registration.RegistrationConnector
import connectors.registration.RegistrationConnector.{Responses => ConnectorResponses}
import connectors.registration.withoutId.RegistrationWithoutIdForOrganisationConnector.{Requests => ConnectorRequests}
import play.api.libs.functional.syntax.{toFunctionalBuilderOps, unlift}
import play.api.libs.json.{JsPath, OWrites}
import play.api.libs.ws.WSClient

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RegistrationWithoutIdForOrganisationConnector @Inject() (frontendAppConfig: FrontendAppConfig, wsClient: WSClient)
    extends RegistrationConnector[ConnectorRequests.Request, ConnectorResponses.Response](frontendAppConfig, wsClient) {

  override def connectorPath: String = RegistrationWithoutIdForOrganisationConnector.connectorPath

  override def call(request: ConnectorRequests.Request)(implicit
    executionContext: ExecutionContext
  ): Future[Either[BaseConnector.Responses.Errors, ConnectorResponses.Response]] =
    post(request)

}

object RegistrationWithoutIdForOrganisationConnector {

  val connectorPath: String = "/registrations/withoutId/organisation"

  object Requests {

    final case class Request(name: String, address: RegistrationConnector.Requests.Address, contactDetails: RegistrationConnector.Requests.ContactDetails)

    object Request {
      implicit lazy val writes: OWrites[Request] =
        ((JsPath \ "name").write[String] and
          (JsPath \ "address").write[RegistrationConnector.Requests.Address] and
          (JsPath \ "contactDetails").write[RegistrationConnector.Requests.ContactDetails])(unlift(Request.unapply))
    }

  }

}
