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

import com.github.tomakehurst.wiremock.client.WireMock._
import connectors.{BaseConnector, RegistrationWithIdForIndividualConnector, RegistrationWithIdForOrganisationConnector}
import play.api.http.Status.{BAD_REQUEST, INTERNAL_SERVER_ERROR, OK, SERVICE_UNAVAILABLE}
import services.BaseService.Responses.Error
import services.RegistrationWithIdService.Common.{Responses => CommonResponses}
import services.RegistrationWithIdService.Individual.{Requests => IndividualRequests, Responses => IndividualResponses}
import services.RegistrationWithIdService.Organisation.{Requests => OrganisationRequests, Responses => OrganisationResponses}

class RegistrationWithIdServiceIntSpec extends BaseBackendConnectorIntSpec {

  val connectorPathForIndividual: String   = RegistrationWithIdForIndividualConnector.connectorPath
  val connectorPathForOrganisation: String = RegistrationWithIdForOrganisationConnector.connectorPath

  private lazy val registrationWithIdService = app.injector.instanceOf[RegistrationWithIdService]

  "attempting to register with an ID" - {
    "as an individual, when" - {
      "the response from the connector" - {
        "succeeds" in {
          stubFor(
            post(urlEqualTo(connectorPathForIndividual))
              .withRequestBody(equalToJson("""
                  |{
                  |  "id": {
                  |    "type": "NINO",
                  |    "value": "AA000000A"
                  |  },
                  |  "firstName": "Patrick",
                  |  "middleName": "John",
                  |  "lastName": "Dyson",
                  |  "dateOfBirth": "1970-10-04"
                  |}
                  |""".stripMargin))
              .willReturn(
                aResponse()
                  .withHeader("Content-Type", "application/json")
                  .withStatus(OK)
                  .withBody("""
                      |{
                      |  "ids": [
                      |    {
                      |      "type": "ARN",
                      |      "value": "WARN3849921"
                      |    },
                      |    {
                      |      "type": "SAFE",
                      |      "value": "XE0000200775706"
                      |    },
                      |    {
                      |      "type": "SAP",
                      |      "value": "1960629967"
                      |    }
                      |  ],
                      |  "firstName": "Patrick",
                      |  "middleName": "John",
                      |  "lastName": "Dyson",
                      |  "dateOfBirth": "1970-10-04",
                      |  "address": {
                      |    "lineOne": "26424 Cecelia Junction",
                      |    "lineTwo": "Suite 858",
                      |    "lineThree": "",
                      |    "lineFour": "West Siobhanberg",
                      |    "postalCode": "OX2 3HD",
                      |    "countryCode": "AD"
                      |  },
                      |  "contactDetails": {
                      |    "landline": "747663966",
                      |    "mobile": "38390756243",
                      |    "fax": "58371813020",
                      |    "emailAddress": "Patrick.Dyson@example.com"
                      |  }
                      |}
                      |""".stripMargin)
              )
          )

          val request = IndividualRequests.Request(
            id = IndividualRequests.Id(
              idType = IndividualRequests.IdType.NINO,
              value = "AA000000A"
            ),
            firstName = "Patrick",
            middleName = Some("John"),
            lastName = "Dyson",
            dateOfBirth = "1970-10-04"
          )

          val response = await(registrationWithIdService.asIndividual(request))

          response shouldBe Right(
            IndividualResponses.Response(
              ids = Seq(
                CommonResponses.Id(CommonResponses.IdType.ARN, "WARN3849921"),
                CommonResponses.Id(CommonResponses.IdType.SAFE, "XE0000200775706"),
                CommonResponses.Id(CommonResponses.IdType.SAP, "1960629967")
              ),
              firstName = "Patrick",
              middleName = Some("John"),
              lastName = "Dyson",
              dateOfBirth = Some("1970-10-04"),
              address = CommonResponses.Address(lineOne = "26424 Cecelia Junction",
                                                lineTwo = Some("Suite 858"),
                                                lineThree = Some(""),
                                                lineFour = Some("West Siobhanberg"),
                                                postalCode = "OX2 3HD",
                                                countryCode = "AD"
              ),
              contactDetails = CommonResponses.ContactDetails(landline = Some("747663966"),
                                                              mobile = Some("38390756243"),
                                                              fax = Some("58371813020"),
                                                              emailAddress = Some("Patrick.Dyson@example.com")
              )
            )
          )

          verifyThatDownstreamApiWasCalled()
        }
        "fails, where the response body is" - {
          "valid, with a status code of" - {
            "internal service error" in {
              stubFor(
                post(urlEqualTo(connectorPathForIndividual))
                  .withRequestBody(equalToJson("""
                      |{
                      |  "id": {
                      |    "type": "NINO",
                      |    "value": "AA000000A"
                      |  },
                      |  "firstName": "Patrick",
                      |  "middleName": "John",
                      |  "lastName": "Dyson",
                      |  "dateOfBirth": "1970-10-04"
                      |}
                      |""".stripMargin))
                  .willReturn(
                    aResponse()
                      .withHeader("Content-Type", "application/json")
                      .withStatus(INTERNAL_SERVER_ERROR)
                  )
              )

              val request = IndividualRequests.Request(
                id = IndividualRequests.Id(
                  idType = IndividualRequests.IdType.NINO,
                  value = "AA000000A"
                ),
                firstName = "Patrick",
                middleName = Some("John"),
                lastName = "Dyson",
                dateOfBirth = "1970-10-04"
              )

              val response = await(registrationWithIdService.asIndividual(request))

              response shouldBe Left(BaseService.Responses.Errors(INTERNAL_SERVER_ERROR))
              verifyThatDownstreamApiWasCalled()
            }

            "service unavailable" in {
              stubFor(
                post(urlEqualTo(connectorPathForIndividual))
                  .withRequestBody(equalToJson("""
                      |{
                      |  "id": {
                      |    "type": "NINO",
                      |    "value": "AA000000A"
                      |  },
                      |  "firstName": "Patrick",
                      |  "middleName": "John",
                      |  "lastName": "Dyson",
                      |  "dateOfBirth": "1970-10-04"
                      |}
                      |""".stripMargin))
                  .willReturn(
                    aResponse()
                      .withHeader("Content-Type", "application/json")
                      .withStatus(SERVICE_UNAVAILABLE)
                      .withBody("""
                          |[
                          |  {
                          |    "code": "eis-returned-service-unavailable"
                          |  }
                          |]
                          |""".stripMargin)
                  )
              )

              val request = IndividualRequests.Request(
                id = IndividualRequests.Id(
                  idType = IndividualRequests.IdType.NINO,
                  value = "AA000000A"
                ),
                firstName = "Patrick",
                middleName = Some("John"),
                lastName = "Dyson",
                dateOfBirth = "1970-10-04"
              )

              val response = await(registrationWithIdService.asIndividual(request))

              response shouldBe Left(
                BaseService.Responses.Errors(
                  SERVICE_UNAVAILABLE,
                  Seq(
                    Error("eis-returned-service-unavailable")
                  )
                )
              )
              verifyThatDownstreamApiWasCalled()
            }
            "bad request" in {
              stubFor(
                post(urlEqualTo(connectorPathForIndividual))
                  .withRequestBody(equalToJson("""
                      |{
                      |  "id": {
                      |    "type": "NINO",
                      |    "value": "AA000000A"
                      |  },
                      |  "firstName": "Patrick",
                      |  "middleName": "John",
                      |  "lastName": "",
                      |  "dateOfBirth": "10-04-1970"
                      |}
                      |""".stripMargin))
                  .willReturn(
                    aResponse()
                      .withHeader("Content-Type", "application/json")
                      .withStatus(BAD_REQUEST)
                      .withBody("""
                          |[
                          |  {
                          |    "code": "invalid-last-name"
                          |  },
                          |  {
                          |    "code": "invalid-date-of-birth"
                          |  }
                          |]
                          |""".stripMargin)
                  )
              )

              val request = IndividualRequests.Request(
                id = IndividualRequests.Id(
                  idType = IndividualRequests.IdType.NINO,
                  value = "AA000000A"
                ),
                firstName = "Patrick",
                middleName = Some("John"),
                lastName = "",
                dateOfBirth = "10-04-1970"
              )

              val response = await(registrationWithIdService.asIndividual(request))

              response shouldBe Left(
                BaseService.Responses.Errors(
                  BAD_REQUEST,
                  Seq(
                    Error("invalid-last-name"),
                    Error("invalid-date-of-birth")
                  )
                )
              )

              verifyThatDownstreamApiWasCalled()
            }
          }
          "invalid, with a status code of" - {
            "service unavailable" in {
              stubFor(
                post(urlEqualTo(connectorPathForIndividual))
                  .withRequestBody(equalToJson("""
                      |{
                      |  "id": {
                      |    "type": "NINO",
                      |    "value": "AA000000A"
                      |  },
                      |  "firstName": "Patrick",
                      |  "middleName": "John",
                      |  "lastName": "Dyson",
                      |  "dateOfBirth": "1970-10-04"
                      |}
                      |""".stripMargin))
                  .willReturn(
                    aResponse()
                      .withHeader("Content-Type", "application/json")
                      .withStatus(SERVICE_UNAVAILABLE)
                      .withBody("""
                          |[
                          |  {
                          |    "codes": "eis-returned-service-unavailable"
                          |  }
                          |]
                          |""".stripMargin)
                  )
              )

              val request = IndividualRequests.Request(
                id = IndividualRequests.Id(
                  idType = IndividualRequests.IdType.NINO,
                  value = "AA000000A"
                ),
                firstName = "Patrick",
                middleName = Some("John"),
                lastName = "Dyson",
                dateOfBirth = "1970-10-04"
              )

              assertThrows[BaseConnector.Exceptions.ResponseParsingException] {
                await(registrationWithIdService.asIndividual(request))
              }
              verifyThatDownstreamApiWasCalled()
            }
            "OK" in {
              stubFor(
                post(urlEqualTo(connectorPathForIndividual))
                  .withRequestBody(equalToJson("""
                      |{
                      |  "id": {
                      |    "type": "NINO",
                      |    "value": "AA000000A"
                      |  },
                      |  "firstName": "Patrick",
                      |  "middleName": "John",
                      |  "lastName": "Dyson",
                      |  "dateOfBirth": "1970-10-04"
                      |}
                      |""".stripMargin))
                  .willReturn(
                    aResponse()
                      .withHeader("Content-Type", "application/json")
                      .withStatus(OK)
                      .withBody("""
                          |{
                          |  "ids": [
                          |    {
                          |      "type": "ARN",
                          |      "value": "WARN3849921"
                          |    },
                          |    {
                          |      "type": "SAFE",
                          |      "value": "XE0000200775706"
                          |    },
                          |    {
                          |      "type": "SAP",
                          |      "value": "1960629967"
                          |    }
                          |  ],
                          |  "firstName": "Patrick",
                          |  "middleName": "John",
                          |  "lastName": "Dyson",
                          |  "dateOfBirth": "1970-10-04"
                          |}
                          |""".stripMargin)
                  )
              )

              val request = IndividualRequests.Request(
                id = IndividualRequests.Id(
                  idType = IndividualRequests.IdType.NINO,
                  value = "AA000000A"
                ),
                firstName = "Patrick",
                middleName = Some("John"),
                lastName = "Dyson",
                dateOfBirth = "1970-10-04"
              )

              assertThrows[BaseConnector.Exceptions.ResponseParsingException] {
                await(registrationWithIdService.asIndividual(request))
              }

              verifyThatDownstreamApiWasCalled()
            }
          }
        }
      }
    }
    "as an organisation, when" - {
      "the response from the connector" - {
        "succeeds" in {
          stubFor(
            post(urlEqualTo(connectorPathForOrganisation))
              .withRequestBody(equalToJson("""
                                             |{
                                             |  "id": {
                                             |    "type": "UTR",
                                             |    "value": "1234567890"
                                             |  },
                                             |  "name": "Dyson",
                                             |  "type": "CorporateBody"
                                             |}
                                             |""".stripMargin))
              .willReturn(
                aResponse()
                  .withHeader("Content-Type", "application/json")
                  .withStatus(OK)
                  .withBody("""
                          {
                              |  "name": "Dyson",
                              |  "type": "CorporateBody",
                              |  "ids": [
                              |    {
                              |      "type": "ARN",
                              |      "value": "WARN1442450"
                              |    },
                              |    {
                              |      "type": "SAFE",
                              |      "value": "XE0000586571722"
                              |    },
                              |    {
                              |      "type": "SAP",
                              |      "value": "8231791429"
                              |    }
                              |  ],
                              |  "address": {
                              |    "lineOne": "2627 Gus Hill",
                              |    "lineTwo": "Apt. 898",
                              |    "lineThree": "",
                              |    "lineFour": "West Corrinamouth",
                              |    "postalCode": "OX2 3HD",
                              |    "countryCode": "AD"
                              |  },
                              |  "contactDetails": {
                              |    "landline": "176905117",
                              |    "mobile": "62281724761",
                              |    "fax": "08959633679",
                              |    "emailAddress": "edward.goodenough@example.com"
                              |  }
                              |}
                              |""".stripMargin)
              )
          )

          val request = OrganisationRequests.Request(
            id = OrganisationRequests.Id(
              idType = OrganisationRequests.IdType.UTR,
              value = "1234567890"
            ),
            name = "Dyson",
            _type = OrganisationRequests.Type.CorporateBody
          )

          val response = await(registrationWithIdService.asOrganisation(request))

          response shouldBe Right(
            OrganisationResponses.Response(
              ids = Seq(
                CommonResponses.Id(CommonResponses.IdType.ARN, "WARN1442450"),
                CommonResponses.Id(CommonResponses.IdType.SAFE, "XE0000586571722"),
                CommonResponses.Id(CommonResponses.IdType.SAP, "8231791429")
              ),
              name = "Dyson",
              _type = OrganisationResponses.Type.CorporateBody,
              address = CommonResponses.Address(lineOne = "2627 Gus Hill",
                                                lineTwo = Some("Apt. 898"),
                                                lineThree = Some(""),
                                                lineFour = Some("West Corrinamouth"),
                                                postalCode = "OX2 3HD",
                                                countryCode = "AD"
              ),
              contactDetails = CommonResponses.ContactDetails(landline = Some("176905117"),
                                                              mobile = Some("62281724761"),
                                                              fax = Some("08959633679"),
                                                              emailAddress = Some("edward.goodenough@example.com")
              )
            )
          )

          verifyThatDownstreamApiWasCalled()
        }
        "fails, where the response body is" - {
          "valid, with a status code of" - {
            "internal service error" in {
              stubFor(
                post(urlEqualTo(connectorPathForOrganisation))
                  .withRequestBody(equalToJson("""
                                                 |{
                                                 |  "id": {
                                                 |    "type": "UTR",
                                                 |    "value": "1234567890"
                                                 |  },
                                                 |  "name": "Dyson",
                                                 |  "type": "CorporateBody"
                                                 |}
                                                 |""".stripMargin))
                  .willReturn(
                    aResponse()
                      .withHeader("Content-Type", "application/json")
                      .withStatus(INTERNAL_SERVER_ERROR)
                  )
              )

              val request = OrganisationRequests.Request(
                id = OrganisationRequests.Id(
                  idType = OrganisationRequests.IdType.UTR,
                  value = "1234567890"
                ),
                name = "Dyson",
                _type = OrganisationRequests.Type.CorporateBody
              )

              val response = await(registrationWithIdService.asOrganisation(request))

              response shouldBe Left(BaseService.Responses.Errors(INTERNAL_SERVER_ERROR))
              verifyThatDownstreamApiWasCalled()
            }
            "service unavailable" in {
              stubFor(
                post(urlEqualTo(connectorPathForOrganisation))
                  .withRequestBody(equalToJson("""
                                                 |{
                                                 |  "id": {
                                                 |    "type": "UTR",
                                                 |    "value": "1234567890"
                                                 |  },
                                                 |  "name": "Dyson",
                                                 |  "type": "CorporateBody"
                                                 |}
                                                 |""".stripMargin))
                  .willReturn(
                    aResponse()
                      .withHeader("Content-Type", "application/json")
                      .withStatus(SERVICE_UNAVAILABLE)
                      .withBody("""
                                  |[
                                  |  {
                                  |    "code": "eis-returned-service-unavailable"
                                  |  }
                                  |]
                                  |""".stripMargin)
                  )
              )

              val request = OrganisationRequests.Request(
                id = OrganisationRequests.Id(
                  idType = OrganisationRequests.IdType.UTR,
                  value = "1234567890"
                ),
                name = "Dyson",
                _type = OrganisationRequests.Type.CorporateBody
              )

              val response = await(registrationWithIdService.asOrganisation(request))

              response shouldBe Left(
                BaseService.Responses.Errors(
                  SERVICE_UNAVAILABLE,
                  Seq(
                    Error("eis-returned-service-unavailable")
                  )
                )
              )
              verifyThatDownstreamApiWasCalled()
            }
            "bad request" in {
              stubFor(
                post(urlEqualTo(connectorPathForOrganisation))
                  .withRequestBody(equalToJson("""
                                                 |{
                                                 |  "id": {
                                                 |    "type": "UTR",
                                                 |    "value": "1234567890"
                                                 |  },
                                                 |  "name": "Dyson",
                                                 |  "type": "CorporateBody"
                                                 |}
                                                 |""".stripMargin))
                  .willReturn(
                    aResponse()
                      .withHeader("Content-Type", "application/json")
                      .withStatus(BAD_REQUEST)
                      .withBody("""
                                  |[
                                  |  {
                                  |    "code": "invalid-name"
                                  |  },
                                  |  {
                                  |    "code": "invalid-type"
                                  |  }
                                  |]
                                  |""".stripMargin)
                  )
              )

              val request = OrganisationRequests.Request(
                id = OrganisationRequests.Id(
                  idType = OrganisationRequests.IdType.UTR,
                  value = "1234567890"
                ),
                name = "Dyson",
                _type = OrganisationRequests.Type.CorporateBody
              )

              val response = await(registrationWithIdService.asOrganisation(request))

              response shouldBe Left(
                BaseService.Responses.Errors(
                  BAD_REQUEST,
                  Seq(
                    Error("invalid-name"),
                    Error("invalid-type")
                  )
                )
              )
              verifyThatDownstreamApiWasCalled()
            }
          }
          "invalid, with a status code of" - {
            "service unavailable" in {
              stubFor(
                post(urlEqualTo(connectorPathForOrganisation))
                  .withRequestBody(equalToJson("""
                                                 |{
                                                 |  "id": {
                                                 |    "type": "UTR",
                                                 |    "value": "1234567890"
                                                 |  },
                                                 |  "name": "Dyson",
                                                 |  "type": "CorporateBody"
                                                 |}
                                                 |""".stripMargin))
                  .willReturn(
                    aResponse()
                      .withHeader("Content-Type", "application/json")
                      .withStatus(SERVICE_UNAVAILABLE)
                      .withBody("""
                                  |[
                                  |  {
                                  |    "codes": "eis-returned-service-unavailable"
                                  |  }
                                  |]
                                  |""".stripMargin)
                  )
              )

              val request = OrganisationRequests.Request(
                id = OrganisationRequests.Id(
                  idType = OrganisationRequests.IdType.UTR,
                  value = "1234567890"
                ),
                name = "Dyson",
                _type = OrganisationRequests.Type.CorporateBody
              )

              assertThrows[BaseConnector.Exceptions.ResponseParsingException] {
                await(registrationWithIdService.asOrganisation(request))
              }
              verifyThatDownstreamApiWasCalled()
            }
            "OK" in {
              stubFor(
                post(urlEqualTo(connectorPathForOrganisation))
                  .withRequestBody(equalToJson("""
                                                 |{
                                                 |  "id": {
                                                 |    "type": "UTR",
                                                 |    "value": "1234567890"
                                                 |  },
                                                 |  "name": "Dyson",
                                                 |  "type": "CorporateBody"
                                                 |}
                                                 |""".stripMargin))
                  .willReturn(
                    aResponse()
                      .withHeader("Content-Type", "application/json")
                      .withStatus(OK)
                      .withBody("""
                          {
                                  |  "type": "CorporateBody",
                                  |  "ids": [
                                  |    {
                                  |      "type": "ARN",
                                  |      "value": "WARN1442450"
                                  |    },
                                  |    {
                                  |      "type": "SAFE",
                                  |      "value": "XE0000586571722"
                                  |    },
                                  |    {
                                  |      "type": "SAP",
                                  |      "value": "8231791429"
                                  |    }
                                  |  ],
                                  |  "address": {
                                  |    "lineOne": "2627 Gus Hill",
                                  |    "lineTwo": "Apt. 898",
                                  |    "lineThree": "",
                                  |    "lineFour": "West Corrinamouth",
                                  |    "postalCode": "OX2 3HD",
                                  |    "countryCode": "AD"
                                  |  },
                                  |  "contactDetails": {
                                  |    "landline": "176905117",
                                  |    "mobile": "62281724761",
                                  |    "fax": "08959633679",
                                  |    "emailAddress": "edward.goodenough@example.com"
                                  |  }
                                  |}
                                  |""".stripMargin)
                  )
              )

              val request = OrganisationRequests.Request(
                id = OrganisationRequests.Id(
                  idType = OrganisationRequests.IdType.UTR,
                  value = "1234567890"
                ),
                name = "Dyson",
                _type = OrganisationRequests.Type.CorporateBody
              )

              assertThrows[BaseConnector.Exceptions.ResponseParsingException] {
                await(registrationWithIdService.asOrganisation(request))
              }
              verifyThatDownstreamApiWasCalled()
            }
          }
        }
      }
    }
  }
}
