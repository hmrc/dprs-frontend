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

package converters

import connectors.BaseConnector
import services.BaseService

abstract class BaseConverter[SERVICE_REQUEST, CONNECTOR_REQUEST, CONNECTOR_RESPONSE, SERVICE_RESPONSE] {

  def convertServiceRequest(serviceRequest: SERVICE_REQUEST): CONNECTOR_REQUEST

  def convertSuccessfulConnectorResponse(connectorResponse: Option[CONNECTOR_RESPONSE]): Option[SERVICE_RESPONSE]

  def convertFailedConnectorResponse(errors: BaseConnector.Responses.Errors): BaseService.Responses.Errors =
    BaseService.Responses.Errors(status = errors.status, errors = errors.errors.map(convert))

  private def convert(error: BaseConnector.Responses.Error): BaseService.Responses.Error =
    BaseService.Responses.Error(error.code)

}
