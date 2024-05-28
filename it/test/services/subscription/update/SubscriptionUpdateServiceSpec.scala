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
import connectors.subscription.update.SubscriptionUpdateConnector
import play.api.http.Status._
import services.BaseService.Responses.Error
import services.subscription.SubscriptionService
import services.{BaseBackendConnectorSpec, BaseService}

class SubscriptionUpdateServiceSpec extends BaseBackendConnectorSpec {

  private val connectorPath: String = SubscriptionUpdateConnector.connectorPath
  private lazy val service          = app.injector.instanceOf[SubscriptionUpdateService]

  val id = "a7405c8d-06ee-46a3-b5a0-5d65176360ed"

  "attempting to update a subscription, when" - {
    "the response from the connector" - {
      "succeeds, when" - {
        "there are two contacts, one of each type" in {
          stubFor(
            post(urlEqualTo(s"$connectorPath/$id"))
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
            id = id,
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

          response shouldBe Right(SubscriptionService.Responses.Response("OK"))
          verifyThatDownstreamApiWasCalled()
        }

        "there is only one contact, an individual" in {
          stubFor(
            post(urlEqualTo(s"$connectorPath/$id"))
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
            id = id,
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

          response shouldBe Right(SubscriptionService.Responses.Response("OK"))
          verifyThatDownstreamApiWasCalled()
        }

        "there is only one contact, an organisation" in {
          stubFor(
            post(urlEqualTo(s"$connectorPath/$id"))
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
            id = id,
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

          response shouldBe Right(SubscriptionService.Responses.Response("OK"))
          verifyThatDownstreamApiWasCalled()
        }
      }

      "fails, where the response body is" - {
        "absent, with a status code of" - {
          "service unavailable" in {
            stubFor(
              post(urlEqualTo(s"$connectorPath/$id"))
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
                        |    "code": "eis-returned-service-unavailable"
                        |  }
                        |]
                        |""".stripMargin)
                )
            )

            val request = SubscriptionUpdateService.Requests.Request(
              id = id,
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
                SERVICE_UNAVAILABLE,
                Seq(
                  Error("eis-returned-service-unavailable")
                )
              )
            )
            verifyThatDownstreamApiWasCalled()
          }
        }

        "valid, with a status code of" - {
          "internal server error" in {
            stubFor(
              post(urlEqualTo(s"$connectorPath/$id"))
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
              id = id,
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
              post(urlEqualTo(s"$connectorPath/$id"))
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
              id = id,
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

        "invalid, specifically:" - {
          "the name is" - {
            "Blank" in {
              stubFor(
                post(urlEqualTo(s"$connectorPath/$id"))
                  .withRequestBody(equalToJson("""
                      |{
                      |    "name": "",
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
                      .withBody("""
                          |[
                          |  {
                          |    "code": "invalid-name"
                          |  }
                          |]
                          |""".stripMargin)
                  )
              )

              val request = SubscriptionUpdateService.Requests.Request(
                id = id,
                name = Some(""),
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
                    Error("invalid-name")
                  )
                )
              )
              verifyThatDownstreamApiWasCalled()
            }

            "too long" in {
              stubFor(
                post(urlEqualTo(s"$connectorPath/$id"))
                  .withRequestBody(equalToJson("""
                      |{
                      |    "name": "Harold Winter, III, Earl Of East Mountain & North River, Duke Of South Wales, Phd",
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
                      .withBody("""
                          |[
                          |  {
                          |    "code": "invalid-name"
                          |  }
                          |]
                          |""".stripMargin)
                  )
              )

              val request = SubscriptionUpdateService.Requests.Request(
                id = id,
                name = Some("Harold Winter, III, Earl Of East Mountain & North River, Duke Of South Wales, Phd"),
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
                    Error("invalid-name")
                  )
                )
              )
              verifyThatDownstreamApiWasCalled()
            }
          }

          "it contains no contacts" in {
            stubFor(
              post(urlEqualTo(s"$connectorPath/$id"))
                .withRequestBody(equalToJson("""
                    |{
                    |    "name": "Harold Winter",
                    |    "contacts": [
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
                        |    "code": "invalid-number-of-contacts"
                        |  }
                        |]
                        |""".stripMargin)
                )
            )

            val request = SubscriptionUpdateService.Requests.Request(
              id = id,
              name = Some("Harold Winter"),
              contacts = Seq()
            )

            val response = await(service.call(request))

            response shouldBe Left(
              BaseService.Responses.Errors(
                BAD_REQUEST,
                Seq(
                  Error("invalid-number-of-contacts")
                )
              )
            )
            verifyThatDownstreamApiWasCalled()
          }

          "it contains three contacts" in {
            stubFor(
              post(urlEqualTo(s"$connectorPath/$id"))
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
                    |        },
                    |        {
                    |            "type": "I",
                    |            "firstName": "Patricia",
                    |            "middleName": "Jane",
                    |            "lastName": "Dyson",
                    |            "landline": "747663967",
                    |            "mobile": "38390756244",
                    |            "emailAddress": "Patricia.Dyson@example.com"
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
                        |    "code": "invalid-number-of-contacts"
                        |  }
                        |]
                        |""".stripMargin)
                )
            )

            val request = SubscriptionUpdateService.Requests.Request(
              id = id,
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
                ),
                SubscriptionService.Requests.Individual(
                  firstName = "Patricia",
                  middleName = Some("Jane"),
                  lastName = "Dyson",
                  landline = Some("747663967"),
                  mobile = Some("38390756244"),
                  emailAddress = "Patricia.Dyson@example.com"
                )
              )
            )

            val response = await(service.call(request))

            response shouldBe Left(
              BaseService.Responses.Errors(
                BAD_REQUEST,
                Seq(
                  Error("invalid-number-of-contacts")
                )
              )
            )
            verifyThatDownstreamApiWasCalled()
          }

          "it contains two contacts, one has a type which is" - {
            "an individual, where" - {
              "the first name is" - {
                "blank" in {
                  stubFor(
                    post(urlEqualTo(s"$connectorPath/$id"))
                      .withRequestBody(equalToJson("""
                          |{
                          |    "name": "Harold Winter",
                          |    "contacts": [
                          |        {
                          |            "type": "I",
                          |            "firstName": "",
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
                          .withBody("""
                              |[
                              |  {
                              |    "code": "invalid-contact-1-first-name"
                              |  }
                              |]
                              |""".stripMargin)
                      )
                  )

                  val request = SubscriptionUpdateService.Requests.Request(
                    id = id,
                    name = Some("Harold Winter"),
                    contacts = Seq(
                      SubscriptionService.Requests.Individual(
                        firstName = "",
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
                        Error("invalid-contact-1-first-name")
                      )
                    )
                  )
                  verifyThatDownstreamApiWasCalled()
                }

                "too long" in {
                  stubFor(
                    post(urlEqualTo(s"$connectorPath/$id"))
                      .withRequestBody(equalToJson("""
                          |{
                          |    "name": "Harold Winter",
                          |    "contacts": [
                          |        {
                          |            "type": "I",
                          |            "firstName": "Patrick Alexander John Fitzpatrick James",
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
                          .withBody("""
                              |[
                              |  {
                              |    "code": "invalid-contact-1-first-name"
                              |  }
                              |]
                              |""".stripMargin)
                      )
                  )

                  val request = SubscriptionUpdateService.Requests.Request(
                    id = id,
                    name = Some("Harold Winter"),
                    contacts = Seq(
                      SubscriptionService.Requests.Individual(
                        firstName = "Patrick Alexander John Fitzpatrick James",
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
                        Error("invalid-contact-1-first-name")
                      )
                    )
                  )
                  verifyThatDownstreamApiWasCalled()
                }
              }

              "the middle name is" - {
                "blank" in {
                  stubFor(
                    post(urlEqualTo(s"$connectorPath/$id"))
                      .withRequestBody(equalToJson("""
                          |{
                          |    "name": "Harold Winter",
                          |    "contacts": [
                          |        {
                          |            "type": "I",
                          |            "firstName": "Patrick",
                          |            "middleName": "",
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
                          .withBody("""
                              |[
                              |  {
                              |    "code": "invalid-contact-1-middle-name"
                              |  }
                              |]
                              |""".stripMargin)
                      )
                  )

                  val request = SubscriptionUpdateService.Requests.Request(
                    id = id,
                    name = Some("Harold Winter"),
                    contacts = Seq(
                      SubscriptionService.Requests.Individual(
                        firstName = "Patrick",
                        middleName = Some(""),
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
                        Error("invalid-contact-1-middle-name")
                      )
                    )
                  )
                  verifyThatDownstreamApiWasCalled()
                }

                "too long" in {
                  stubFor(
                    post(urlEqualTo(s"$connectorPath/$id"))
                      .withRequestBody(equalToJson("""
                          |{
                          |    "name": "Harold Winter",
                          |    "contacts": [
                          |        {
                          |            "type": "I",
                          |            "firstName": "Patrick",
                          |            "middleName": "John Alexander John Fitzpatrick James",
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
                          .withBody("""
                              |[
                              |  {
                              |    "code": "invalid-contact-1-middle-name"
                              |  }
                              |]
                              |""".stripMargin)
                      )
                  )

                  val request = SubscriptionUpdateService.Requests.Request(
                    id = id,
                    name = Some("Harold Winter"),
                    contacts = Seq(
                      SubscriptionService.Requests.Individual(
                        firstName = "Patrick",
                        middleName = Some("John Alexander John Fitzpatrick James"),
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
                        Error("invalid-contact-1-middle-name")
                      )
                    )
                  )
                  verifyThatDownstreamApiWasCalled()
                }
              }

              "the last name is" - {
                "blank" in {
                  stubFor(
                    post(urlEqualTo(s"$connectorPath/$id"))
                      .withRequestBody(equalToJson("""
                          |{
                          |    "name": "Harold Winter",
                          |    "contacts": [
                          |        {
                          |            "type": "I",
                          |            "firstName": "Patrick",
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
                              |    "code": "invalid-contact-1-last-name"
                              |  }
                              |]
                              |""".stripMargin)
                      )
                  )

                  val request = SubscriptionUpdateService.Requests.Request(
                    id = id,
                    name = Some("Harold Winter"),
                    contacts = Seq(
                      SubscriptionService.Requests.Individual(
                        firstName = "Patrick",
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
                      BAD_REQUEST,
                      Seq(
                        Error("invalid-contact-1-last-name")
                      )
                    )
                  )
                  verifyThatDownstreamApiWasCalled()
                }

                "too long" in {
                  stubFor(
                    post(urlEqualTo(s"$connectorPath/$id"))
                      .withRequestBody(equalToJson("""
                          |{
                          |    "name": "Harold Winter",
                          |    "contacts": [
                          |        {
                          |            "type": "I",
                          |            "firstName": "Patrick",
                          |            "middleName": "John",
                          |            "lastName": "Dyson Alexander John Fitzpatrick James",
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
                              |    "code": "invalid-contact-1-last-name"
                              |  }
                              |]
                              |""".stripMargin)
                      )
                  )

                  val request = SubscriptionUpdateService.Requests.Request(
                    id = id,
                    name = Some("Harold Winter"),
                    contacts = Seq(
                      SubscriptionService.Requests.Individual(
                        firstName = "Patrick",
                        middleName = Some("John"),
                        lastName = "Dyson Alexander John Fitzpatrick James",
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
                        Error("invalid-contact-1-last-name")
                      )
                    )
                  )
                  verifyThatDownstreamApiWasCalled()
                }
              }

              "the landline is" - {
                "blank" in {
                  stubFor(
                    post(urlEqualTo(s"$connectorPath/$id"))
                      .withRequestBody(equalToJson("""
                          |{
                          |    "name": "Harold Winter",
                          |    "contacts": [
                          |        {
                          |            "type": "I",
                          |            "firstName": "Patrick",
                          |            "middleName": "John",
                          |            "lastName": "Dyson",
                          |            "landline": "",
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
                              |    "code": "invalid-contact-1-landline"
                              |  }
                              |]
                              |""".stripMargin)
                      )
                  )

                  val request = SubscriptionUpdateService.Requests.Request(
                    id = id,
                    name = Some("Harold Winter"),
                    contacts = Seq(
                      SubscriptionService.Requests.Individual(
                        firstName = "Patrick",
                        middleName = Some("John"),
                        lastName = "Dyson",
                        landline = Some(""),
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
                        Error("invalid-contact-1-landline")
                      )
                    )
                  )
                  verifyThatDownstreamApiWasCalled()
                }

                "too long" in {
                  stubFor(
                    post(urlEqualTo(s"$connectorPath/$id"))
                      .withRequestBody(equalToJson("""
                          |{
                          |    "name": "Harold Winter",
                          |    "contacts": [
                          |        {
                          |            "type": "I",
                          |            "firstName": "Patrick",
                          |            "middleName": "John",
                          |            "lastName": "Dyson",
                          |            "landline": "747663966747663966747663966",
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
                              |    "code": "invalid-contact-1-landline"
                              |  }
                              |]
                              |""".stripMargin)
                      )
                  )

                  val request = SubscriptionUpdateService.Requests.Request(
                    id = id,
                    name = Some("Harold Winter"),
                    contacts = Seq(
                      SubscriptionService.Requests.Individual(
                        firstName = "Patrick",
                        middleName = Some("John"),
                        lastName = "Dyson",
                        landline = Some("747663966747663966747663966"),
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
                        Error("invalid-contact-1-landline")
                      )
                    )
                  )
                  verifyThatDownstreamApiWasCalled()
                }

                "of an invalid format" in {
                  stubFor(
                    post(urlEqualTo(s"$connectorPath/$id"))
                      .withRequestBody(equalToJson("""
                          |{
                          |    "name": "Harold Winter",
                          |    "contacts": [
                          |        {
                          |            "type": "I",
                          |            "firstName": "Patrick",
                          |            "middleName": "John",
                          |            "lastName": "Dyson",
                          |            "landline": "£747663966",
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
                              |    "code": "invalid-contact-1-landline"
                              |  }
                              |]
                              |""".stripMargin)
                      )
                  )

                  val request = SubscriptionUpdateService.Requests.Request(
                    id = id,
                    name = Some("Harold Winter"),
                    contacts = Seq(
                      SubscriptionService.Requests.Individual(
                        firstName = "Patrick",
                        middleName = Some("John"),
                        lastName = "Dyson",
                        landline = Some("£747663966"),
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
                        Error("invalid-contact-1-landline")
                      )
                    )
                  )
                  verifyThatDownstreamApiWasCalled()
                }
              }

              "the mobile is" - {
                "blank" in {
                  stubFor(
                    post(urlEqualTo(s"$connectorPath/$id"))
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
                          |            "mobile": "",
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
                              |    "code": "invalid-contact-1-mobile"
                              |  }
                              |]
                              |""".stripMargin)
                      )
                  )

                  val request = SubscriptionUpdateService.Requests.Request(
                    id = id,
                    name = Some("Harold Winter"),
                    contacts = Seq(
                      SubscriptionService.Requests.Individual(
                        firstName = "Patrick",
                        middleName = Some("John"),
                        lastName = "Dyson",
                        landline = Some("747663966"),
                        mobile = Some(""),
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
                        Error("invalid-contact-1-mobile")
                      )
                    )
                  )
                  verifyThatDownstreamApiWasCalled()
                }

                "too long" in {
                  stubFor(
                    post(urlEqualTo(s"$connectorPath/$id"))
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
                          |            "mobile": "38390756243383907562433839",
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
                              |    "code": "invalid-contact-1-mobile"
                              |  }
                              |]
                              |""".stripMargin)
                      )
                  )

                  val request = SubscriptionUpdateService.Requests.Request(
                    id = id,
                    name = Some("Harold Winter"),
                    contacts = Seq(
                      SubscriptionService.Requests.Individual(
                        firstName = "Patrick",
                        middleName = Some("John"),
                        lastName = "Dyson",
                        landline = Some("747663966"),
                        mobile = Some("38390756243383907562433839"),
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
                        Error("invalid-contact-1-mobile")
                      )
                    )
                  )
                  verifyThatDownstreamApiWasCalled()
                }

                "of an invalid format" in {
                  stubFor(
                    post(urlEqualTo(s"$connectorPath/$id"))
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
                          |            "mobile": "£38390756243",
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
                              |    "code": "invalid-contact-1-mobile"
                              |  }
                              |]
                              |""".stripMargin)
                      )
                  )

                  val request = SubscriptionUpdateService.Requests.Request(
                    id = id,
                    name = Some("Harold Winter"),
                    contacts = Seq(
                      SubscriptionService.Requests.Individual(
                        firstName = "Patrick",
                        middleName = Some("John"),
                        lastName = "Dyson",
                        landline = Some("747663966"),
                        mobile = Some("£38390756243"),
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
                        Error("invalid-contact-1-mobile")
                      )
                    )
                  )
                  verifyThatDownstreamApiWasCalled()
                }
              }

              "the email address is" - {
                "blank" in {
                  stubFor(
                    post(urlEqualTo(s"$connectorPath/$id"))
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
                          |            "mobile": "48390756243",
                          |            "emailAddress": ""
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
                              |    "code": "invalid-contact-1-email-address"
                              |  }
                              |]
                              |""".stripMargin)
                      )
                  )

                  val request = SubscriptionUpdateService.Requests.Request(
                    id = id,
                    name = Some("Harold Winter"),
                    contacts = Seq(
                      SubscriptionService.Requests.Individual(
                        firstName = "Patrick",
                        middleName = Some("John"),
                        lastName = "Dyson",
                        landline = Some("747663966"),
                        mobile = Some("48390756243"),
                        emailAddress = ""
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
                        Error("invalid-contact-1-email-address")
                      )
                    )
                  )
                  verifyThatDownstreamApiWasCalled()
                }

                "too long" in {
                  stubFor(
                    post(urlEqualTo(s"$connectorPath/$id"))
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
                          |            "mobile": "48390756243",
                          |            "emailAddress": "loremipsumdolorsitametconsetetursadipscingelitrseddiamnonumyeirmodtemporinviduntutlaboreetdoloremagnLoremipsumdolorsisum@example.com"
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
                              |    "code": "invalid-contact-1-email-address"
                              |  }
                              |]
                              |""".stripMargin)
                      )
                  )

                  val request = SubscriptionUpdateService.Requests.Request(
                    id = id,
                    name = Some("Harold Winter"),
                    contacts = Seq(
                      SubscriptionService.Requests.Individual(
                        firstName = "Patrick",
                        middleName = Some("John"),
                        lastName = "Dyson",
                        landline = Some("747663966"),
                        mobile = Some("48390756243"),
                        emailAddress =
                          "loremipsumdolorsitametconsetetursadipscingelitrseddiamnonumyeirmodtemporinviduntutlaboreetdoloremagnLoremipsumdolorsisum@example.com"
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
                        Error("invalid-contact-1-email-address")
                      )
                    )
                  )
                  verifyThatDownstreamApiWasCalled()
                }

                "of an invalid format" in {
                  stubFor(
                    post(urlEqualTo(s"$connectorPath/$id"))
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
                          |            "emailAddress": "@example.com"
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
                              |    "code": "invalid-contact-1-email-address"
                              |  }
                              |]
                              |""".stripMargin)
                      )
                  )

                  val request = SubscriptionUpdateService.Requests.Request(
                    id = id,
                    name = Some("Harold Winter"),
                    contacts = Seq(
                      SubscriptionService.Requests.Individual(
                        firstName = "Patrick",
                        middleName = Some("John"),
                        lastName = "Dyson",
                        landline = Some("747663966"),
                        mobile = Some("38390756243"),
                        emailAddress = "@example.com"
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
                        Error("invalid-contact-1-email-address")
                      )
                    )
                  )
                  verifyThatDownstreamApiWasCalled()
                }
              }
            }

            "an organisation, where" - {
              "the name is" - {
                "blank" in {
                  stubFor(
                    post(urlEqualTo(s"$connectorPath/$id"))
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
                          |            "name": "",
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
                              |    "code": "invalid-contact-2-name"
                              |  }
                              |]
                              |""".stripMargin)
                      )
                  )

                  val request = SubscriptionUpdateService.Requests.Request(
                    id = id,
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
                        name = "",
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
                        Error("invalid-contact-2-name")
                      )
                    )
                  )
                  verifyThatDownstreamApiWasCalled()
                }

                "too long" in {
                  stubFor(
                    post(urlEqualTo(s"$connectorPath/$id"))
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
                          |            "name": "The Dyson Electronics Company Of Great Britain And Northern Ireland (aka The Dyson Electronics Company Of Great Britain And Northern Ireland)",
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
                              |    "code": "invalid-contact-2-name"
                              |  }
                              |]
                              |""".stripMargin)
                      )
                  )

                  val request = SubscriptionUpdateService.Requests.Request(
                    id = id,
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
                        name =
                          "The Dyson Electronics Company Of Great Britain And Northern Ireland (aka The Dyson Electronics Company Of Great Britain And Northern Ireland)",
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
                        Error("invalid-contact-2-name")
                      )
                    )
                  )
                  verifyThatDownstreamApiWasCalled()
                }
              }

              "the landline is" - {
                "blank" in {
                  stubFor(
                    post(urlEqualTo(s"$connectorPath/$id"))
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
                          |            "landline": "",
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
                              |    "code": "invalid-contact-2-landline"
                              |  }
                              |]
                              |""".stripMargin)
                      )
                  )

                  val request = SubscriptionUpdateService.Requests.Request(
                    id = id,
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
                        landline = Some(""),
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
                        Error("invalid-contact-2-landline")
                      )
                    )
                  )
                  verifyThatDownstreamApiWasCalled()
                }

                "too long" in {
                  stubFor(
                    post(urlEqualTo(s"$connectorPath/$id"))
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
                          |            "landline": "747663966747663966747663966",
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
                              |    "code": "invalid-contact-2-landline"
                              |  }
                              |]
                              |""".stripMargin)
                      )
                  )

                  val request = SubscriptionUpdateService.Requests.Request(
                    id = id,
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
                        landline = Some("747663966747663966747663966"),
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
                        Error("invalid-contact-2-landline")
                      )
                    )
                  )
                  verifyThatDownstreamApiWasCalled()
                }

                "of an invalid format" in {
                  stubFor(
                    post(urlEqualTo(s"$connectorPath/$id"))
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
                          |            "landline": "£847663966",
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
                              |    "code": "invalid-contact-2-landline"
                              |  }
                              |]
                              |""".stripMargin)
                      )
                  )

                  val request = SubscriptionUpdateService.Requests.Request(
                    id = id,
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
                        landline = Some("£847663966"),
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
                        Error("invalid-contact-2-landline")
                      )
                    )
                  )
                  verifyThatDownstreamApiWasCalled()
                }
              }

              "the mobile is" - {
                "blank" in {
                  stubFor(
                    post(urlEqualTo(s"$connectorPath/$id"))
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
                          |            "mobile": "",
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
                              |    "code": "invalid-contact-2-mobile"
                              |  }
                              |]
                              |""".stripMargin)
                      )
                  )

                  val request = SubscriptionUpdateService.Requests.Request(
                    id = id,
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
                        mobile = Some(""),
                        emailAddress = "info@example.com"
                      )
                    )
                  )

                  val response = await(service.call(request))

                  response shouldBe Left(
                    BaseService.Responses.Errors(
                      BAD_REQUEST,
                      Seq(
                        Error("invalid-contact-2-mobile")
                      )
                    )
                  )
                  verifyThatDownstreamApiWasCalled()
                }

                "too long" in {
                  stubFor(
                    post(urlEqualTo(s"$connectorPath/$id"))
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
                          |            "mobile": "38390756243383907562433839",
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
                              |    "code": "invalid-contact-2-mobile"
                              |  }
                              |]
                              |""".stripMargin)
                      )
                  )

                  val request = SubscriptionUpdateService.Requests.Request(
                    id = id,
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
                        mobile = Some("38390756243383907562433839"),
                        emailAddress = "info@example.com"
                      )
                    )
                  )

                  val response = await(service.call(request))

                  response shouldBe Left(
                    BaseService.Responses.Errors(
                      BAD_REQUEST,
                      Seq(
                        Error("invalid-contact-2-mobile")
                      )
                    )
                  )
                  verifyThatDownstreamApiWasCalled()
                }

                "of an invalid format" in {
                  stubFor(
                    post(urlEqualTo(s"$connectorPath/$id"))
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
                          |            "mobile": "£48390756243",
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
                              |    "code": "invalid-contact-2-mobile"
                              |  }
                              |]
                              |""".stripMargin)
                      )
                  )

                  val request = SubscriptionUpdateService.Requests.Request(
                    id = id,
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
                        mobile = Some("£48390756243"),
                        emailAddress = "info@example.com"
                      )
                    )
                  )

                  val response = await(service.call(request))

                  response shouldBe Left(
                    BaseService.Responses.Errors(
                      BAD_REQUEST,
                      Seq(
                        Error("invalid-contact-2-mobile")
                      )
                    )
                  )
                  verifyThatDownstreamApiWasCalled()
                }
              }

              "the email address is" - {
                "blank" in {
                  stubFor(
                    post(urlEqualTo(s"$connectorPath/$id"))
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
                          |            "mobile": "48390756243",
                          |            "emailAddress": "Patrick.Dyson@example.com"
                          |        },
                          |        {
                          |            "type": "O",
                          |            "name": "Dyson",
                          |            "landline": "847663966",
                          |            "mobile": "48390756243",
                          |            "emailAddress": ""
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
                              |    "code": "invalid-contact-2-email-address"
                              |  }
                              |]
                              |""".stripMargin)
                      )
                  )

                  val request = SubscriptionUpdateService.Requests.Request(
                    id = id,
                    name = Some("Harold Winter"),
                    contacts = Seq(
                      SubscriptionService.Requests.Individual(
                        firstName = "Patrick",
                        middleName = Some("John"),
                        lastName = "Dyson",
                        landline = Some("747663966"),
                        mobile = Some("48390756243"),
                        emailAddress = "Patrick.Dyson@example.com"
                      ),
                      SubscriptionService.Requests.Organisation(
                        name = "Dyson",
                        landline = Some("847663966"),
                        mobile = Some("48390756243"),
                        emailAddress = ""
                      )
                    )
                  )

                  val response = await(service.call(request))

                  response shouldBe Left(
                    BaseService.Responses.Errors(
                      BAD_REQUEST,
                      Seq(
                        Error("invalid-contact-2-email-address")
                      )
                    )
                  )
                  verifyThatDownstreamApiWasCalled()
                }

                "too long" in {
                  stubFor(
                    post(urlEqualTo(s"$connectorPath/$id"))
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
                          |            "mobile": "48390756243",
                          |            "emailAddress": "Patrick.Dyson@example.com"
                          |        },
                          |        {
                          |            "type": "O",
                          |            "name": "Dyson",
                          |            "landline": "847663966",
                          |            "mobile": "48390756243",
                          |            "emailAddress": "loremipsumdolorsitametconsetetursadipscingelitrseddiamnonumyeirmodtemporinviduntutlaboreetdoloremagnLoremipsumdolorsisum@example.com"
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
                              |    "code": "invalid-contact-2-email-address"
                              |  }
                              |]
                              |""".stripMargin)
                      )
                  )

                  val request = SubscriptionUpdateService.Requests.Request(
                    id = id,
                    name = Some("Harold Winter"),
                    contacts = Seq(
                      SubscriptionService.Requests.Individual(
                        firstName = "Patrick",
                        middleName = Some("John"),
                        lastName = "Dyson",
                        landline = Some("747663966"),
                        mobile = Some("48390756243"),
                        emailAddress = "Patrick.Dyson@example.com"
                      ),
                      SubscriptionService.Requests.Organisation(
                        name = "Dyson",
                        landline = Some("847663966"),
                        mobile = Some("48390756243"),
                        emailAddress =
                          "loremipsumdolorsitametconsetetursadipscingelitrseddiamnonumyeirmodtemporinviduntutlaboreetdoloremagnLoremipsumdolorsisum@example.com"
                      )
                    )
                  )

                  val response = await(service.call(request))

                  response shouldBe Left(
                    BaseService.Responses.Errors(
                      BAD_REQUEST,
                      Seq(
                        Error("invalid-contact-2-email-address")
                      )
                    )
                  )
                  verifyThatDownstreamApiWasCalled()
                }

                "of an invalid format" in {
                  stubFor(
                    post(urlEqualTo(s"$connectorPath/$id"))
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
                          |            "emailAddress": "@example.com"
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
                              |    "code": "invalid-contact-2-email-address"
                              |  }
                              |]
                              |""".stripMargin)
                      )
                  )

                  val request = SubscriptionUpdateService.Requests.Request(
                    id = id,
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
                        emailAddress = "@example.com"
                      )
                    )
                  )

                  val response = await(service.call(request))

                  response shouldBe Left(
                    BaseService.Responses.Errors(
                      BAD_REQUEST,
                      Seq(
                        Error("invalid-contact-2-email-address")
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
    }
  }
}
