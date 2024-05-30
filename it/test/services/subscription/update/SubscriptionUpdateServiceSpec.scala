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
import connectors.BaseConnector
import connectors.subscription.update.SubscriptionUpdateConnector
import play.api.http.Status._
import services.BaseService.Responses.Error
import services.subscription.SubscriptionService
import services.{BaseBackendConnectorSpec, BaseService}

class SubscriptionUpdateServiceSpec extends BaseBackendConnectorSpec {

  private val connectorPath: String = SubscriptionUpdateConnector.connectorPath
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
              SubscriptionService.Requests.Individual(
                firstName = "Patrick",
                middleName = Some("John"),
                lastName = "Dyson",
                landline = Some("747663966"),
                mobile = Some("38390756243"),
                emailAddress = "Patrick.Dyson@example.com"
              ),
              SubscriptionService.Requests.Organisation(
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
              SubscriptionService.Requests.Organisation(
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
      "fails, where the response body is" - {
        "wrong, with a status code of service unavailable" in {
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
          val request = SubscriptionUpdateService.Requests.Request(
            id = "a7405c8d-06ee-46a3-b5a0-5d65176360ed",
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
              SubscriptionService.Requests.Organisation(
                name = "Dyson",
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
        "valid, with a status code of" - {
          "internal server error" in {
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
                    .withStatus(INTERNAL_SERVER_ERROR)
                )
            )
            val request = SubscriptionUpdateService.Requests.Request(
              id = "a7405c8d-06ee-46a3-b5a0-5d65176360ed",
              name = Some("Harold Winter"),
              contacts = Seq(
                SubscriptionService.Requests.Individual(
                  firstName = "",
                  middleName = Some("John"),
                  lastName = "",
                  landline = Some("747663966"),
                  mobile = Some("38390756243"),
                  emailAddress = "Patrick.Dyson@example.com"
                ),
                SubscriptionService.Requests.Organisation(
                  name = "Dyson",
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
                SubscriptionService.Requests.Individual(
                  firstName = "",
                  middleName = Some("John"),
                  lastName = "",
                  landline = Some("747663966"),
                  mobile = Some("38390756243"),
                  emailAddress = "Patrick.Dyson@example.com"
                ),
                SubscriptionService.Requests.Organisation(
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
        "invalid, with an error code of:" in {
          val errorCodes = Seq(
            "invalid-name",
            "invalid-contact-1-type",
            "invalid-contact-1-name",
            "invalid-contact-1-first-name",
            "invalid-contact-1-middle-name",
            "invalid-contact-1-last-name",
            "invalid-contact-1-landline",
            "invalid-contact-1-mobile",
            "invalid-contact-1-email-address",
            "invalid-contact-2-type",
            "invalid-contact-2-name",
            "invalid-contact-2-first-name",
            "invalid-contact-2-middle-name",
            "invalid-contact-2-last-name",
            "invalid-contact-2-landline",
            "invalid-contact-2-mobile",
            "invalid-contact-2-email-address"
          )
          errorCodes.foreach { errorCode: String =>
            info(s"$BAD_REQUEST -> $errorCode")
            stubFor(
              post(urlEqualTo(s"$connectorPath/a7405c8d-06ee-46a3-b5a0-5d65176360ed"))
                .withRequestBody(equalToJson(
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
                    |""".stripMargin))
                .willReturn(
                  aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withStatus(BAD_REQUEST)
                    .withBody(
                      s"""
                         |[
                         |  {
                         |    "code": "$errorCode"
                         |  }
                         |]
                         |""".stripMargin)
                )
            )
            val request = SubscriptionUpdateService.Requests.Request(
              id = "a7405c8d-06ee-46a3-b5a0-5d65176360ed",
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
                SubscriptionService.Requests.Organisation(
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
                  Error(errorCode)
                )
              )
            )
            verifyThatDownstreamApiWasCalled()
          }
        }
        "invalid, with a status code of:" in {
          val statusCodes = Map[Int, String](
            INTERNAL_SERVER_ERROR -> "eis-returned-internal-server-error",
            SERVICE_UNAVAILABLE   -> "eis-returned-service-unavailable",
            CONFLICT              -> "eis-returned-conflict",
            UNAUTHORIZED          -> "eis-returned-unauthorized",
            FORBIDDEN             -> "eis-returned-forbidden"
          )
          statusCodes.keySet.foreach { statusCode: Int =>
            val errorCode = statusCodes.getOrElse(statusCode, throw new NoSuchElementException)
            info(s"$statusCode -> $errorCode")
            stubFor(
              post(urlEqualTo(s"$connectorPath/a7405c8d-06ee-46a3-b5a0-5d65176360ed"))
                .withRequestBody(equalToJson(
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
                    |""".stripMargin))
                .willReturn(
                  aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withStatus(statusCode)
                    .withBody(s"""
                        |[
                        |  {
                        |    "code": "$errorCode"
                        |  }
                        |]
                        |""".stripMargin)
                )
            )
            val request = SubscriptionUpdateService.Requests.Request(
              id = "a7405c8d-06ee-46a3-b5a0-5d65176360ed",
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
                SubscriptionService.Requests.Organisation(
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
                statusCode,
                Seq(
                  Error(errorCode)
                )
              )
            )
            verifyThatDownstreamApiWasCalled()
          }
        }
      }
    }
  }
}
