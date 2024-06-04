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

package services.subscription.read

import com.github.tomakehurst.wiremock.client.WireMock._
import connectors.BaseConnector.Exceptions.ResponseParsingException
import connectors.subscription.SubscriptionConnector
import play.api.http.Status._
import services.BaseService.Responses.Error
import services.{BaseBackendConnectorSpec, BaseService}
import services.subscription.SubscriptionService

class SubscriptionReadServiceSpec extends BaseBackendConnectorSpec {

  private val connectorPath: String = SubscriptionConnector.connectorPath
  private lazy val service          = app.injector.instanceOf[SubscriptionReadService]

  "attempting to read a subscription, when" - {
    "the response from the connector" - {
      "succeeds" in {
        stubFor(
          get(urlEqualTo(s"$connectorPath/a7405c8d-06ee-46a3-b5a0-5d65176360ed"))
            .willReturn(
              aResponse()
                .withHeader("Content-Type", "application/json")
                .withStatus(OK)
                .withBody("""
                          |{
                          |    "id": "a7405c8d-06ee-46a3-b5a0-5d65176360ed",
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
                          |""".stripMargin)
            )
        )
        val request = SubscriptionReadService.Requests.Request(
          id = "a7405c8d-06ee-46a3-b5a0-5d65176360ed",
        )
        val response = await(service.call(request))
        response shouldBe Right(Some(SubscriptionReadService.Responses.Response(
          name = "Harold Winter",
          contacts = Seq(
            SubscriptionService.Data.Individual(
              firstName = "Patrick",
              middleName = Some("John"),
              lastName = "Dyson",
              landline = Some("747663966"),
              mobile = Some("38390756243"),
              emailAddress = "Patrick.Dyson@example.com"
            ),
            SubscriptionService.Data.Organisation(
              name = "Dyson",
              landline = Some("847663966"),
              mobile = Some("48390756243"),
              emailAddress = "info@example.com"
            )
          )
        )))
        verifyThatDownstreamApiWasCalled()
      }
      "fails, with a status code of" - {
        "internal server error" in {
          stubFor(
            get(urlEqualTo(s"$connectorPath/a7405c8d-06ee-46a3-b5a0-5d65176360ed"))
              .willReturn(
                aResponse()
                  .withHeader("Content-Type", "application/json")
                  .withStatus(INTERNAL_SERVER_ERROR)
              )
          )
          val request = SubscriptionReadService.Requests.Request(
            id = "a7405c8d-06ee-46a3-b5a0-5d65176360ed",
          )
          val response = await(service.call(request))
          response shouldBe Left(
            BaseService.Responses.Errors(
              INTERNAL_SERVER_ERROR,
              Seq()
            )
          )
          verifyThatDownstreamApiWasCalled()
        }
        "service unavailable" in {
          stubFor(
            get(urlEqualTo(s"$connectorPath/a7405c8d-06ee-46a3-b5a0-5d65176360ed"))
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
          val request = SubscriptionReadService.Requests.Request(
            id = "a7405c8d-06ee-46a3-b5a0-5d65176360ed",
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
            get(urlEqualTo(s"$connectorPath/a7405c8d-06ee-46a3-b5a0-5d65176360ed"))
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
          val request = SubscriptionReadService.Requests.Request(
            id = "a7405c8d-06ee-46a3-b5a0-5d65176360ed",
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
            get(urlEqualTo(s"$connectorPath/a7405c8d-06ee-46a3-b5a0-5d65176360ed"))
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
          val request = SubscriptionReadService.Requests.Request(
            id = "a7405c8d-06ee-46a3-b5a0-5d65176360ed",
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
            get(urlEqualTo(s"$connectorPath/a7405c8d-06ee-46a3-b5a0-5d65176360ed"))
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
          val request = SubscriptionReadService.Requests.Request(
            id = "a7405c8d-06ee-46a3-b5a0-5d65176360ed",
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
            get(urlEqualTo(s"$connectorPath/a7405c8d-06ee-46a3-b5a0-5d65176360ed"))
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
          val request = SubscriptionReadService.Requests.Request(
            id = "a7405c8d-06ee-46a3-b5a0-5d65176360ed",
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
      "fails, with an invalid response body for a status of" - {
        "OK with" - {
          "unexpected JSON" in {
            stubFor(
              get(urlEqualTo(s"$connectorPath/a7405c8d-06ee-46a3-b5a0-5d65176360ed"))
                .willReturn(
                  aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withStatus(OK)
                    .withBody(
                      """
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
                        |""".stripMargin)
                )
            )
            val request = SubscriptionReadService.Requests.Request(
              id = "a7405c8d-06ee-46a3-b5a0-5d65176360ed",
            )
            assertThrows[ResponseParsingException] {
              await(service.call(request))
            }
            verifyThatDownstreamApiWasCalled()
          }
          "invalid JSON" in {
            stubFor(
              get(urlEqualTo(s"$connectorPath/a7405c8d-06ee-46a3-b5a0-5d65176360ed"))
                .willReturn(
                  aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withStatus(OK)
                    .withBody(
                      """
                        |{
                        |""".stripMargin)
                )
            )
            val request = SubscriptionReadService.Requests.Request(
              id = "a7405c8d-06ee-46a3-b5a0-5d65176360ed",
            )
            assertThrows[ResponseParsingException] {
              await(service.call(request))
            }
            verifyThatDownstreamApiWasCalled()
          }
        }
        "error with" - {
          "unexpected JSON" in {
            stubFor(
              get(urlEqualTo(s"$connectorPath/a7405c8d-06ee-46a3-b5a0-5d65176360ed"))
                .willReturn(
                  aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withStatus(NOT_FOUND)
                    .withBody(
                      """
                        |[
                        |  {
                        |    "xcode": "eis-returned-not-found"
                        |  }
                        |]
                        |""".stripMargin)
                )
            )
            val request = SubscriptionReadService.Requests.Request(
              id = "a7405c8d-06ee-46a3-b5a0-5d65176360ed",
            )
            assertThrows[ResponseParsingException] {
              await(service.call(request))
            }
            verifyThatDownstreamApiWasCalled()
          }
          "invalid JSON" in {
            stubFor(
              get(urlEqualTo(s"$connectorPath/a7405c8d-06ee-46a3-b5a0-5d65176360ed"))
                .willReturn(
                  aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withStatus(NOT_FOUND)
                    .withBody(
                      """
                        |[
                        |""".stripMargin)
                )
            )
            val request = SubscriptionReadService.Requests.Request(
              id = "a7405c8d-06ee-46a3-b5a0-5d65176360ed",
            )
            assertThrows[ResponseParsingException] {
              await(service.call(request))
            }
            verifyThatDownstreamApiWasCalled()
          }
        }
      }
    }
  }
}
