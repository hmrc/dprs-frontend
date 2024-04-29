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

import connectors.BaseConnector.Exceptions.ResponseParsingException
import connectors.BaseConnector.Responses.Errors
import play.api.http.Status.OK
import play.api.libs.json.Json.toJson
import play.api.libs.json.{JsPath, Reads, Writes}
import play.api.libs.ws.{WSClient, WSResponse}

import java.net.URL
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

abstract class BaseConnector[REQUEST, RESPONSE](wsClient: WSClient) {

  def call(request: REQUEST)(implicit executionContext: ExecutionContext): Future[Either[BaseConnector.Responses.Errors, RESPONSE]]

  /** We would have liked to use HttpClientV2, but when it encounters a 400 or 500 status code, the response body is inaccessible.
    */
  def post(request: REQUEST)(implicit
    executionContext: ExecutionContext,
    writes: Writes[REQUEST],
    reads: Reads[RESPONSE]
  ): Future[Either[Errors, RESPONSE]] =
    wsClient
      .url(url().toString)
      .post(toJson(request))
      .transform {
        case Success(wsResponse) =>
          wsResponse.status match {
            case OK              => asResponse(wsResponse)
            case otherStatusCode => asErrors(otherStatusCode, wsResponse)
          }
        case Failure(exception) => Failure(exception)
      }

  def url(): URL

  private def asResponse(wsResponse: WSResponse)(implicit reads: Reads[RESPONSE]): Try[Either[Errors, RESPONSE]] =
    wsResponse.json
      .validate[RESPONSE]
      .map(response => Success(Right(response)))
      .getOrElse(Failure(new ResponseParsingException()))

  private def asErrors(statusCode: Int, wsResponse: WSResponse): Try[Either[Errors, RESPONSE]] =
    if (wsResponse.body.nonEmpty)
      wsResponse.json
        .validate[Seq[BaseConnector.Responses.Error]]
        .map(errors => Success(Left(Errors(statusCode, errors))))
        .getOrElse(Failure(new ResponseParsingException()))
    else Success(Left(Errors(statusCode)))
}

object BaseConnector {

  object Exceptions {
    final class ResponseParsingException extends RuntimeException
  }

  object Responses {

    final case class Errors(status: Int, errors: Seq[Error] = Seq.empty)

    final case class Error(code: String)

    object Error {
      implicit lazy val reads: Reads[Error] =
        (JsPath \ "code").read[String].map(Error(_))
    }
  }

}
