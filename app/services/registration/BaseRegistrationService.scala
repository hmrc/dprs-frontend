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

package services.registration

import connectors.BaseConnector
import converters.BaseConverter
import services.BaseService

abstract class BaseRegistrationService[SERVICE_REQUEST, SERVICE_RESPONSE, CONNECTOR_REQUEST, CONNECTOR_RESPONSE](
  connector: BaseConnector[CONNECTOR_REQUEST, CONNECTOR_RESPONSE],
  converter: BaseConverter[SERVICE_REQUEST, CONNECTOR_REQUEST, CONNECTOR_RESPONSE, SERVICE_RESPONSE]
) extends BaseService[SERVICE_REQUEST, SERVICE_RESPONSE, CONNECTOR_REQUEST, CONNECTOR_RESPONSE](connector, converter)

object BaseRegistrationService {

  object Responses {

    final case class Id(idType: IdType, value: String)

    sealed trait IdType

    object IdType {
      case object ARN extends IdType

      case object SAP extends IdType

      case object SAFE extends IdType

      case object UNKNOWN extends IdType
    }

    final case class Response(ids: Seq[Id])

  }

}
