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

package connectors

import com.google.inject.{Inject, Singleton}
import config.FrontendAppConfig
import connectors.RegistrationWithIdForIndividualConnector.Requests.Request
import connectors.RegistrationWithIdForIndividualConnector.Responses.Response
import play.api.libs.functional.syntax.{toFunctionalBuilderOps, unlift}
import play.api.libs.json.{JsPath, OWrites, Reads}
import play.api.libs.ws.WSClient
import uk.gov.hmrc.http.StringContextOps

import java.net.URL
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RegistrationWithIdForIndividualConnector @Inject() (frontendAppConfig: FrontendAppConfig, wsClient: WSClient) extends BaseBackendConnector(wsClient) {

  def call(request: Request)(implicit
    executionContext: ExecutionContext
  ): Future[Either[BaseConnector.Responses.Errors, Response]] =
    post[Request, Response](request)

  override def url(): URL =
    url"${frontendAppConfig.registrationWithIdForIndividualBaseUrl + RegistrationWithIdForIndividualConnector.connectorPath}"

}

object RegistrationWithIdForIndividualConnector {

  val connectorPath: String = "/registrations/withId/individual"

  object Requests {

    import RegistrationWithIdConnector.{Requests => CommonRequests}

    final case class Request(id: CommonRequests.Id, firstName: String, middleName: Option[String], lastName: String, dateOfBirth: String)

    object Request {
      implicit lazy val writes: OWrites[Request] =
        ((JsPath \ "id").write[CommonRequests.Id] and
          (JsPath \ "firstName").write[String] and
          (JsPath \ "middleName").writeNullable[String] and
          (JsPath \ "lastName").write[String] and
          (JsPath \ "dateOfBirth").write[String])(unlift(Request.unapply))
    }

  }

  object Responses {

    import RegistrationWithIdConnector.{Responses => CommonResponses}

    final case class Response(ids: Seq[CommonResponses.Id],
                              firstName: String,
                              middleName: Option[String],
                              lastName: String,
                              dateOfBirth: Option[String],
                              address: CommonResponses.Address,
                              contactDetails: CommonResponses.ContactDetails
    )

    object Response {

      implicit lazy val reads: Reads[Response] =
        ((JsPath \ "ids").read[Seq[CommonResponses.Id]] and
          (JsPath \ "firstName").read[String] and
          (JsPath \ "middleName").readNullable[String] and
          (JsPath \ "lastName").read[String] and
          (JsPath \ "dateOfBirth").readNullable[String] and
          (JsPath \ "address").read[CommonResponses.Address] and
          (JsPath \ "contactDetails").read[CommonResponses.ContactDetails])(Response.apply _)

    }

  }
}
