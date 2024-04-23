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

package services.registration.withoutId

import com.google.inject.{Inject, Singleton}
import connectors.registration.withoutId.RegistrationWithoutIdForOrganisationConnector
import services.BaseService
import services.registration.RegistrationService
import services.registration.withoutId.RegistrationWithoutIdForOrganisationService.Converter

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RegistrationWithoutIdForOrganisationService @Inject() (connector: RegistrationWithoutIdForOrganisationConnector) {

  private val converter = new Converter

  def call(request: RegistrationWithoutIdForOrganisationService.Requests.Request)(implicit
    executionContext: ExecutionContext
  ): Future[Either[BaseService.Responses.Errors, RegistrationService.Responses.Response]] =
    connector.call(converter.convert(request)).map {
      case Right(response) => Right(converter.convert(response))
      case Left(errors)    => Left(converter.convert(errors))
    }

}

object RegistrationWithoutIdForOrganisationService {

  import RegistrationWithoutIdService.Requests.{Address, ContactDetails}

  object Requests {

    final case class Request(name: String, address: Address, contactDetails: ContactDetails)

  }

  class Converter extends RegistrationWithoutIdService.Converter {

    def convert(request: RegistrationWithoutIdForOrganisationService.Requests.Request): RegistrationWithoutIdForOrganisationConnector.Requests.Request =
      RegistrationWithoutIdForOrganisationConnector.Requests.Request(
        name = request.name,
        address = convert(request.address),
        contactDetails = convert(request.contactDetails)
      )

  }

}
