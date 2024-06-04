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
import play.api.http.Status.{NO_CONTENT, OK}
import play.api.libs.json.Json.toJson
import play.api.libs.json.{JsPath, JsSuccess, Reads, Writes}
import play.api.libs.ws.{WSClient, WSResponse}

import java.net.URL
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

abstract class BaseConnector[REQUEST, RESPONSE](wsClient: WSClient) {

  def call(request: REQUEST)(implicit executionContext: ExecutionContext): Future[Either[BaseConnector.Responses.Errors, Option[RESPONSE]]]

  /** We would have liked to use HttpClientV2, but when it encounters a 400 or 500 status code, the response body is inaccessible.
    */
  final protected def get(url: URL)(implicit
    executionContext: ExecutionContext,
    reads: Reads[RESPONSE]
  ): Future[Either[Errors, Option[RESPONSE]]] =
    wsClient
      .url(url.toString)
      .get()
      .transform {
        case Success(wsResponse) =>
          wsResponse.status match {
            case OK              => asResponse(wsResponse)
            case otherStatusCode => asErrors(otherStatusCode, wsResponse)
          }
        case Failure(exception) => Failure(exception)
      }

  /** We would have liked to use HttpClientV2, but when it encounters a 400 or 500 status code, the response body is inaccessible.
    */
  final protected def post(url: URL, request: REQUEST)(implicit
    executionContext: ExecutionContext,
    writes: Writes[REQUEST],
    reads: Reads[RESPONSE]
  ): Future[Either[Errors, Option[RESPONSE]]] =
    wsClient
      .url(url.toString)
      .post(toJson(request))
      .transform {
        case Success(wsResponse) =>
          wsResponse.status match {
            case OK | NO_CONTENT => asResponse(wsResponse)
            case otherStatusCode => asErrors(otherStatusCode, wsResponse)
          }
        case Failure(exception) => Failure(exception)
      }

  final protected def post(request: REQUEST)(implicit
    executionContext: ExecutionContext,
    writes: Writes[REQUEST],
    reads: Reads[RESPONSE]
  ): Future[Either[Errors, Option[RESPONSE]]] =
    post(baseUrl(), request)

  def baseUrl(): URL

  private def asResponse(wsResponse: WSResponse)(implicit reads: Reads[RESPONSE]): Try[Either[Errors, Option[RESPONSE]]] =
    wsResponse.status match {
      case NO_CONTENT => Success(Right(None))
      case _ =>
        Try(
          wsResponse.json
            .validate[RESPONSE]
            .map(response => Success(Right(Some(response))))
            .get
        ) match {
          case Success(result) => result
          case Failure(_)      => Failure(new ResponseParsingException())
        }
    }

  private def asErrors(statusCode: Int, wsResponse: WSResponse): Try[Either[Errors, Option[RESPONSE]]] =
    if (wsResponse.body.nonEmpty) {
      Try(
        wsResponse.json
          .validate[Seq[BaseConnector.Responses.Error]]
          .map(errors => Success(Left(Errors(statusCode, errors))))
          .get
      ) match {
        case Success(result) => result
        case Failure(_)      => Failure(new ResponseParsingException())
      }
    } else Success(Left(Errors(statusCode)))

  implicit class UrlHelper(url: URL) {
    def append(id: String) =
      new URL(s"${url.toString}/$id")
  }
}

object BaseConnector {

  object Exceptions {
    final class ResponseParsingException extends RuntimeException
  }

  object Responses {

    final case class EmptyResponse()

    object EmptyResponse {
      implicit lazy val reads: Reads[EmptyResponse] = Reads.apply(_ => JsSuccess(EmptyResponse()))
    }

    final case class Errors(status: Int, errors: Seq[Error] = Seq.empty)

    final case class Error(code: String)

    object Error {
      implicit lazy val reads: Reads[Error] =
        (JsPath \ "code").read[String].map(Error(_))
    }
  }
}
