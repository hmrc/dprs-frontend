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
import connectors.registration.withId.RegistrationWithIdForIndividualConnector
import connectors.registration.withId.RegistrationWithIdForIndividualConnector.{Requests => ConnectorRequests, Responses => ConnectorResponses}
import converters.registration.withId.RegistrationWithIdForIndividualConverter
import services.registration.withId.RegistrationWithIdForIndividualService.{Requests => ServiceRequests, Responses => ServiceResponses}

@Singleton
class RegistrationWithIdForIndividualService @Inject() (connector: RegistrationWithIdForIndividualConnector,
                                                        converter: RegistrationWithIdForIndividualConverter
) extends BaseRegistrationWithIdService[ServiceRequests.Request, ServiceResponses.Response, ConnectorRequests.Request, ConnectorResponses.Response](
      connector,
      converter
    )

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
      case object UNKNOWN extends IdType

    }

  }

  object Responses {

    import services.registration.BaseRegistrationService.{Responses => CommonResponses}
    import services.registration.withId.BaseRegistrationWithIdService.{Responses => ServiceResponses}

    final case class Response(ids: Seq[CommonResponses.Id],
                              firstName: String,
                              middleName: Option[String],
                              lastName: String,
                              dateOfBirth: Option[String],
                              address: ServiceResponses.Address,
                              contactDetails: ServiceResponses.ContactDetails
    )

  }

}
