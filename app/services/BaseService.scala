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

package services

import connectors.BaseConnector
import converters.BaseConverter
import play.api.libs.json.{JsSuccess, Reads}
import services.BaseService.Responses

import scala.concurrent.{ExecutionContext, Future}

class BaseService[SERVICE_REQUEST, SERVICE_RESPONSE, CONNECTOR_REQUEST, CONNECTOR_RESPONSE](
  connector: BaseConnector[CONNECTOR_REQUEST, CONNECTOR_RESPONSE],
  converter: BaseConverter[SERVICE_REQUEST, CONNECTOR_REQUEST, CONNECTOR_RESPONSE, SERVICE_RESPONSE]
) {

  final def call(request: SERVICE_REQUEST)(implicit
    executionContext: ExecutionContext
  ): Future[Either[Responses.Errors, Option[SERVICE_RESPONSE]]] =
    connector.call(converter.convertServiceRequest(request)).map {
      case Right(responseOpt) => Right(converter.convertSuccessfulConnectorResponse(responseOpt))
      case Left(errors)       => Left(converter.convertFailedConnectorResponse(errors))
    }

}

object BaseService {

  object Responses {

    final case class EmptyResponse()

    object EmptyResponse {
      implicit lazy val reads: Reads[EmptyResponse] = Reads.apply(_ => JsSuccess(EmptyResponse()))
    }

    final case class Errors(status: Int, errors: Seq[Error] = Seq.empty)

    final case class Error(code: String)

  }

}
