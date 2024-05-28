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

package services.subscription.create

import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, equalToJson, post, stubFor, urlEqualTo}
import connectors.BaseConnector
import connectors.subscription.create.SubscriptionCreationConnector
import play.api.http.Status.{BAD_REQUEST, CONFLICT, INTERNAL_SERVER_ERROR, OK, SERVICE_UNAVAILABLE}
import services.BaseService.Responses.Error
import services.subscription.{create, SubscriptionService}
import services.{BaseBackendConnectorSpec, BaseService}

class SubscriptionCreationServiceSpec extends BaseBackendConnectorSpec {

  private val connectorPath: String = SubscriptionCreationConnector.connectorPath
  private lazy val service          = app.injector.instanceOf[SubscriptionCreationService]

  "attempting to create a subscription, when" - {
    "the response from the connector" - {
      "succeeds, when" - {
        "there are two contacts, one of each type" in {
          stubFor(
            post(urlEqualTo(connectorPath))
              .withRequestBody(equalToJson("""
                  |{
                  |    "id": {
                  |        "type": "NINO",
                  |        "value": "AA000000A"
                  |    },
                  |    "name": "Harold Winter",
                  |    "contacts": [
                  |        {
                  |            "type": "I",
                  |            "firstName": "Patrick",
                  |            "middleName": "John",
                  |            "lastName": "Dyson",
                  |            "landline": "747663966",
                  |            "mobile": "38390756243",
                  |            "emailAddress": "Patrick.Dyson@example.com"
                  |        },
                  |        {
                  |            "type": "O",
                  |            "name": "Dyson",
                  |            "landline": "847663966",
                  |            "mobile": "48390756243",
                  |            "emailAddress": "info@example.com"
                  |        }
                  |    ]
                  |}
                  |""".stripMargin))
              .willReturn(
                aResponse()
                  .withHeader("Content-Type", "application/json")
                  .withStatus(OK)
                  .withBody("""
                      |{
                      |  "id": "c763df13-41b3-46d3-bff2-08c090b860dc"
                      |}
                      |""".stripMargin)
              )
          )

          val request = SubscriptionCreationService.Requests.Request(
            id = SubscriptionCreationService.Requests.Id(SubscriptionCreationService.Requests.IdType.NINO, "AA000000A"),
            name = Some("Harold Winter"),
            contacts = Seq(
              SubscriptionService.Requests.Individual(
                firstName = "Patrick",
                middleName = Some("John"),
                lastName = "Dyson",
                landline = Some("747663966"),
                mobile = Some("38390756243"),
                emailAddress = "Patrick.Dyson@example.com"
              ),
              SubscriptionService.Requests.Organisation(name = "Dyson",
                                                        landline = Some("847663966"),
                                                        mobile = Some("48390756243"),
                                                        emailAddress = "info@example.com"
              )
            )
          )

          val response = await(service.call(request))

          response shouldBe Right(SubscriptionService.Responses.Response("c763df13-41b3-46d3-bff2-08c090b860dc"))
          verifyThatDownstreamApiWasCalled()
        }
        "there is only contact, an individual" in {
          stubFor(
            post(urlEqualTo(connectorPath))
              .withRequestBody(equalToJson("""
                  |{
                  |    "id": {
                  |        "type": "NINO",
                  |        "value": "AA000000A"
                  |    },
                  |    "name": "Harold Winter",
                  |    "contacts": [
                  |        {
                  |            "type": "I",
                  |            "firstName": "Patrick",
                  |            "middleName": "John",
                  |            "lastName": "Dyson",
                  |            "landline": "747663966",
                  |            "mobile": "38390756243",
                  |            "emailAddress": "Patrick.Dyson@example.com"
                  |        }
                  |    ]
                  |}
                  |""".stripMargin))
              .willReturn(
                aResponse()
                  .withHeader("Content-Type", "application/json")
                  .withStatus(OK)
                  .withBody("""
                      |{
                      |  "id": "c763df13-41b3-46d3-bff2-08c090b860dc"
                      |}
                      |""".stripMargin)
              )
          )

          val request = SubscriptionCreationService.Requests.Request(
            id = SubscriptionCreationService.Requests.Id(SubscriptionCreationService.Requests.IdType.NINO, "AA000000A"),
            name = Some("Harold Winter"),
            contacts = Seq(
              SubscriptionService.Requests.Individual(
                firstName = "Patrick",
                middleName = Some("John"),
                lastName = "Dyson",
                landline = Some("747663966"),
                mobile = Some("38390756243"),
                emailAddress = "Patrick.Dyson@example.com"
              )
            )
          )

          val response = await(service.call(request))

          response shouldBe Right(SubscriptionService.Responses.Response("c763df13-41b3-46d3-bff2-08c090b860dc"))
          verifyThatDownstreamApiWasCalled()
        }
        "there is only contact, an organisation" in {
          stubFor(
            post(urlEqualTo(connectorPath))
              .withRequestBody(equalToJson("""
                  |{
                  |    "id": {
                  |        "type": "NINO",
                  |        "value": "AA000000A"
                  |    },
                  |    "name": "Harold Winter",
                  |    "contacts": [
                  |        {
                  |            "type": "O",
                  |            "name": "Dyson",
                  |            "landline": "847663966",
                  |            "mobile": "48390756243",
                  |            "emailAddress": "info@example.com"
                  |        }
                  |    ]
                  |}
                  |""".stripMargin))
              .willReturn(
                aResponse()
                  .withHeader("Content-Type", "application/json")
                  .withStatus(OK)
                  .withBody("""
                      |{
                      |  "id": "c763df13-41b3-46d3-bff2-08c090b860dc"
                      |}
                      |""".stripMargin)
              )
          )

          val request = SubscriptionCreationService.Requests.Request(
            id = SubscriptionCreationService.Requests.Id(SubscriptionCreationService.Requests.IdType.NINO, "AA000000A"),
            name = Some("Harold Winter"),
            contacts = Seq(
              SubscriptionService.Requests.Organisation(name = "Dyson",
                                                        landline = Some("847663966"),
                                                        mobile = Some("48390756243"),
                                                        emailAddress = "info@example.com"
              )
            )
          )

          val response = await(service.call(request))

          response shouldBe Right(SubscriptionService.Responses.Response("c763df13-41b3-46d3-bff2-08c090b860dc"))
          verifyThatDownstreamApiWasCalled()
        }
      }
      "fails, where the response body is" - {
        "absent, with a status code of" - {
          "internal service error" in {
            stubFor(
              post(urlEqualTo(connectorPath))
                .withRequestBody(equalToJson("""
                                               |{
                                               |    "id": {
                                               |        "type": "NINO",
                                               |        "value": "AA000000A"
                                               |    },
                                               |    "name": "Harold Winter",
                                               |    "contacts": [
                                               |        {
                                               |            "type": "I",
                                               |            "firstName": "Patrick",
                                               |            "middleName": "John",
                                               |            "lastName": "Dyson",
                                               |            "landline": "747663966",
                                               |            "mobile": "38390756243",
                                               |            "emailAddress": "Patrick.Dyson@example.com"
                                               |        },
                                               |        {
                                               |            "type": "O",
                                               |            "name": "Dyson",
                                               |            "landline": "847663966",
                                               |            "mobile": "48390756243",
                                               |            "emailAddress": "info@example.com"
                                               |        }
                                               |    ]
                                               |}
                                               |""".stripMargin))
                .willReturn(
                  aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withStatus(INTERNAL_SERVER_ERROR)
                )
            )

            val request = SubscriptionCreationService.Requests.Request(
              id = SubscriptionCreationService.Requests.Id(SubscriptionCreationService.Requests.IdType.NINO, "AA000000A"),
              name = Some("Harold Winter"),
              contacts = Seq(
                SubscriptionService.Requests.Individual(
                  firstName = "Patrick",
                  middleName = Some("John"),
                  lastName = "Dyson",
                  landline = Some("747663966"),
                  mobile = Some("38390756243"),
                  emailAddress = "Patrick.Dyson@example.com"
                ),
                SubscriptionService.Requests.Organisation(name = "Dyson",
                                                          landline = Some("847663966"),
                                                          mobile = Some("48390756243"),
                                                          emailAddress = "info@example.com"
                )
              )
            )

            val response = await(service.call(request))

            response shouldBe Left(BaseService.Responses.Errors(INTERNAL_SERVER_ERROR))
            verifyThatDownstreamApiWasCalled()
          }
        }
        "valid, with a status code of" - {
          "bad request" in {
            stubFor(
              post(urlEqualTo(connectorPath))
                .withRequestBody(equalToJson("""
                                               |{
                                               |    "id": {
                                               |        "type": "NINO",
                                               |        "value": "AA000000A"
                                               |    },
                                               |    "name": "Harold Winter",
                                               |    "contacts": [
                                               |        {
                                               |            "type": "I",
                                               |            "firstName": "",
                                               |            "middleName": "John",
                                               |            "lastName": "",
                                               |            "landline": "747663966",
                                               |            "mobile": "38390756243",
                                               |            "emailAddress": "Patrick.Dyson@example.com"
                                               |        },
                                               |        {
                                               |            "type": "O",
                                               |            "name": "Dyson",
                                               |            "landline": "847663966",
                                               |            "mobile": "48390756243",
                                               |            "emailAddress": "info@example.com"
                                               |        }
                                               |    ]
                                               |}
                                               |""".stripMargin))
                .willReturn(
                  aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withStatus(BAD_REQUEST)
                    .withBody("""
                                |[
                                |  {
                                |    "code": "invalid-first-name"
                                |  },
                                |  {
                                |    "code": "invalid-last-name"
                                |  }
                                |]
                                |""".stripMargin)
                )
            )

            val request = SubscriptionCreationService.Requests.Request(
              id = SubscriptionCreationService.Requests.Id(SubscriptionCreationService.Requests.IdType.NINO, "AA000000A"),
              name = Some("Harold Winter"),
              contacts = Seq(
                SubscriptionService.Requests.Individual(firstName = "",
                                                        middleName = Some("John"),
                                                        lastName = "",
                                                        landline = Some("747663966"),
                                                        mobile = Some("38390756243"),
                                                        emailAddress = "Patrick.Dyson@example.com"
                ),
                SubscriptionService.Requests.Organisation(name = "Dyson",
                                                          landline = Some("847663966"),
                                                          mobile = Some("48390756243"),
                                                          emailAddress = "info@example.com"
                )
              )
            )

            val response = await(service.call(request))

            response shouldBe Left(
              BaseService.Responses.Errors(
                BAD_REQUEST,
                Seq(
                  Error("invalid-first-name"),
                  Error("invalid-last-name")
                )
              )
            )
            verifyThatDownstreamApiWasCalled()
          }
          "service unavailable" in {
            stubFor(
              post(urlEqualTo(connectorPath))
                .withRequestBody(equalToJson("""
                                               |{
                                               |    "id": {
                                               |        "type": "NINO",
                                               |        "value": "AA000000A"
                                               |    },
                                               |    "name": "Harold Winter",
                                               |    "contacts": [
                                               |        {
                                               |            "type": "I",
                                               |            "firstName": "Patrick",
                                               |            "middleName": "John",
                                               |            "lastName": "Dyson",
                                               |            "landline": "747663966",
                                               |            "mobile": "38390756243",
                                               |            "emailAddress": "Patrick.Dyson@example.com"
                                               |        },
                                               |        {
                                               |            "type": "O",
                                               |            "name": "Dyson",
                                               |            "landline": "847663966",
                                               |            "mobile": "48390756243",
                                               |            "emailAddress": "info@example.com"
                                               |        }
                                               |    ]
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

            val request = SubscriptionCreationService.Requests.Request(
              id = SubscriptionCreationService.Requests.Id(SubscriptionCreationService.Requests.IdType.NINO, "AA000000A"),
              name = Some("Harold Winter"),
              contacts = Seq(
                SubscriptionService.Requests.Individual(
                  firstName = "Patrick",
                  middleName = Some("John"),
                  lastName = "Dyson",
                  landline = Some("747663966"),
                  mobile = Some("38390756243"),
                  emailAddress = "Patrick.Dyson@example.com"
                ),
                SubscriptionService.Requests.Organisation(name = "Dyson",
                                                          landline = Some("847663966"),
                                                          mobile = Some("48390756243"),
                                                          emailAddress = "info@example.com"
                )
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
          "conflict" in {
            stubFor(
              post(urlEqualTo(connectorPath))
                .withRequestBody(equalToJson("""
                                               |{
                                               |    "id": {
                                               |        "type": "NINO",
                                               |        "value": "AA000000A"
                                               |    },
                                               |    "name": "Harold Winter",
                                               |    "contacts": [
                                               |        {
                                               |            "type": "I",
                                               |            "firstName": "Patrick",
                                               |            "middleName": "John",
                                               |            "lastName": "Dyson",
                                               |            "landline": "747663966",
                                               |            "mobile": "38390756243",
                                               |            "emailAddress": "Patrick.Dyson@example.com"
                                               |        },
                                               |        {
                                               |            "type": "O",
                                               |            "name": "Dyson",
                                               |            "landline": "847663966",
                                               |            "mobile": "48390756243",
                                               |            "emailAddress": "info@example.com"
                                               |        }
                                               |    ]
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

            val request = SubscriptionCreationService.Requests.Request(
              id = SubscriptionCreationService.Requests.Id(SubscriptionCreationService.Requests.IdType.NINO, "AA000000A"),
              name = Some("Harold Winter"),
              contacts = Seq(
                SubscriptionService.Requests.Individual(
                  firstName = "Patrick",
                  middleName = Some("John"),
                  lastName = "Dyson",
                  landline = Some("747663966"),
                  mobile = Some("38390756243"),
                  emailAddress = "Patrick.Dyson@example.com"
                ),
                SubscriptionService.Requests.Organisation(name = "Dyson",
                                                          landline = Some("847663966"),
                                                          mobile = Some("48390756243"),
                                                          emailAddress = "info@example.com"
                )
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
                                               |    "id": {
                                               |        "type": "NINO",
                                               |        "value": "AA000000A"
                                               |    },
                                               |    "name": "Harold Winter",
                                               |    "contacts": [
                                               |        {
                                               |            "type": "I",
                                               |            "firstName": "Patrick",
                                               |            "middleName": "John",
                                               |            "lastName": "Dyson",
                                               |            "landline": "747663966",
                                               |            "mobile": "38390756243",
                                               |            "emailAddress": "Patrick.Dyson@example.com"
                                               |        },
                                               |        {
                                               |            "type": "O",
                                               |            "name": "Dyson",
                                               |            "landline": "847663966",
                                               |            "mobile": "48390756243",
                                               |            "emailAddress": "info@example.com"
                                               |        }
                                               |    ]
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

            val request = SubscriptionCreationService.Requests.Request(
              id = SubscriptionCreationService.Requests.Id(SubscriptionCreationService.Requests.IdType.NINO, "AA000000A"),
              name = Some("Harold Winter"),
              contacts = Seq(
                SubscriptionService.Requests.Individual(
                  firstName = "Patrick",
                  middleName = Some("John"),
                  lastName = "Dyson",
                  landline = Some("747663966"),
                  mobile = Some("38390756243"),
                  emailAddress = "Patrick.Dyson@example.com"
                ),
                SubscriptionService.Requests.Organisation(name = "Dyson",
                                                          landline = Some("847663966"),
                                                          mobile = Some("48390756243"),
                                                          emailAddress = "info@example.com"
                )
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
                                               |    "id": {
                                               |        "type": "NINO",
                                               |        "value": "AA000000A"
                                               |    },
                                               |    "name": "Harold Winter",
                                               |    "contacts": [
                                               |        {
                                               |            "type": "I",
                                               |            "firstName": "Patrick",
                                               |            "middleName": "John",
                                               |            "lastName": "Dyson",
                                               |            "landline": "747663966",
                                               |            "mobile": "38390756243",
                                               |            "emailAddress": "Patrick.Dyson@example.com"
                                               |        },
                                               |        {
                                               |            "type": "O",
                                               |            "name": "Dyson",
                                               |            "landline": "847663966",
                                               |            "mobile": "48390756243",
                                               |            "emailAddress": "info@example.com"
                                               |        }
                                               |    ]
                                               |}
                                               |""".stripMargin))
                .willReturn(
                  aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withStatus(OK)
                    .withBody("""
                                |{
                                |}
                                |""".stripMargin)
                )
            )

            val request = SubscriptionCreationService.Requests.Request(
              id = SubscriptionCreationService.Requests.Id(SubscriptionCreationService.Requests.IdType.NINO, "AA000000A"),
              name = Some("Harold Winter"),
              contacts = Seq(
                SubscriptionService.Requests.Individual(
                  firstName = "Patrick",
                  middleName = Some("John"),
                  lastName = "Dyson",
                  landline = Some("747663966"),
                  mobile = Some("38390756243"),
                  emailAddress = "Patrick.Dyson@example.com"
                ),
                SubscriptionService.Requests.Organisation(name = "Dyson",
                                                          landline = Some("847663966"),
                                                          mobile = Some("48390756243"),
                                                          emailAddress = "info@example.com"
                )
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
