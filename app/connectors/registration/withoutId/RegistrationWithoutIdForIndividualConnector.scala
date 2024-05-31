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
import connectors.registration.RegistrationConnector.Responses.Response
import connectors.registration.withoutId.RegistrationWithoutIdForIndividualConnector.Requests.Request
import play.api.libs.functional.syntax.{toFunctionalBuilderOps, unlift}
import play.api.libs.json.{JsPath, OWrites}
import play.api.libs.ws.WSClient

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RegistrationWithoutIdForIndividualConnector @Inject() (frontendAppConfig: FrontendAppConfig, wsClient: WSClient)
    extends RegistrationConnector[Request, Response](frontendAppConfig, wsClient) {

  override def connectorPath: String = RegistrationWithoutIdForIndividualConnector.connectorPath

  override def call(request: Request)(implicit
    executionContext: ExecutionContext
  ): Future[Either[BaseConnector.Responses.Errors, Option[Response]]] =
    post(request)

}

object RegistrationWithoutIdForIndividualConnector {

  val connectorPath: String = "/registrations/withoutId/individual"

  object Requests {

    import connectors.registration.RegistrationConnector.{Requests => CommonRequests}

    final case class Request(firstName: String,
                             middleName: Option[String],
                             lastName: String,
                             dateOfBirth: String,
                             address: CommonRequests.Address,
                             contactDetails: CommonRequests.ContactDetails
    )

    object Request {
      implicit lazy val writes: OWrites[Request] =
        ((JsPath \ "firstName").write[String] and
          (JsPath \ "middleName").writeNullable[String] and
          (JsPath \ "lastName").write[String] and
          (JsPath \ "dateOfBirth").write[String] and
          (JsPath \ "address").write[CommonRequests.Address] and
          (JsPath \ "contactDetails").write[CommonRequests.ContactDetails])(unlift(Request.unapply))
    }

  }

}
