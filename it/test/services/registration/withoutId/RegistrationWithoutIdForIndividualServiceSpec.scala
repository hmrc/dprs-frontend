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
import connectors.registration.withoutId.RegistrationWithoutIdForIndividualConnector.connectorPath
import play.api.http.Status.{BAD_REQUEST, CONFLICT, INTERNAL_SERVER_ERROR, OK, SERVICE_UNAVAILABLE}
import services.BaseService.Responses.Error
import services.registration.RegistrationService
import services.registration.withoutId.RegistrationWithoutIdForIndividualService.Requests
import services.registration.withoutId.RegistrationWithoutIdService.{Requests => RequestsCommon}
import services.{BaseBackendConnectorIntSpec, BaseService}

class RegistrationWithoutIdForIndividualServiceSpec extends BaseBackendConnectorIntSpec {

  private lazy val service = app.injector.instanceOf[RegistrationWithoutIdForIndividualService]

  "attempting to register without an ID, as an" - {
    "the response from the connector" - {
      "succeeds" in {
        stubFor(
          post(urlEqualTo(connectorPath))
            .withRequestBody(equalToJson("""
                  |{
                  |    "firstName": "Patrick",
                  |    "middleName": "John",
                  |    "lastName": "Dyson",
                  |    "dateOfBirth": "1970-10-04",
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
                  |        "emailAddress": "Patrick.Dyson@example.com"
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

        val request = Requests.Request(
          firstName = "Patrick",
          middleName = Some("John"),
          lastName = "Dyson",
          dateOfBirth = "1970-10-04",
          address = RequestsCommon.Address(lineOne = "34 Park Lane",
                                           lineTwo = Some("Building A"),
                                           lineThree = Some("Suite 100"),
                                           lineFour = Some("Manchester"),
                                           postalCode = "M54 1MQ",
                                           countryCode = "GB"
          ),
          contactDetails = RequestsCommon.ContactDetails(landline = Some("747663966"),
                                                         mobile = Some("38390756243"),
                                                         fax = Some("58371813020"),
                                                         emailAddress = Some("Patrick.Dyson@example.com")
          )
        )

        val response = await(service.call(request))

        response shouldBe Right(
          RegistrationService.Responses.Response(ids =
            Seq(
              RegistrationService.Responses.Id(RegistrationService.Responses.IdType.ARN, "WARN3849921"),
              RegistrationService.Responses.Id(RegistrationService.Responses.IdType.SAFE, "XE0000200775706"),
              RegistrationService.Responses.Id(RegistrationService.Responses.IdType.SAP, "1960629967")
            )
          )
        )
        verifyThatDownstreamApiWasCalled()
      }
      "fails, where the response body is" - {
        "valid, with a status code of" - {
          "internal service error" in {
            stubFor(
              post(urlEqualTo(connectorPath))
                .withRequestBody(equalToJson("""
                      |{
                      |    "firstName": "Patrick",
                      |    "middleName": "John",
                      |    "lastName": "Dyson",
                      |    "dateOfBirth": "1970-10-04",
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
                      |        "emailAddress": "Patrick.Dyson@example.com"
                      |    }
                      |}
                      |""".stripMargin))
                .willReturn(
                  aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withStatus(INTERNAL_SERVER_ERROR)
                )
            )

            val request = Requests.Request(
              firstName = "Patrick",
              middleName = Some("John"),
              lastName = "Dyson",
              dateOfBirth = "1970-10-04",
              address = RequestsCommon.Address(lineOne = "34 Park Lane",
                                               lineTwo = Some("Building A"),
                                               lineThree = Some("Suite 100"),
                                               lineFour = Some("Manchester"),
                                               postalCode = "M54 1MQ",
                                               countryCode = "GB"
              ),
              contactDetails = RequestsCommon.ContactDetails(landline = Some("747663966"),
                                                             mobile = Some("38390756243"),
                                                             fax = Some("58371813020"),
                                                             emailAddress = Some("Patrick.Dyson@example.com")
              )
            )

            val response = await(service.call(request))

            response shouldBe Left(BaseService.Responses.Errors(INTERNAL_SERVER_ERROR))
            verifyThatDownstreamApiWasCalled()
          }
          "service unavailable" in {
            stubFor(
              post(urlEqualTo(connectorPath))
                .withRequestBody(equalToJson("""
                      |{
                      |    "firstName": "Patrick",
                      |    "middleName": "John",
                      |    "lastName": "Dyson",
                      |    "dateOfBirth": "1970-10-04",
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
                      |        "emailAddress": "Patrick.Dyson@example.com"
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

            val request = Requests.Request(
              firstName = "Patrick",
              middleName = Some("John"),
              lastName = "Dyson",
              dateOfBirth = "1970-10-04",
              address = RequestsCommon.Address(lineOne = "34 Park Lane",
                                               lineTwo = Some("Building A"),
                                               lineThree = Some("Suite 100"),
                                               lineFour = Some("Manchester"),
                                               postalCode = "M54 1MQ",
                                               countryCode = "GB"
              ),
              contactDetails = RequestsCommon.ContactDetails(landline = Some("747663966"),
                                                             mobile = Some("38390756243"),
                                                             fax = Some("58371813020"),
                                                             emailAddress = Some("Patrick.Dyson@example.com")
              )
            )

            val response = await(service.call(request))

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
              post(urlEqualTo(connectorPath))
                .withRequestBody(equalToJson("""
                      |{
                      |    "firstName": "Patrick",
                      |    "middleName": "John",
                      |    "lastName": "",
                      |    "dateOfBirth": "10-04-1970",
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
                      |        "emailAddress": "Patrick.Dyson@example.com"
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
                          |    "code": "invalid-last-name"
                          |  },
                          |  {
                          |    "code": "invalid-date-of-birth"
                          |  }
                          |]
                          |""".stripMargin)
                )
            )

            val request = Requests.Request(
              firstName = "Patrick",
              middleName = Some("John"),
              lastName = "",
              dateOfBirth = "10-04-1970",
              address = RequestsCommon.Address(lineOne = "34 Park Lane",
                                               lineTwo = Some("Building A"),
                                               lineThree = Some("Suite 100"),
                                               lineFour = Some("Manchester"),
                                               postalCode = "M54 1MQ",
                                               countryCode = "GB"
              ),
              contactDetails = RequestsCommon.ContactDetails(landline = Some("747663966"),
                                                             mobile = Some("38390756243"),
                                                             fax = Some("58371813020"),
                                                             emailAddress = Some("Patrick.Dyson@example.com")
              )
            )

            val response = await(service.call(request))

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
          "conflict" in {
            stubFor(
              post(urlEqualTo(connectorPath))
                .withRequestBody(equalToJson("""
                                               |{
                                               |    "firstName": "Patrick",
                                               |    "middleName": "John",
                                               |    "lastName": "",
                                               |    "dateOfBirth": "10-04-1970",
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
                                               |        "emailAddress": "Patrick.Dyson@example.com"
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

            val request = Requests.Request(
              firstName = "Patrick",
              middleName = Some("John"),
              lastName = "",
              dateOfBirth = "10-04-1970",
              address = RequestsCommon.Address(lineOne = "34 Park Lane",
                                               lineTwo = Some("Building A"),
                                               lineThree = Some("Suite 100"),
                                               lineFour = Some("Manchester"),
                                               postalCode = "M54 1MQ",
                                               countryCode = "GB"
              ),
              contactDetails = RequestsCommon.ContactDetails(landline = Some("747663966"),
                                                             mobile = Some("38390756243"),
                                                             fax = Some("58371813020"),
                                                             emailAddress = Some("Patrick.Dyson@example.com")
              )
            )

            val response = await(service.call(request))

            response shouldBe Left(
              BaseService.Responses.Errors(
                CONFLICT,
                Seq(
                  Error("eis-returned-conflict")
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
                      |    "firstName": "Patrick",
                      |    "middleName": "John",
                      |    "lastName": "Dyson",
                      |    "dateOfBirth": "1970-10-04",
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
                      |        "emailAddress": "Patrick.Dyson@example.com"
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

            val request = Requests.Request(
              firstName = "Patrick",
              middleName = Some("John"),
              lastName = "Dyson",
              dateOfBirth = "1970-10-04",
              address = RequestsCommon.Address(lineOne = "34 Park Lane",
                                               lineTwo = Some("Building A"),
                                               lineThree = Some("Suite 100"),
                                               lineFour = Some("Manchester"),
                                               postalCode = "M54 1MQ",
                                               countryCode = "GB"
              ),
              contactDetails = RequestsCommon.ContactDetails(landline = Some("747663966"),
                                                             mobile = Some("38390756243"),
                                                             fax = Some("58371813020"),
                                                             emailAddress = Some("Patrick.Dyson@example.com")
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
                      |    "firstName": "Patrick",
                      |    "middleName": "John",
                      |    "lastName": "Dyson",
                      |    "dateOfBirth": "1970-10-04",
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
                      |        "emailAddress": "Patrick.Dyson@example.com"
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

            val request = Requests.Request(
              firstName = "Patrick",
              middleName = Some("John"),
              lastName = "Dyson",
              dateOfBirth = "1970-10-04",
              address = RequestsCommon.Address(lineOne = "34 Park Lane",
                                               lineTwo = Some("Building A"),
                                               lineThree = Some("Suite 100"),
                                               lineFour = Some("Manchester"),
                                               postalCode = "M54 1MQ",
                                               countryCode = "GB"
              ),
              contactDetails = RequestsCommon.ContactDetails(landline = Some("747663966"),
                                                             mobile = Some("38390756243"),
                                                             fax = Some("58371813020"),
                                                             emailAddress = Some("Patrick.Dyson@example.com")
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
