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

import connectors.registration.RegistrationConnector
import services.BaseService

object RegistrationService {

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

  abstract class Converter extends BaseService.Converter {

    private val idTypes = Map(
      "ARN"  -> RegistrationService.Responses.IdType.ARN,
      "SAP"  -> RegistrationService.Responses.IdType.SAP,
      "SAFE" -> RegistrationService.Responses.IdType.SAFE
    )

    protected def convert(id: RegistrationConnector.Responses.Id): RegistrationService.Responses.Id =
      RegistrationService.Responses.Id(idType = convert(id.idType), value = id.value)

    protected def convert(idType: String): RegistrationService.Responses.IdType =
      idTypes.getOrElse(idType, RegistrationService.Responses.IdType.UNKNOWN)
  }
}
