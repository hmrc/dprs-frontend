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
import connectors.registration.withId.{RegistrationWithIdConnector, RegistrationWithIdForOrganisationConnector}
import services.BaseService
import services.registration.RegistrationService
import services.registration.withId.RegistrationWithIdForOrganisationService.Converter
import services.registration.withId.RegistrationWithIdForOrganisationService.Requests.Request
import services.registration.withId.RegistrationWithIdForOrganisationService.Responses.Response

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RegistrationWithIdForOrganisationService @Inject() (connector: RegistrationWithIdForOrganisationConnector) {

  private val converter = new Converter

  def call(request: Request)(implicit
    executionContext: ExecutionContext
  ): Future[Either[BaseService.Responses.Errors, Response]] =
    connector.call(converter.convert(request)).map {
      case Right(response) => Right(converter.convert(response))
      case Left(errors)    => Left(converter.convert(errors))
    }

}

object RegistrationWithIdForOrganisationService {

  object Requests {

    final case class Request(id: Id, name: String, _type: Type)

    final case class Id(idType: IdType, value: String)

    sealed trait IdType

    object IdType {

      val all: Set[IdType] = Set(EORI, UTR)

      case object EORI extends IdType
      case object UTR extends IdType

    }

    sealed trait Type

    object Type {
      val all: Set[Type] =
        Set(NotSpecified, Partnership, LimitedLiabilityPartnership, CorporateBody, UnincorporatedBody)

      case object NotSpecified extends Type
      case object Partnership extends Type
      case object LimitedLiabilityPartnership extends Type
      case object CorporateBody extends Type
      case object UnincorporatedBody extends Type
    }

  }

  object Responses {

    import RegistrationWithIdService.Responses._

    final case class Response(ids: Seq[RegistrationService.Responses.Id], name: String, _type: Type, address: Address, contactDetails: ContactDetails)

    sealed trait Type

    object Type {

      val all: Set[Type] =
        Set(NotSpecified, Partnership, LimitedLiabilityPartnership, CorporateBody, UnincorporatedBody, Unknown)

      case object CorporateBody extends Type
      case object LimitedLiabilityPartnership extends Type
      case object NotSpecified extends Type
      case object Partnership extends Type
      case object UnincorporatedBody extends Type
      case object Unknown extends Type

    }

  }

  class Converter extends RegistrationWithIdService.Converter {

    private val responseTypes = Map(
      "NotSpecified"                -> RegistrationWithIdForOrganisationService.Responses.Type.NotSpecified,
      "Partnership"                 -> RegistrationWithIdForOrganisationService.Responses.Type.Partnership,
      "LimitedLiabilityPartnership" -> RegistrationWithIdForOrganisationService.Responses.Type.LimitedLiabilityPartnership,
      "CorporateBody"               -> RegistrationWithIdForOrganisationService.Responses.Type.CorporateBody,
      "UnincorporatedBody"          -> RegistrationWithIdForOrganisationService.Responses.Type.UnincorporatedBody
    )

    def convert(request: RegistrationWithIdForOrganisationService.Requests.Request): RegistrationWithIdForOrganisationConnector.Requests.Request =
      RegistrationWithIdForOrganisationConnector.Requests.Request(id = convert(request.id), name = request.name, _type = convert(request._type))

    def convert(response: RegistrationWithIdForOrganisationConnector.Responses.Response): RegistrationWithIdForOrganisationService.Responses.Response =
      RegistrationWithIdForOrganisationService.Responses.Response(
        ids = response.ids.map(convert),
        name = response.name,
        _type = convertResponseType(response._type),
        address = convert(response.address),
        contactDetails = convert(response.contactDetails)
      )

    private def convert(id: RegistrationWithIdForOrganisationService.Requests.Id): RegistrationWithIdConnector.Requests.Id =
      RegistrationWithIdConnector.Requests.Id(idType = convert(id.idType), value = id.value)

    private def convert(idType: RegistrationWithIdForOrganisationService.Requests.IdType): String =
      idType match {
        case RegistrationWithIdForOrganisationService.Requests.IdType.EORI => "EORI"
        case RegistrationWithIdForOrganisationService.Requests.IdType.UTR  => "UTR"
      }

    private def convert(_type: RegistrationWithIdForOrganisationService.Requests.Type): String =
      _type match {
        case RegistrationWithIdForOrganisationService.Requests.Type.NotSpecified                => "NotSpecified"
        case RegistrationWithIdForOrganisationService.Requests.Type.Partnership                 => "Partnership"
        case RegistrationWithIdForOrganisationService.Requests.Type.LimitedLiabilityPartnership => "LimitedLiabilityPartnership"
        case RegistrationWithIdForOrganisationService.Requests.Type.CorporateBody               => "CorporateBody"
        case RegistrationWithIdForOrganisationService.Requests.Type.UnincorporatedBody          => "UnincorporatedBody"
      }

    private def convertResponseType(_type: String): RegistrationWithIdForOrganisationService.Responses.Type =
      responseTypes.getOrElse(_type, RegistrationWithIdForOrganisationService.Responses.Type.Unknown)

  }

}
