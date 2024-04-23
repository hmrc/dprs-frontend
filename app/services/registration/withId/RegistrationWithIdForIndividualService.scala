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

package services.registration.withId

import com.google.inject.{Inject, Singleton}
import connectors.registration.withId.{RegistrationWithIdConnector, RegistrationWithIdForIndividualConnector}
import services.BaseService
import services.registration.RegistrationService

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RegistrationWithIdForIndividualService @Inject() (connector: RegistrationWithIdForIndividualConnector) {

  private val converter = new RegistrationWithIdForIndividualService.Converter

  def call(request: RegistrationWithIdForIndividualService.Requests.Request)(implicit
    executionContext: ExecutionContext
  ): Future[Either[BaseService.Responses.Errors, RegistrationWithIdForIndividualService.Responses.Response]] =
    connector.call(converter.convert(request)).map {
      case Right(response) => Right(converter.convert(response))
      case Left(errors)    => Left(converter.convert(errors))
    }
}

object RegistrationWithIdForIndividualService {

  object Requests {

    final case class Request(id: Id, firstName: String, middleName: Option[String], lastName: String, dateOfBirth: String)

    final case class Id(idType: IdType, value: String)

    sealed trait IdType

    object IdType {

      val all: Set[IdType] = Set(EORI, NINO, UTR)

      case object EORI extends IdType
      case object NINO extends IdType
      case object UTR extends IdType

    }

  }

  object Responses {

    import RegistrationWithIdService.Responses._

    final case class Response(ids: Seq[RegistrationService.Responses.Id],
                              firstName: String,
                              middleName: Option[String],
                              lastName: String,
                              dateOfBirth: Option[String],
                              address: Address,
                              contactDetails: ContactDetails
    )

  }

  class Converter extends RegistrationWithIdService.Converter {

    def convert(request: RegistrationWithIdForIndividualService.Requests.Request): RegistrationWithIdForIndividualConnector.Requests.Request =
      RegistrationWithIdForIndividualConnector.Requests.Request(id = convert(request.id),
                                                                firstName = request.firstName,
                                                                middleName = request.middleName,
                                                                lastName = request.lastName,
                                                                dateOfBirth = request.dateOfBirth
      )

    def convert(response: RegistrationWithIdForIndividualConnector.Responses.Response): RegistrationWithIdForIndividualService.Responses.Response =
      RegistrationWithIdForIndividualService.Responses.Response(
        ids = response.ids.map(convert),
        firstName = response.firstName,
        middleName = response.middleName,
        lastName = response.lastName,
        dateOfBirth = response.dateOfBirth,
        address = convert(response.address),
        contactDetails = convert(response.contactDetails)
      )

    private def convert(id: RegistrationWithIdForIndividualService.Requests.Id): RegistrationWithIdConnector.Requests.Id =
      RegistrationWithIdConnector.Requests.Id(idType = convert(id.idType), value = id.value)

    private def convert(idType: RegistrationWithIdForIndividualService.Requests.IdType): String = idType match {
      case RegistrationWithIdForIndividualService.Requests.IdType.EORI => "EORI"
      case RegistrationWithIdForIndividualService.Requests.IdType.NINO => "NINO"
      case RegistrationWithIdForIndividualService.Requests.IdType.UTR  => "UTR"
    }

  }

}
