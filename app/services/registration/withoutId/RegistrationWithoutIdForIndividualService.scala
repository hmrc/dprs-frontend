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
import connectors.registration.withoutId.RegistrationWithoutIdForIndividualConnector
import connectors.registration.withoutId.RegistrationWithoutIdForIndividualConnector.{Requests => ConnectorRequests}
import converters.registration.withoutId.RegistrationWithoutIdForIndividualConverter
import services.registration.withoutId.RegistrationWithoutIdForIndividualService.{Requests => ServiceRequests}

@Singleton
class RegistrationWithoutIdForIndividualService @Inject() (connector: RegistrationWithoutIdForIndividualConnector,
                                                           converter: RegistrationWithoutIdForIndividualConverter
) extends BaseRegistrationWithoutIdService[ServiceRequests.Request, ConnectorRequests.Request](
      connector,
      converter
    )

object RegistrationWithoutIdForIndividualService {

  object Requests {

    import services.registration.withoutId.BaseRegistrationWithoutIdService.{Requests => CommonRegistrationWithoutIdRequests}

    final case class Request(firstName: String,
                             middleName: Option[String],
                             lastName: String,
                             dateOfBirth: String,
                             address: CommonRegistrationWithoutIdRequests.Address,
                             contactDetails: CommonRegistrationWithoutIdRequests.ContactDetails
    )

  }

}
