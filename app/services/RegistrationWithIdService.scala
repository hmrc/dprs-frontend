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

import com.google.inject.{Inject, Singleton}
import connectors.{RegistrationWithIdForIndividualConnector, RegistrationWithIdForOrganisationConnector}
import services.RegistrationWithIdService.Individual.BaseConverter
import services.RegistrationWithIdService.Organisation.Requests.Request
import services.RegistrationWithIdService.Organisation.Responses.Response
import services.RegistrationWithIdService.{Individual, Organisation}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RegistrationWithIdService @Inject() (individualConnector: RegistrationWithIdForIndividualConnector,
                                           organisationConnector: RegistrationWithIdForOrganisationConnector
) {

  private val individualConverter   = new Individual.Converter
  private val organisationConverter = new Organisation.Converter

  def asIndividual(request: RegistrationWithIdService.Individual.Requests.Request)(implicit
    executionContext: ExecutionContext
  ): Future[Either[BaseService.Responses.Errors, RegistrationWithIdService.Individual.Responses.Response]] =
    individualConnector.call(individualConverter.convert(request)).map {
      case Right(response) => Right(individualConverter.convert(response))
      case Left(errors)    => Left(individualConverter.convert(errors))
    }

  def asOrganisation(request: Request)(implicit
    executionContext: ExecutionContext
  ): Future[Either[BaseService.Responses.Errors, Response]] =
    organisationConnector.call(organisationConverter.convert(request)).map {
      case Right(response) => Right(organisationConverter.convert(response))
      case Left(errors)    => Left(organisationConverter.convert(errors))
    }

}

object RegistrationWithIdService {

  object Common {

    object Responses {

      final case class Id(idType: IdType, value: String)

      sealed trait IdType

      object IdType {
        case object ARN extends IdType
        case object SAP extends IdType
        case object SAFE extends IdType
        case object UNKNOWN extends IdType
      }

      final case class Address(lineOne: String,
                               lineTwo: Option[String],
                               lineThree: Option[String],
                               lineFour: Option[String],
                               postalCode: String,
                               countryCode: String
      )

      final case class ContactDetails(landline: Option[String], mobile: Option[String], fax: Option[String], emailAddress: Option[String])

    }

  }

  object Individual {

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

      import Common.Responses._

      final case class Response(ids: Seq[Id],
                                firstName: String,
                                middleName: Option[String],
                                lastName: String,
                                dateOfBirth: Option[String],
                                address: Address,
                                contactDetails: ContactDetails
      )

    }

    abstract class BaseConverter extends BaseService.Converter {

      import connectors.RegistrationWithIdConnector.{Responses => CommonConnectorResponses}
      import services.RegistrationWithIdService.Common.{Responses => CommonServiceResponses}

      private val idTypes = Map(
        "ARN"  -> CommonServiceResponses.IdType.ARN,
        "SAP"  -> CommonServiceResponses.IdType.SAP,
        "SAFE" -> CommonServiceResponses.IdType.SAFE
      )

      def convert(id: CommonConnectorResponses.Id): CommonServiceResponses.Id =
        CommonServiceResponses.Id(idType = convert(id.idType), value = id.value)

      def convert(address: CommonConnectorResponses.Address): CommonServiceResponses.Address =
        CommonServiceResponses.Address(
          lineOne = address.lineOne,
          lineTwo = address.lineTwo,
          lineThree = address.lineThree,
          lineFour = address.lineFour,
          postalCode = address.postalCode,
          countryCode = address.countryCode
        )

      def convert(
        contactDetails: CommonConnectorResponses.ContactDetails
      ): CommonServiceResponses.ContactDetails = CommonServiceResponses.ContactDetails(
        landline = contactDetails.landline,
        mobile = contactDetails.mobile,
        fax = contactDetails.fax,
        emailAddress = contactDetails.emailAddress
      )

      private def convert(idType: String): CommonServiceResponses.IdType =
        idTypes.getOrElse(idType, CommonServiceResponses.IdType.UNKNOWN)
    }

