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
import connectors.registration.withId.{RegistrationWithIdConnector, RegistrationWithIdForOrganisationConnector}
import services.registration.withId.RegistrationWithIdForOrganisationService
import services.registration.BaseRegistrationService.{Responses => CommonRegistrationResponses}
import services.registration.withId.BaseRegistrationWithIdService.{Responses => CommonRegistrationWithIdResponses}

class RegistrationWithIdForOrganisationConverterSpec extends BaseSpec {

  private val converter = new RegistrationWithIdForOrganisationConverter

  "when registering as an organisation, the converter returns the expected value, for a" - {
    "service request, for every" - {
      "id type" in {
        val idTypes =
          Table(
            ("ID Type (Raw)", "Expected  ID Type"),
            ("EORI", "EORI"),
            ("UTR", "UTR")
          )
        forAll(idTypes) { (rawIdType, expectedIdType) =>
          val idType = RegistrationWithIdForOrganisationService.Requests.IdType.all.find(_.toString == rawIdType).get
          val serviceRequest =
            RegistrationWithIdForOrganisationService.Requests.Request(
              id = RegistrationWithIdForOrganisationService.Requests.Id(idType, value = "1234567890"),
              name = "Dyson",
              _type = RegistrationWithIdForOrganisationService.Requests.Type.CorporateBody
            )

          val connectorRequest = converter.convertServiceRequest(serviceRequest)

          connectorRequest shouldBe RegistrationWithIdForOrganisationConnector.Requests.Request(id = RegistrationWithIdConnector.Requests.Id(expectedIdType,
                                                                                                                                             "1234567890"
                                                                                                ),
                                                                                                name = "Dyson",
                                                                                                _type = "CorporateBody"
          )
        }
      }
      "type" in {
        val types =
          Table(
            ("Type (Raw)", "Expected Type"),
            ("NotSpecified", "NotSpecified"),
            ("Partnership", "Partnership"),
            ("LimitedLiabilityPartnership", "LimitedLiabilityPartnership"),
            ("CorporateBody", "CorporateBody"),
            ("UnincorporatedBody", "UnincorporatedBody")
          )
        forAll(types) { (rawType, expectedType) =>
          val _type = RegistrationWithIdForOrganisationService.Requests.Type.all.find(_.toString == rawType).get
          val serviceRequest =
            RegistrationWithIdForOrganisationService.Requests.Request(
              id = RegistrationWithIdForOrganisationService.Requests.Id(RegistrationWithIdForOrganisationService.Requests.IdType.UTR, value = "1234567890"),
              name = "Dyson",
              _type = _type
            )

          val connectorRequest = converter.convertServiceRequest(serviceRequest)

          connectorRequest shouldBe RegistrationWithIdForOrganisationConnector.Requests.Request(id =
                                                                                                  RegistrationWithIdConnector.Requests.Id("UTR", "1234567890"),
                                                                                                name = "Dyson",
                                                                                                _type = expectedType
          )
        }
      }

    }
    "connector response, for various" - {
      "types" in {
        val types =
          Table(
            ("Type", "Expected Type (Raw)"),
            ("NotSpecified", "NotSpecified"),
            ("Partnership", "Partnership"),
            ("LimitedLiabilityPartnership", "LimitedLiabilityPartnership"),
            ("CorporateBody", "CorporateBody"),
            ("UnincorporatedBody", "UnincorporatedBody"),
            ("Charity", "Unknown"),
            ("SoleTrader", "Unknown")
          )

        forAll(types) { (rawType, expectedRawType) =>
          val expectedType = RegistrationWithIdForOrganisationService.Responses.Type.all.find(_.toString == expectedRawType).get
          val connectorResponse = RegistrationWithIdForOrganisationConnector.Responses.Response(
            ids = Seq(
              RegistrationConnector.Responses.Id("ARN", "WARN1442450"),
              RegistrationConnector.Responses.Id("SAFE", "XE0000586571722"),
              RegistrationConnector.Responses.Id("SAP", "8231791429"),
              RegistrationConnector.Responses.Id("CAT", "7c9c26fa-2471-4e23-9a4c-1fe7da7b9582")
            ),
            name = "Dyson",
            _type = rawType,
            address = RegistrationWithIdConnector.Responses.Address(lineOne = "2627 Gus Hill",
                                                                    lineTwo = Some("Apt. 898"),
                                                                    lineThree = None,
                                                                    lineFour = Some("West Corrinamouth"),
                                                                    postalCode = "OX2 3HD",
                                                                    countryCode = "AD"
            ),
            contactDetails = RegistrationWithIdConnector.Responses.ContactDetails(landline = Some("176905117"),
                                                                                  mobile = Some("62281724761"),
                                                                                  fax = Some("08959633679"),
                                                                                  emailAddress = Some("edward.goodenough@example.com")
            )
          )

          val serviceResponse = converter.convertSuccessfulConnectorResponse(connectorResponse)

          serviceResponse shouldBe RegistrationWithIdForOrganisationService.Responses.Response(
            ids = Seq(
              CommonRegistrationResponses.Id(CommonRegistrationResponses.IdType.ARN, "WARN1442450"),
              CommonRegistrationResponses.Id(CommonRegistrationResponses.IdType.SAFE, "XE0000586571722"),
              CommonRegistrationResponses.Id(CommonRegistrationResponses.IdType.SAP, "8231791429"),
              CommonRegistrationResponses.Id(CommonRegistrationResponses.IdType.UNKNOWN, "7c9c26fa-2471-4e23-9a4c-1fe7da7b9582")
            ),
            name = "Dyson",
            _type = expectedType,
            address = CommonRegistrationWithIdResponses.Address(lineOne = "2627 Gus Hill",
                                                                lineTwo = Some("Apt. 898"),
                                                                lineThree = None,
                                                                lineFour = Some("West Corrinamouth"),
                                                                postalCode = "OX2 3HD",
                                                                countryCode = "AD"
            ),
            contactDetails = CommonRegistrationWithIdResponses.ContactDetails(landline = Some("176905117"),
                                                                              mobile = Some("62281724761"),
                                                                              fax = Some("08959633679"),
                                                                              emailAddress = Some("edward.goodenough@example.com")
            )
          )

        }
      }
    }
  }
}
