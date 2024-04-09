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

import base.BaseSpec

class RegistrationWithIdServiceSpec extends BaseSpec {

  "when registering as an" - {
    "individual" - {
      import connectors.RegistrationWithIdConnector.{Requests => CommonConnectorRequests, Responses => CommonConnectorResponses}
      import connectors.RegistrationWithIdForIndividualConnector.{Requests => ConnectorRequests, Responses => ConnectorResponses}
      import services.RegistrationWithIdService.Individual.{Requests => ServiceRequests, Responses => ServiceResponses}
      import services.RegistrationWithIdService.Common.{Responses => CommonServiceResponses}
      "converter returns the expected value, for a " - {
        val converter = new RegistrationWithIdService.Individual.Converter
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
              CommonConnectorResponses.Id("ARN", "WARN3849921"),
              CommonConnectorResponses.Id("SAFE", "XE0000200775706"),
              CommonConnectorResponses.Id("SAP", "1960629967"),
              CommonConnectorResponses.Id("CAT", "25562288-ae0d-447a-863a-aac881b287a9")
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
              CommonServiceResponses.Id(CommonServiceResponses.IdType.ARN, "WARN3849921"),
              CommonServiceResponses.Id(CommonServiceResponses.IdType.SAFE, "XE0000200775706"),
              CommonServiceResponses.Id(CommonServiceResponses.IdType.SAP, "1960629967"),
              CommonServiceResponses.Id(CommonServiceResponses.IdType.UNKNOWN, "25562288-ae0d-447a-863a-aac881b287a9")
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
    "organisation" - {
      import connectors.RegistrationWithIdConnector.{Requests => CommonConnectorRequests, Responses => CommonConnectorResponses}
      import connectors.RegistrationWithIdForOrganisationConnector.{Requests => ConnectorRequests, Responses => ConnectorResponses}
      import services.RegistrationWithIdService.Organisation.{Requests => ServiceRequests, Responses => ServiceResponses}
      import services.RegistrationWithIdService.Common.{Responses => CommonServiceResponses}
      "converter returns the expected value, for a" - {
        val converter = new RegistrationWithIdService.Organisation.Converter
        "service request, for every" - {
          "id type" in {
            val idTypes =
              Table(
                ("ID Type (Raw)", "Expected  ID Type"),
                ("EORI", "EORI"),
                ("UTR", "UTR")
              )
            forAll(idTypes) { (rawIdType, expectedIdType) =>
              val idType = ServiceRequests.IdType.all.find(_.toString == rawIdType).get
              val serviceRequest =
                ServiceRequests.Request(id = ServiceRequests.Id(idType, value = "1234567890"), name = "Dyson", _type = ServiceRequests.Type.CorporateBody)

              val connectorRequest = converter.convert(serviceRequest)

              connectorRequest shouldBe ConnectorRequests.Request(id = CommonConnectorRequests.Id(expectedIdType, "1234567890"),
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
              val _type = ServiceRequests.Type.all.find(_.toString == rawType).get
              val serviceRequest =
                ServiceRequests.Request(id = ServiceRequests.Id(ServiceRequests.IdType.UTR, value = "1234567890"), name = "Dyson", _type = _type)

              val connectorRequest = converter.convert(serviceRequest)

              connectorRequest shouldBe ConnectorRequests.Request(id = CommonConnectorRequests.Id("UTR", "1234567890"), name = "Dyson", _type = expectedType)
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
              val expectedType = ServiceResponses.Type.all.find(_.toString == expectedRawType).get
              val connectorResponse = ConnectorResponses.Response(
                ids = Seq(
                  CommonConnectorResponses.Id("ARN", "WARN1442450"),
                  CommonConnectorResponses.Id("SAFE", "XE0000586571722"),
                  CommonConnectorResponses.Id("SAP", "8231791429"),
                  CommonConnectorResponses.Id("CAT", "7c9c26fa-2471-4e23-9a4c-1fe7da7b9582")
                ),
                name = "Dyson",
                _type = rawType,
                address = CommonConnectorResponses.Address(lineOne = "2627 Gus Hill",
                                                           lineTwo = Some("Apt. 898"),
                                                           lineThree = None,
                                                           lineFour = Some("West Corrinamouth"),
                                                           postalCode = "OX2 3HD",
                                                           countryCode = "AD"
                ),
                contactDetails = CommonConnectorResponses.ContactDetails(landline = Some("176905117"),
                                                                         mobile = Some("62281724761"),
                                                                         fax = Some("08959633679"),
                                                                         emailAddress = Some("edward.goodenough@example.com")
                )
              )

              val serviceResponse = converter.convert(connectorResponse)

              serviceResponse shouldBe ServiceResponses.Response(
                ids = Seq(
                  CommonServiceResponses.Id(CommonServiceResponses.IdType.ARN, "WARN1442450"),
                  CommonServiceResponses.Id(CommonServiceResponses.IdType.SAFE, "XE0000586571722"),
                  CommonServiceResponses.Id(CommonServiceResponses.IdType.SAP, "8231791429"),
                  CommonServiceResponses.Id(CommonServiceResponses.IdType.UNKNOWN, "7c9c26fa-2471-4e23-9a4c-1fe7da7b9582")
                ),
                name = "Dyson",
                _type = expectedType,
                address = CommonServiceResponses.Address(lineOne = "2627 Gus Hill",
                                                         lineTwo = Some("Apt. 898"),
                                                         lineThree = None,
                                                         lineFour = Some("West Corrinamouth"),
                                                         postalCode = "OX2 3HD",
                                                         countryCode = "AD"
                ),
                contactDetails = CommonServiceResponses.ContactDetails(landline = Some("176905117"),
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
  }
}
