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

import base.BaseSpec
import connectors.registration.RegistrationConnector
import connectors.registration.withId.RegistrationWithIdConnector.{Requests => CommonConnectorRequests, Responses => CommonConnectorResponses}
import connectors.registration.withId.RegistrationWithIdForIndividualConnector.{Requests => ConnectorRequests, Responses => ConnectorResponses}
import services.registration.RegistrationService
import services.registration.withId.RegistrationWithIdForIndividualService.{Requests => ServiceRequests, Responses => ServiceResponses}
import services.registration.withId.RegistrationWithIdService.{Responses => CommonServiceResponses}

class RegistrationWithIdForIndividualConverterSpec extends BaseSpec {

  private val converter = new RegistrationWithIdForIndividualService.Converter

  "when registering as an individual, the converter returns the expected value, for a" - {
    "service request" in {
      val idTypes =
        Table(
          ("Type (Raw)", "Expected Type"),
          ("EORI", "EORI"),
          ("NINO", "NINO"),
          ("UTR", "UTR")
        )

      forAll(idTypes) { (rawType, expectedType) =>
        val idType = ServiceRequests.IdType.all.find(_.toString == rawType).get
        val serviceRequest =
          ServiceRequests.Request(id = ServiceRequests.Id(idType, "AA000000A"),
                                  firstName = "Patrick",
                                  middleName = Some("John"),
                                  lastName = "Dyson",
                                  dateOfBirth = "1970-10-04"
          )

        val connectorRequest = converter.convert(serviceRequest)

        connectorRequest shouldBe ConnectorRequests.Request(id = CommonConnectorRequests.Id(expectedType, "AA000000A"),
                                                            firstName = "Patrick",
                                                            middleName = Some("John"),
                                                            lastName = "Dyson",
                                                            dateOfBirth = "1970-10-04"
        )
      }
    }
    "connector response" in {
      val connectorResponse = ConnectorResponses.Response(
        ids = Seq(
          RegistrationConnector.Responses.Id("ARN", "WARN3849921"),
          RegistrationConnector.Responses.Id("SAFE", "XE0000200775706"),
          RegistrationConnector.Responses.Id("SAP", "1960629967"),
          RegistrationConnector.Responses.Id("CAT", "25562288-ae0d-447a-863a-aac881b287a9")
        ),
        firstName = "Patrick",
        middleName = Some("John"),
        lastName = "Dyson",
        dateOfBirth = Some("1970-10-04"),
        address = CommonConnectorResponses.Address(lineOne = "26424 Cecelia Junction",
                                                   lineTwo = Some("Suite 858"),
                                                   lineThree = None,
                                                   lineFour = Some("West Siobhanberg"),
                                                   postalCode = "OX2 3HD",
                                                   countryCode = "AD"
        ),
        contactDetails = CommonConnectorResponses.ContactDetails(landline = Some("747663966"),
                                                                 mobile = Some("38390756243"),
                                                                 fax = Some("58371813020"),
                                                                 emailAddress = Some("Patrick.Dyson@example.com")
        )
      )

      val serviceResponse = converter.convert(connectorResponse)

      serviceResponse shouldBe ServiceResponses.Response(
        ids = Seq(
          RegistrationService.Responses.Id(RegistrationService.Responses.IdType.ARN, "WARN3849921"),
          RegistrationService.Responses.Id(RegistrationService.Responses.IdType.SAFE, "XE0000200775706"),
          RegistrationService.Responses.Id(RegistrationService.Responses.IdType.SAP, "1960629967"),
          RegistrationService.Responses.Id(RegistrationService.Responses.IdType.UNKNOWN, "25562288-ae0d-447a-863a-aac881b287a9")
        ),
        firstName = "Patrick",
        middleName = Some("John"),
        lastName = "Dyson",
        dateOfBirth = Some("1970-10-04"),
        address = CommonServiceResponses.Address(
          lineOne = "26424 Cecelia Junction",
          lineTwo = Some("Suite 858"),
          lineThree = None,
          lineFour = Some("West Siobhanberg"),
          postalCode = "OX2 3HD",
          countryCode = "AD"
        ),
        contactDetails = CommonServiceResponses.ContactDetails(landline = Some("747663966"),
                                                               mobile = Some("38390756243"),
                                                               fax = Some("58371813020"),
                                                               emailAddress = Some("Patrick.Dyson@example.com")
        )
      )
    }
  }
}
