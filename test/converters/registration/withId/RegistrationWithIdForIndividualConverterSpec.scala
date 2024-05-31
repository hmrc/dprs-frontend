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

package converters.registration.withId

import base.BaseSpec
import connectors.registration.RegistrationConnector
import connectors.registration.withId.{RegistrationWithIdConnector, RegistrationWithIdForIndividualConnector}
import services.registration.withId.RegistrationWithIdForIndividualService

class RegistrationWithIdForIndividualConverterSpec extends BaseSpec {

  private val converter = new RegistrationWithIdForIndividualConverter

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
        val idType = RegistrationWithIdForIndividualService.Requests.IdType.all.find(_.toString == rawType).get
        val serviceRequest =
          RegistrationWithIdForIndividualService.Requests.Request(
            id = RegistrationWithIdForIndividualService.Requests.Id(idType, "AA000000A"),
            firstName = "Patrick",
            middleName = Some("John"),
            lastName = "Dyson",
            dateOfBirth = "1970-10-04"
          )

        val connectorRequest = converter.convertServiceRequest(serviceRequest)

        connectorRequest shouldBe RegistrationWithIdForIndividualConnector.Requests.Request(
          id = RegistrationWithIdConnector.Requests.Id(expectedType, "AA000000A"),
          firstName = "Patrick",
          middleName = Some("John"),
          lastName = "Dyson",
          dateOfBirth = "1970-10-04"
        )
      }
    }
    "connector response" in {
      val connectorResponse = RegistrationWithIdForIndividualConnector.Responses.Response(
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
        address = RegistrationWithIdConnector.Responses.Address(lineOne = "26424 Cecelia Junction",
                                                                lineTwo = Some("Suite 858"),
                                                                lineThree = None,
                                                                lineFour = Some("West Siobhanberg"),
                                                                postalCode = "OX2 3HD",
                                                                countryCode = "AD"
        ),
        contactDetails = RegistrationWithIdConnector.Responses.ContactDetails(landline = Some("747663966"),
                                                                              mobile = Some("38390756243"),
                                                                              fax = Some("58371813020"),
                                                                              emailAddress = Some("Patrick.Dyson@example.com")
        )
      )

      val serviceResponse = converter.convertSuccessfulConnectorResponse(Some(connectorResponse))

      serviceResponse shouldBe Some(
        RegistrationWithIdForIndividualService.Responses.Response(
          ids = Seq(
            services.registration.BaseRegistrationService.Responses.Id(services.registration.BaseRegistrationService.Responses.IdType.ARN, "WARN3849921"),
            services.registration.BaseRegistrationService.Responses.Id(services.registration.BaseRegistrationService.Responses.IdType.SAFE, "XE0000200775706"),
            services.registration.BaseRegistrationService.Responses.Id(services.registration.BaseRegistrationService.Responses.IdType.SAP, "1960629967"),
            services.registration.BaseRegistrationService.Responses.Id(services.registration.BaseRegistrationService.Responses.IdType.UNKNOWN,
                                                                       "25562288-ae0d-447a-863a-aac881b287a9"
            )
          ),
          firstName = "Patrick",
          middleName = Some("John"),
          lastName = "Dyson",
          dateOfBirth = Some("1970-10-04"),
          address = services.registration.withId.BaseRegistrationWithIdService.Responses.Address(
            lineOne = "26424 Cecelia Junction",
            lineTwo = Some("Suite 858"),
            lineThree = None,
            lineFour = Some("West Siobhanberg"),
            postalCode = "OX2 3HD",
            countryCode = "AD"
          ),
          contactDetails = services.registration.withId.BaseRegistrationWithIdService.Responses.ContactDetails(landline = Some("747663966"),
                                                                                                               mobile = Some("38390756243"),
                                                                                                               fax = Some("58371813020"),
                                                                                                               emailAddress = Some("Patrick.Dyson@example.com")
          )
        )
      )
    }
  }
}