    class Converter extends BaseConverter {

      import connectors.RegistrationWithIdConnector.{Requests => CommonConnectorRequests}
      import connectors.RegistrationWithIdForIndividualConnector.{Requests => ConnectorRequests, Responses => ConnectorResponses}
      import services.RegistrationWithIdService.Individual.{Requests => ServiceRequests, Responses => ServiceResponses}

      def convert(request: ServiceRequests.Request): ConnectorRequests.Request =
        ConnectorRequests.Request(id = convert(request.id),
                                  firstName = request.firstName,
                                  middleName = request.middleName,
                                  lastName = request.lastName,
                                  dateOfBirth = request.dateOfBirth
        )

      def convert(response: ConnectorResponses.Response): ServiceResponses.Response =
        ServiceResponses.Response(
          ids = response.ids.map(convert),
          firstName = response.firstName,
          middleName = response.middleName,
          lastName = response.lastName,
          dateOfBirth = response.dateOfBirth,
          address = convert(response.address),
          contactDetails = convert(response.contactDetails)
        )

      private def convert(id: ServiceRequests.Id): CommonConnectorRequests.Id =
        CommonConnectorRequests.Id(idType = convert(id.idType), value = id.value)

      private def convert(idType: ServiceRequests.IdType): String = idType match {
        case ServiceRequests.IdType.EORI => "EORI"
        case ServiceRequests.IdType.NINO => "NINO"
        case ServiceRequests.IdType.UTR  => "UTR"
      }

    }

  }

  object Organisation {

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

      import Common.Responses._

      final case class Response(ids: Seq[Id], name: String, _type: Type, address: Address, contactDetails: ContactDetails)

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

    class Converter extends BaseConverter {

      import connectors.RegistrationWithIdConnector.{Requests => CommonConnectorRequests}
      import connectors.RegistrationWithIdForOrganisationConnector.{Requests => ConnectorRequests, Responses => ConnectorResponses}
      import services.RegistrationWithIdService.Organisation.{Requests => ServiceRequests, Responses => ServiceResponses}

      private val responseTypes = Map(
        "NotSpecified"                -> ServiceResponses.Type.NotSpecified,
        "Partnership"                 -> ServiceResponses.Type.Partnership,
        "LimitedLiabilityPartnership" -> ServiceResponses.Type.LimitedLiabilityPartnership,
        "CorporateBody"               -> ServiceResponses.Type.CorporateBody,
        "UnincorporatedBody"          -> ServiceResponses.Type.UnincorporatedBody
      )

      def convert(request: Request): ConnectorRequests.Request =
        ConnectorRequests.Request(id = convert(request.id), name = request.name, _type = convert(request._type))

      def convert(response: ConnectorResponses.Response): Response =
        Response(
          ids = response.ids.map(convert),
          name = response.name,
          _type = convertResponseType(response._type),
          address = convert(response.address),
          contactDetails = convert(response.contactDetails)
        )

      private def convert(id: ServiceRequests.Id): CommonConnectorRequests.Id =
        CommonConnectorRequests.Id(idType = convert(id.idType), value = id.value)

      private def convert(idType: ServiceRequests.IdType): String =
        idType match {
          case ServiceRequests.IdType.EORI => "EORI"
          case ServiceRequests.IdType.UTR  => "UTR"
        }

      private def convert(_type: ServiceRequests.Type): String =
        _type match {
          case ServiceRequests.Type.NotSpecified                => "NotSpecified"
          case ServiceRequests.Type.Partnership                 => "Partnership"
          case ServiceRequests.Type.LimitedLiabilityPartnership => "LimitedLiabilityPartnership"
          case ServiceRequests.Type.CorporateBody               => "CorporateBody"
          case ServiceRequests.Type.UnincorporatedBody          => "UnincorporatedBody"
        }

      private def convertResponseType(_type: String): ServiceResponses.Type =
        responseTypes.getOrElse(_type, ServiceResponses.Type.Unknown)

    }

  }
}
