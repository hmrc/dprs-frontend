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

package services.subscription.update

import com.github.tomakehurst.wiremock.client.WireMock._
import connectors.subscription.SubscriptionConnector
import play.api.http.Status._
import services.BaseService.Responses.Error
import services.subscription.SubscriptionService
import services.{BaseBackendConnectorSpec, BaseService}

class SubscriptionUpdateServiceSpec extends BaseBackendConnectorSpec {

  private val connectorPath: String = SubscriptionConnector.connectorPath
  private lazy val service          = app.injector.instanceOf[SubscriptionUpdateService]

  "attempting to update a subscription, when" - {
    "the response from the connector" - {
      "succeeds, when" - {
        "there are two contacts, one of each type" in {
          stubFor(
            post(urlEqualTo(s"$connectorPath/a7405c8d-06ee-46a3-b5a0-5d65176360ed"))
              .withRequestBody(equalToJson(s"""
                  |{
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
                  .withStatus(NO_CONTENT)
              )
          )
          val request = SubscriptionUpdateService.Requests.Request(
            id = "a7405c8d-06ee-46a3-b5a0-5d65176360ed",
            name = Some("Harold Winter"),
            contacts = Seq(
              SubscriptionService.RequestOrResponse.Individual(
                firstName = "Patrick",
                middleName = Some("John"),
                lastName = "Dyson",
                landline = Some("747663966"),
                mobile = Some("38390756243"),
                emailAddress = "Patrick.Dyson@example.com"
              ),
              SubscriptionService.RequestOrResponse.Organisation(
                name = "Dyson",
                landline = Some("847663966"),
                mobile = Some("48390756243"),
                emailAddress = "info@example.com"
              )
            )
          )
          val response = await(service.call(request))
          response shouldBe Right(None)
          verifyThatDownstreamApiWasCalled()
        }
        "there is only one contact, an individual" in {
          stubFor(
            post(urlEqualTo(s"$connectorPath/a7405c8d-06ee-46a3-b5a0-5d65176360ed"))
              .withRequestBody(equalToJson("""
                  |{
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
                  .withStatus(NO_CONTENT)
              )
          )
          val request = SubscriptionUpdateService.Requests.Request(
            id = "a7405c8d-06ee-46a3-b5a0-5d65176360ed",
            name = Some("Harold Winter"),
            contacts = Seq(
              SubscriptionService.RequestOrResponse.Individual(
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
          response shouldBe Right(None)
          verifyThatDownstreamApiWasCalled()
        }
        "there is only one contact, an organisation" in {
          stubFor(
            post(urlEqualTo(s"$connectorPath/a7405c8d-06ee-46a3-b5a0-5d65176360ed"))
              .withRequestBody(equalToJson("""
                  |{
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
                  .withStatus(NO_CONTENT)
              )
          )
          val request = SubscriptionUpdateService.Requests.Request(
            id = "a7405c8d-06ee-46a3-b5a0-5d65176360ed",
            name = Some("Harold Winter"),
            contacts = Seq(
              SubscriptionService.RequestOrResponse.Organisation(
                name = "Dyson",
                landline = Some("847663966"),
                mobile = Some("48390756243"),
                emailAddress = "info@example.com"
              )
            )
          )
          val response = await(service.call(request))
          response shouldBe Right(None)
          verifyThatDownstreamApiWasCalled()
        }
      }
      "fails, with a status code of" - {
        "bad request" in {
          stubFor(
            post(urlEqualTo(s"$connectorPath/a7405c8d-06ee-46a3-b5a0-5d65176360ed"))
              .withRequestBody(equalToJson("""
                                               |{
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
          val request = SubscriptionUpdateService.Requests.Request(
            id = "a7405c8d-06ee-46a3-b5a0-5d65176360ed",
            name = Some("Harold Winter"),
            contacts = Seq(
              SubscriptionService.RequestOrResponse.Individual(
                firstName = "",
                middleName = Some("John"),
                lastName = "",
                landline = Some("747663966"),
                mobile = Some("38390756243"),
                emailAddress = "Patrick.Dyson@example.com"
              ),
              SubscriptionService.RequestOrResponse.Organisation(
                name = "Dyson",
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
            post(urlEqualTo(s"$connectorPath/a7405c8d-06ee-46a3-b5a0-5d65176360ed"))
              .withRequestBody(equalToJson("""
                                               |{
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
          val request = SubscriptionUpdateService.Requests.Request(
            id = "a7405c8d-06ee-46a3-b5a0-5d65176360ed",
            name = Some("Harold Winter"),
            contacts = Seq(
              SubscriptionService.RequestOrResponse.Individual(
                firstName = "",
                middleName = Some("John"),
                lastName = "",
                landline = Some("747663966"),
                mobile = Some("38390756243"),
                emailAddress = "Patrick.Dyson@example.com"
              ),
              SubscriptionService.RequestOrResponse.Organisation(
                name = "Dyson",
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
            post(urlEqualTo(s"$connectorPath/a7405c8d-06ee-46a3-b5a0-5d65176360ed"))
              .withRequestBody(equalToJson("""
                                               |{
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
          val request = SubscriptionUpdateService.Requests.Request(
            id = "a7405c8d-06ee-46a3-b5a0-5d65176360ed",
            name = Some("Harold Winter"),
            contacts = Seq(
              SubscriptionService.RequestOrResponse.Individual(
                firstName = "",
                middleName = Some("John"),
                lastName = "",
                landline = Some("747663966"),
                mobile = Some("38390756243"),
                emailAddress = "Patrick.Dyson@example.com"
              ),
              SubscriptionService.RequestOrResponse.Organisation(
                name = "Dyson",
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
        "forbidden" in {
          stubFor(
            post(urlEqualTo(s"$connectorPath/a7405c8d-06ee-46a3-b5a0-5d65176360ed"))
              .withRequestBody(equalToJson("""
                                               |{
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
                  .withStatus(FORBIDDEN)
                  .withBody("""
                                |[
                                |  {
                                |    "code": "eis-returned-forbidden"
                                |  }
                                |]
                                |""".stripMargin)
              )
          )
          val request = SubscriptionUpdateService.Requests.Request(
            id = "a7405c8d-06ee-46a3-b5a0-5d65176360ed",
            name = Some("Harold Winter"),
            contacts = Seq(
              SubscriptionService.RequestOrResponse.Individual(
                firstName = "",
                middleName = Some("John"),
                lastName = "",
                landline = Some("747663966"),
                mobile = Some("38390756243"),
                emailAddress = "Patrick.Dyson@example.com"
              ),
              SubscriptionService.RequestOrResponse.Organisation(
                name = "Dyson",
                landline = Some("847663966"),
                mobile = Some("48390756243"),
                emailAddress = "info@example.com"
              )
            )
          )

          val response = await(service.call(request))

          response shouldBe Left(
            BaseService.Responses.Errors(
              FORBIDDEN,
              Seq(
                Error("eis-returned-forbidden")
              )
            )
          )
          verifyThatDownstreamApiWasCalled()
        }
        "unauthorised" in {
          stubFor(
            post(urlEqualTo(s"$connectorPath/a7405c8d-06ee-46a3-b5a0-5d65176360ed"))
              .withRequestBody(equalToJson("""
                                               |{
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
                  .withStatus(UNAUTHORIZED)
                  .withBody("""
                                |[
                                |  {
                                |    "code": "eis-returned-unauthorised"
                                |  }
                                |]
                                |""".stripMargin)
              )
          )
          val request = SubscriptionUpdateService.Requests.Request(
            id = "a7405c8d-06ee-46a3-b5a0-5d65176360ed",
            name = Some("Harold Winter"),
            contacts = Seq(
              SubscriptionService.RequestOrResponse.Individual(
                firstName = "",
                middleName = Some("John"),
                lastName = "",
                landline = Some("747663966"),
                mobile = Some("38390756243"),
                emailAddress = "Patrick.Dyson@example.com"
              ),
              SubscriptionService.RequestOrResponse.Organisation(
                name = "Dyson",
                landline = Some("847663966"),
                mobile = Some("48390756243"),
                emailAddress = "info@example.com"
              )
            )
          )

          val response = await(service.call(request))

          response shouldBe Left(
            BaseService.Responses.Errors(
              UNAUTHORIZED,
              Seq(
                Error("eis-returned-unauthorised")
              )
            )
          )
          verifyThatDownstreamApiWasCalled()
        }
        "not found" in {
          stubFor(
            post(urlEqualTo(s"$connectorPath/a7405c8d-06ee-46a3-b5a0-5d65176360ed"))
              .withRequestBody(equalToJson("""
                    |{
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
                  .withStatus(NOT_FOUND)
                  .withBody("""
                        |[
                        |  {
                        |    "code": "eis-returned-not-found"
                        |  }
                        |]
                        |""".stripMargin)
              )
          )
          val request = SubscriptionUpdateService.Requests.Request(
            id = "a7405c8d-06ee-46a3-b5a0-5d65176360ed",
            name = Some("Harold Winter"),
            contacts = Seq(
              SubscriptionService.RequestOrResponse.Individual(
                firstName = "",
                middleName = Some("John"),
                lastName = "",
                landline = Some("747663966"),
                mobile = Some("38390756243"),
                emailAddress = "Patrick.Dyson@example.com"
              ),
              SubscriptionService.RequestOrResponse.Organisation(
                name = "Dyson",
                landline = Some("847663966"),
                mobile = Some("48390756243"),
                emailAddress = "info@example.com"
              )
            )
          )

          val response = await(service.call(request))

          response shouldBe Left(
            BaseService.Responses.Errors(
              NOT_FOUND,
              Seq(
                Error("eis-returned-not-found")
              )
            )
          )
          verifyThatDownstreamApiWasCalled()
        }
      }
    }
  }
}
