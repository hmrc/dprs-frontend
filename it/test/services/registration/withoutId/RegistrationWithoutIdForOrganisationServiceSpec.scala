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

import com.github.tomakehurst.wiremock.client.WireMock._
import connectors.BaseConnector
import connectors.registration.withoutId.RegistrationWithoutIdForOrganisationConnector.connectorPath
import play.api.http.Status._
import services.BaseBackendConnectorSpec
import services.BaseService.{Responses => CommonResponses}
import services.registration.BaseRegistrationService.{Responses => CommonRegistrationResponses}
import services.registration.withoutId.BaseRegistrationWithoutIdService.{Requests => CommonRegistrationWithoutIdRequests}
import services.registration.withoutId.RegistrationWithoutIdForOrganisationService.Requests.Request

class RegistrationWithoutIdForOrganisationServiceSpec extends BaseBackendConnectorSpec {

  private lazy val service = app.injector.instanceOf[RegistrationWithoutIdForOrganisationService]

  "attempting to register without an ID, as an organisation, when" - {
    "the response from the connector" - {
      "succeeds" in {
        stubFor(
          post(urlEqualTo(connectorPath))
            .withRequestBody(equalToJson("""
                                           |{
                                           |    "name": "Dyson",
                                           |    "address": {
                                           |        "lineOne": "34 Park Lane",
                                           |        "lineTwo": "Building A",
                                           |        "lineThree": "Suite 100",
                                           |        "lineFour": "Manchester",
                                           |        "postalCode": "M54 1MQ",
                                           |        "countryCode": "GB"
                                           |    },
                                           |    "contactDetails": {
                                           |        "landline": "747663966",
                                           |        "mobile": "38390756243",
                                           |        "fax": "58371813020",
                                           |        "emailAddress": "dyson@example.com"
                                           |    }
                                           |}
                                           |""".stripMargin))
            .willReturn(
              aResponse()
                .withHeader("Content-Type", "application/json")
                .withStatus(OK)
                .withBody("""
                            |{
                            |    "ids": [
                            |        {
                            |            "type": "ARN",
                            |            "value": "WARN3849921"
                            |        },
                            |        {
                            |            "type": "SAFE",
                            |            "value": "XE0000200775706"
                            |        },
                            |        {
                            |            "type": "SAP",
                            |            "value": "1960629967"
                            |        }
                            |    ]
                            |}
                            |""".stripMargin)
            )
        )

        val request = Request(
          name = "Dyson",
          address = CommonRegistrationWithoutIdRequests.Address(lineOne = "34 Park Lane",
                                                                lineTwo = Some("Building A"),
                                                                lineThree = Some("Suite 100"),
                                                                lineFour = Some("Manchester"),
                                                                postalCode = "M54 1MQ",
                                                                countryCode = "GB"
          ),
          contactDetails = CommonRegistrationWithoutIdRequests.ContactDetails(landline = Some("747663966"),
                                                                              mobile = Some("38390756243"),
                                                                              fax = Some("58371813020"),
                                                                              emailAddress = Some("dyson@example.com")
          )
        )

        val response = await(service.call(request))

        response shouldBe Right(
          CommonRegistrationResponses.Response(ids =
            Seq(
              CommonRegistrationResponses.Id(CommonRegistrationResponses.IdType.ARN, "WARN3849921"),
              CommonRegistrationResponses.Id(CommonRegistrationResponses.IdType.SAFE, "XE0000200775706"),
              CommonRegistrationResponses.Id(CommonRegistrationResponses.IdType.SAP, "1960629967")
            )
          )
        )
        verifyThatDownstreamApiWasCalled()
      }
      "fails, where the response body is" - {
        "absent, with a status code of" - {
          "internal service error" in {
            stubFor(
              post(urlEqualTo(connectorPath))
                .withRequestBody(equalToJson("""
                                               |{
                                               |    "name": "Dyson",
                                               |    "address": {
                                               |        "lineOne": "34 Park Lane",
                                               |        "lineTwo": "Building A",
                                               |        "lineThree": "Suite 100",
                                               |        "lineFour": "Manchester",
                                               |        "postalCode": "M54 1MQ",
                                               |        "countryCode": "GB"
                                               |    },
                                               |    "contactDetails": {
                                               |        "landline": "747663966",
                                               |        "mobile": "38390756243",
                                               |        "fax": "58371813020",
                                               |        "emailAddress": "dyson@example.com"
                                               |    }
                                               |}
                                               |""".stripMargin))
                .willReturn(
                  aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withStatus(INTERNAL_SERVER_ERROR)
                )
            )

            val request = Request(
              name = "Dyson",
              address = CommonRegistrationWithoutIdRequests.Address(lineOne = "34 Park Lane",
                                                                    lineTwo = Some("Building A"),
                                                                    lineThree = Some("Suite 100"),
                                                                    lineFour = Some("Manchester"),
                                                                    postalCode = "M54 1MQ",
                                                                    countryCode = "GB"
              ),
              contactDetails = CommonRegistrationWithoutIdRequests.ContactDetails(landline = Some("747663966"),
                                                                                  mobile = Some("38390756243"),
                                                                                  fax = Some("58371813020"),
                                                                                  emailAddress = Some("dyson@example.com")
              )
            )

            val response = await(service.call(request))

            response shouldBe Left(CommonResponses.Errors(INTERNAL_SERVER_ERROR))
            verifyThatDownstreamApiWasCalled()
          }
        }
        "valid, with a status code of" - {
          "service unavailable" in {
            stubFor(
              post(urlEqualTo(connectorPath))
                .withRequestBody(equalToJson("""
                                               |{
                                               |    "name": "Dyson",
                                               |    "address": {
                                               |        "lineOne": "34 Park Lane",
                                               |        "lineTwo": "Building A",
                                               |        "lineThree": "Suite 100",
                                               |        "lineFour": "Manchester",
                                               |        "postalCode": "M54 1MQ",
                                               |        "countryCode": "GB"
                                               |    },
                                               |    "contactDetails": {
                                               |        "landline": "747663966",
                                               |        "mobile": "38390756243",
                                               |        "fax": "58371813020",
                                               |        "emailAddress": "dyson@example.com"
                                               |    }
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

            val request = Request(
              name = "Dyson",
              address = CommonRegistrationWithoutIdRequests.Address(lineOne = "34 Park Lane",
                                                                    lineTwo = Some("Building A"),
                                                                    lineThree = Some("Suite 100"),
                                                                    lineFour = Some("Manchester"),
                                                                    postalCode = "M54 1MQ",
                                                                    countryCode = "GB"
              ),
              contactDetails = CommonRegistrationWithoutIdRequests.ContactDetails(landline = Some("747663966"),
                                                                                  mobile = Some("38390756243"),
                                                                                  fax = Some("58371813020"),
                                                                                  emailAddress = Some("dyson@example.com")
              )
            )

            val response = await(service.call(request))

            response shouldBe Left(
              CommonResponses.Errors(
                SERVICE_UNAVAILABLE,
                Seq(
                  CommonResponses.Error("eis-returned-service-unavailable")
                )
              )
            )
            verifyThatDownstreamApiWasCalled()
          }
          "bad request" in {
            stubFor(
              post(urlEqualTo(connectorPath))
                .withRequestBody(equalToJson("""
                                               |{
                                               |    "name": "",
                                               |    "address": {
                                               |        "lineOne": "",
                                               |        "lineTwo": "Building A",
                                               |        "lineThree": "Suite 100",
                                               |        "lineFour": "Manchester",
                                               |        "postalCode": "M54 1MQ",
                                               |        "countryCode": "GB"
                                               |    },
                                               |    "contactDetails": {
                                               |        "landline": "747663966",
                                               |        "mobile": "38390756243",
                                               |        "fax": "58371813020",
                                               |        "emailAddress": "dyson@example.com"
                                               |    }
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
                                |    "code": "invalid-address-line-one"
                                |  }
                                |]
                                |""".stripMargin)
                )
            )

            val request = Request(
              name = "",
              address = CommonRegistrationWithoutIdRequests.Address(lineOne = "",
                                                                    lineTwo = Some("Building A"),
                                                                    lineThree = Some("Suite 100"),
                                                                    lineFour = Some("Manchester"),
                                                                    postalCode = "M54 1MQ",
                                                                    countryCode = "GB"
              ),
              contactDetails = CommonRegistrationWithoutIdRequests.ContactDetails(landline = Some("747663966"),
                                                                                  mobile = Some("38390756243"),
                                                                                  fax = Some("58371813020"),
                                                                                  emailAddress = Some("dyson@example.com")
              )
            )

            val response = await(service.call(request))

            response shouldBe Left(
              CommonResponses.Errors(
                BAD_REQUEST,
                Seq(
                  CommonResponses.Error("invalid-name"),
                  CommonResponses.Error("invalid-address-line-one")
                )
              )
            )
            verifyThatDownstreamApiWasCalled()
          }
          "conflict" in {
            stubFor(
              post(urlEqualTo(connectorPath))
                .withRequestBody(equalToJson("""
                                               |{
                                               |    "name": "",
                                               |    "address": {
                                               |        "lineOne": "",
                                               |        "lineTwo": "Building A",
                                               |        "lineThree": "Suite 100",
                                               |        "lineFour": "Manchester",
                                               |        "postalCode": "M54 1MQ",
                                               |        "countryCode": "GB"
                                               |    },
                                               |    "contactDetails": {
                                               |        "landline": "747663966",
                                               |        "mobile": "38390756243",
                                               |        "fax": "58371813020",
                                               |        "emailAddress": "dyson@example.com"
                                               |    }
                                               |}
                                               |""".stripMargin))
                .willReturn(
                  aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withStatus(CONFLICT)
                    .withBody("""
                                |[
                                |  {
                                |    "code": "eis-returned-conflict"
                                |  }
                                |]
                                |""".stripMargin)
                )
            )

            val request = Request(
              name = "",
              address = CommonRegistrationWithoutIdRequests.Address(lineOne = "",
                                                                    lineTwo = Some("Building A"),
                                                                    lineThree = Some("Suite 100"),
                                                                    lineFour = Some("Manchester"),
                                                                    postalCode = "M54 1MQ",
                                                                    countryCode = "GB"
              ),
              contactDetails = CommonRegistrationWithoutIdRequests.ContactDetails(landline = Some("747663966"),
                                                                                  mobile = Some("38390756243"),
                                                                                  fax = Some("58371813020"),
                                                                                  emailAddress = Some("dyson@example.com")
              )
            )

            val response = await(service.call(request))

            response shouldBe Left(
              CommonResponses.Errors(
                CONFLICT,
                Seq(
                  CommonResponses.Error("eis-returned-conflict")
                )
              )
            )
            verifyThatDownstreamApiWasCalled()
          }
        }
        "invalid, with a status code of" - {
          "service unavailable" in {
            stubFor(
              post(urlEqualTo(connectorPath))
                .withRequestBody(equalToJson("""
                                               |{
                                               |    "name": "Dyson",
                                               |    "address": {
                                               |        "lineOne": "34 Park Lane",
                                               |        "lineTwo": "Building A",
                                               |        "lineThree": "Suite 100",
                                               |        "lineFour": "Manchester",
                                               |        "postalCode": "M54 1MQ",
                                               |        "countryCode": "GB"
                                               |    },
                                               |    "contactDetails": {
                                               |        "landline": "747663966",
                                               |        "mobile": "38390756243",
                                               |        "fax": "58371813020",
                                               |        "emailAddress": "dyson@example.com"
                                               |    }
                                               |}
                                               |""".stripMargin))
                .willReturn(
                  aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withStatus(SERVICE_UNAVAILABLE)
                    .withBody("""
                                |[
                                |  {
                                |    "codex": "eis-returned-service-unavailable"
                                |  }
                                |]
                                |""".stripMargin)
                )
            )

            val request = Request(
              name = "Dyson",
              address = CommonRegistrationWithoutIdRequests.Address(lineOne = "34 Park Lane",
                                                                    lineTwo = Some("Building A"),
                                                                    lineThree = Some("Suite 100"),
                                                                    lineFour = Some("Manchester"),
                                                                    postalCode = "M54 1MQ",
                                                                    countryCode = "GB"
              ),
              contactDetails = CommonRegistrationWithoutIdRequests.ContactDetails(landline = Some("747663966"),
                                                                                  mobile = Some("38390756243"),
                                                                                  fax = Some("58371813020"),
                                                                                  emailAddress = Some("dyson@example.com")
              )
            )

            assertThrows[BaseConnector.Exceptions.ResponseParsingException] {
              await(service.call(request))
            }

            verifyThatDownstreamApiWasCalled()
          }
          "OK" in {
            stubFor(
              post(urlEqualTo(connectorPath))
                .withRequestBody(equalToJson("""
                                               |{
                                               |    "name": "Dyson",
                                               |    "address": {
                                               |        "lineOne": "34 Park Lane",
                                               |        "lineTwo": "Building A",
                                               |        "lineThree": "Suite 100",
                                               |        "lineFour": "Manchester",
                                               |        "postalCode": "M54 1MQ",
                                               |        "countryCode": "GB"
                                               |    },
                                               |    "contactDetails": {
                                               |        "landline": "747663966",
                                               |        "mobile": "38390756243",
                                               |        "fax": "58371813020",
                                               |        "emailAddress": "dyson@example.com"
                                               |    }
                                               |}
                                               |""".stripMargin))
                .willReturn(
                  aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withStatus(OK)
                    .withBody("""
                                |{
                                |    "things": [
                                |        {
                                |            "type": "ARN",
                                |            "value": "WARN3849921"
                                |        },
                                |        {
                                |            "type": "SAFE",
                                |            "value": "XE0000200775706"
                                |        },
                                |        {
                                |            "type": "SAP",
                                |            "value": "1960629967"
                                |        }
                                |    ]
                                |}
                                |""".stripMargin)
                )
            )

            val request = Request(
              name = "Dyson",
              address = CommonRegistrationWithoutIdRequests.Address(lineOne = "34 Park Lane",
                                                                    lineTwo = Some("Building A"),
                                                                    lineThree = Some("Suite 100"),
                                                                    lineFour = Some("Manchester"),
                                                                    postalCode = "M54 1MQ",
                                                                    countryCode = "GB"
              ),
              contactDetails = CommonRegistrationWithoutIdRequests.ContactDetails(landline = Some("747663966"),
                                                                                  mobile = Some("38390756243"),
                                                                                  fax = Some("58371813020"),
                                                                                  emailAddress = Some("dyson@example.com")
              )
            )

            assertThrows[BaseConnector.Exceptions.ResponseParsingException] {
              await(service.call(request))
            }

            verifyThatDownstreamApiWasCalled()
          }
        }
      }
    }
  }
}
