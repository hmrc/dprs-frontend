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
import connectors.registration.withId.RegistrationWithIdForOrganisationConnector
import connectors.registration.withId.RegistrationWithIdForOrganisationConnector.{Requests => ConnectorRequests, Responses => ConnectorResponses}
import converters.registration.withId.RegistrationWithIdForOrganisationConverter
import services.registration.withId.RegistrationWithIdForOrganisationService.{Requests => ServiceRequests, Responses => ServiceResponses}

@Singleton
class RegistrationWithIdForOrganisationService @Inject() (connector: RegistrationWithIdForOrganisationConnector,
                                                          converter: RegistrationWithIdForOrganisationConverter
) extends BaseRegistrationWithIdService[ServiceRequests.Request, ServiceResponses.Response, ConnectorRequests.Request, ConnectorResponses.Response](
      connector,
      converter
    )

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

    import services.registration.BaseRegistrationService.{Responses => CommonResponses}
    import services.registration.withId.BaseRegistrationWithIdService.{Responses => ServiceResponses}

    final case class Response(ids: Seq[CommonResponses.Id],
                              name: String,
                              _type: Type,
                              address: ServiceResponses.Address,
                              contactDetails: ServiceResponses.ContactDetails
    )

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

}
