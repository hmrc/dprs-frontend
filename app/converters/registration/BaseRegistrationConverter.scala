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

package converters.registration

import connectors.registration.RegistrationConnector
import converters.BaseConverter
import services.registration.BaseRegistrationService

abstract class BaseRegistrationConverter[SERVICE_REQUEST, CONNECTOR_REQUEST, CONNECTOR_RESPONSE, SERVICE_RESPONSE]
    extends BaseConverter[SERVICE_REQUEST, CONNECTOR_REQUEST, CONNECTOR_RESPONSE, SERVICE_RESPONSE] {

  private val idTypes = Map(
    "ARN"  -> BaseRegistrationService.Responses.IdType.ARN,
    "SAP"  -> BaseRegistrationService.Responses.IdType.SAP,
    "SAFE" -> BaseRegistrationService.Responses.IdType.SAFE
  )

  protected def convert(id: RegistrationConnector.Responses.Id): BaseRegistrationService.Responses.Id =
    BaseRegistrationService.Responses.Id(idType = convert(id.idType), value = id.value)

  protected def convert(idType: String): BaseRegistrationService.Responses.IdType =
    idTypes.getOrElse(idType, BaseRegistrationService.Responses.IdType.UNKNOWN)

}
