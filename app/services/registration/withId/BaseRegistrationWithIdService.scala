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

import connectors.BaseConnector
import converters.BaseConverter
import services.registration.BaseRegistrationService

abstract class BaseRegistrationWithIdService[SERVICE_REQUEST, SERVICE_RESPONSE, CONNECTOR_REQUEST, CONNECTOR_RESPONSE](
  connector: BaseConnector[CONNECTOR_REQUEST, CONNECTOR_RESPONSE],
  converter: BaseConverter[SERVICE_REQUEST, CONNECTOR_REQUEST, CONNECTOR_RESPONSE, SERVICE_RESPONSE]
) extends BaseRegistrationService[SERVICE_REQUEST, SERVICE_RESPONSE, CONNECTOR_REQUEST, CONNECTOR_RESPONSE](connector, converter)

object BaseRegistrationWithIdService {

  object Responses {

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
