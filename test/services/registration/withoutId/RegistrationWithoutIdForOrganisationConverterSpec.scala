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

import base.BaseSpec
import connectors.registration.RegistrationConnector.{Requests => ConnectorRequestsCommon, Responses => ConnectorResponsesCommon}
import connectors.registration.withoutId.RegistrationWithoutIdForOrganisationConnector.{Requests => ConnectorRequests}
import services.registration.RegistrationService
import services.registration.withoutId.RegistrationWithoutIdForOrganisationService.{Requests => ServiceRequests}
import services.registration.withoutId.RegistrationWithoutIdService.{Requests => ServiceRequestsCommon}

class RegistrationWithoutIdForOrganisationConverterSpec extends BaseSpec {

  private val converter = new RegistrationWithoutIdForOrganisationService.Converter

  "when registering as an organisation, the converter returns the expected value, for a " - {
    "service request" in {
      val serviceRequest = ServiceRequests.Request(
        name = "Dyson",
        address = ServiceRequestsCommon.Address(lineOne = "34 Park Lane",
                                                lineTwo = Some("Building A"),
                                                lineThree = Some("Suite 100"),
                                                lineFour = Some("Manchester"),
                                                postalCode = "M54 1MQ",
                                                countryCode = "GB"
        ),
        contactDetails = ServiceRequestsCommon.ContactDetails(landline = Some("747663966"),
                                                              mobile = Some("38390756243"),
                                                              fax = Some("58371813020"),
                                                              emailAddress = Some("dyson@example.com")
        )
      )

      val connectorRequest = converter.convert(serviceRequest)

      connectorRequest shouldBe ConnectorRequests.Request(
        name = "Dyson",
        address = ConnectorRequestsCommon.Address(lineOne = "34 Park Lane",
                                                  lineTwo = Some("Building A"),
                                                  lineThree = Some("Suite 100"),
                                                  lineFour = Some("Manchester"),
                                                  postalCode = "M54 1MQ",
                                                  countryCode = "GB"
        ),
        contactDetails = ConnectorRequestsCommon.ContactDetails(landline = Some("747663966"),
                                                                mobile = Some("38390756243"),
                                                                fax = Some("58371813020"),
                                                                emailAddress = Some("dyson@example.com")
        )
      )
    }
    "connector response" in {
      val connectorResponse = ConnectorResponsesCommon.Response(
        Seq(
          ConnectorResponsesCommon.Id("ARN", "WARN3849921"),
          ConnectorResponsesCommon.Id("SAFE", "XE0000200775706"),
          ConnectorResponsesCommon.Id("SAP", "1960629967"),
          ConnectorResponsesCommon.Id("CAT", "25562288-ae0d-447a-863a-aac881b287a9")
        )
      )

      val serviceResponse = converter.convert(connectorResponse)

      serviceResponse shouldBe RegistrationService.Responses.Response(
        Seq(
          RegistrationService.Responses.Id(RegistrationService.Responses.IdType.ARN, "WARN3849921"),
          RegistrationService.Responses.Id(RegistrationService.Responses.IdType.SAFE, "XE0000200775706"),
          RegistrationService.Responses.Id(RegistrationService.Responses.IdType.SAP, "1960629967"),
          RegistrationService.Responses.Id(RegistrationService.Responses.IdType.UNKNOWN, "25562288-ae0d-447a-863a-aac881b287a9")
        )
      )
    }
  }
}
