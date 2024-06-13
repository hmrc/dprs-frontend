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

package services.registration.withId

import com.github.tomakehurst.wiremock.client.WireMock._
import connectors.registration.withId.RegistrationWithIdForIndividualConnector
import play.api.http.Status.{BAD_REQUEST, CONFLICT, FORBIDDEN, INTERNAL_SERVER_ERROR, NOT_FOUND, OK, SERVICE_UNAVAILABLE, UNAUTHORIZED}
import services.BaseBackendConnectorSpec
import services.BaseService.{Responses => CommonResponses}
import services.registration.BaseRegistrationService.{Responses => CommonRegistrationResponses}
import services.registration.withId.BaseRegistrationWithIdService.{Responses => CommonRegistrationWithIdResponses}
import services.registration.withId.RegistrationWithIdForIndividualService.Requests
import services.registration.withId.RegistrationWithIdForIndividualService.Requests.Request

class RegistrationWithIdForIndividualServiceSpec extends BaseBackendConnectorSpec {

  private val connectorPath: String = RegistrationWithIdForIndividualConnector.connectorPath
  private lazy val service          = app.injector.instanceOf[RegistrationWithIdForIndividualService]

  "attempting to register with an ID, as an individual, when" - {
    "the response from the connector" - {
      "succeeds" in {
        stubFor(
          post(urlEqualTo(connectorPath))
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

        val request = Request(
          id = Requests.Id(
            idType = Requests.IdType.NINO,
            value = "AA000000A"
          ),
          firstName = "Patrick",
          middleName = Some("John"),
          lastName = "Dyson",
          dateOfBirth = "1970-10-04"
        )

        val response = await(service.call(request))

        response shouldBe Right(
          Some(
            RegistrationWithIdForIndividualService.Responses.Response(
              ids = Seq(
                CommonRegistrationResponses.Id(CommonRegistrationResponses.IdType.ARN, "WARN3849921"),
                CommonRegistrationResponses.Id(CommonRegistrationResponses.IdType.SAFE, "XE0000200775706"),
                CommonRegistrationResponses.Id(CommonRegistrationResponses.IdType.SAP, "1960629967")
              ),
              firstName = "Patrick",
              middleName = Some("John"),
              lastName = "Dyson",
              dateOfBirth = Some("1970-10-04"),
              address = CommonRegistrationWithIdResponses.Address(lineOne = "26424 Cecelia Junction",
                                                                  lineTwo = Some("Suite 858"),
                                                                  lineThree = Some(""),
                                                                  lineFour = Some("West Siobhanberg"),
                                                                  postalCode = "OX2 3HD",
                                                                  countryCode = "AD"
              ),
              contactDetails = CommonRegistrationWithIdResponses.ContactDetails(landline = Some("747663966"),
                                                                                mobile = Some("38390756243"),
                                                                                fax = Some("58371813020"),
                                                                                emailAddress = Some("Patrick.Dyson@example.com")
              )
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

            val request = Request(
              id = Requests.Id(
                idType = Requests.IdType.NINO,
                value = "AA000000A"
              ),
              firstName = "Patrick",
              middleName = Some("John"),
              lastName = "Dyson",
              dateOfBirth = "1970-10-04"
            )

            val response = await(service.call(request))

            response shouldBe Left(CommonResponses.Errors(INTERNAL_SERVER_ERROR))
            verifyThatDownstreamApiWasCalled()
          }
        }
        "valid, with a status code of" - {
          "bad request" in {
            stubFor(
              post(urlEqualTo(connectorPath))
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

            val request = Request(
              id = Requests.Id(
                idType = Requests.IdType.NINO,
                value = "AA000000A"
              ),
              firstName = "Patrick",
              middleName = Some("John"),
              lastName = "",
              dateOfBirth = "10-04-1970"
            )

            val response = await(service.call(request))

            response shouldBe Left(
              CommonResponses.Errors(
                BAD_REQUEST,
                Seq(
                  CommonResponses.Error("invalid-last-name"),
                  CommonResponses.Error("invalid-date-of-birth")
                )
              )
            )
            verifyThatDownstreamApiWasCalled()
          }
          "internal error" in {
            stubFor(
              post(urlEqualTo(connectorPath))
                .withRequestBody(equalToJson("""
                                               |{
                                               |  "id": {
                                               |    "type": "NINO",
                                               |    "value": "AA000000A"
                                               |  },
                                               |  "firstName": "Patrick",
                                               |  "middleName": "John",
                                               |  "lastName": "Dyson",
                                               |  "dateOfBirth": "1970-04-10"
                                               |}
                                               |""".stripMargin))
                .willReturn(
                  aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withStatus(SERVICE_UNAVAILABLE)
                    .withBody("""
                                |[
                                |  {
                                |    "code": "eis-returned-internal-error"
                                |  }
                                |]
                                |""".stripMargin)
                )
            )

            val request = Request(
              id = Requests.Id(
                idType = Requests.IdType.NINO,
                value = "AA000000A"
              ),
              firstName = "Patrick",
              middleName = Some("John"),
              lastName = "Dyson",
              dateOfBirth = "1970-04-10"
            )

            val response = await(service.call(request))

            response shouldBe Left(
              CommonResponses.Errors(
                SERVICE_UNAVAILABLE,
                Seq(
                  CommonResponses.Error("eis-returned-internal-error")
                )
              )
            )
            verifyThatDownstreamApiWasCalled()
          }
          "could not be processed" in {
            stubFor(
              post(urlEqualTo(connectorPath))
                .withRequestBody(equalToJson("""
                                               |{
                                               |  "id": {
                                               |    "type": "NINO",
                                               |    "value": "AA000000A"
                                               |  },
                                               |  "firstName": "Patrick",
                                               |  "middleName": "John",
                                               |  "lastName": "Dyson",
                                               |  "dateOfBirth": "1970-04-10"
                                               |}
                                               |""".stripMargin))
                .willReturn(
                  aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withStatus(SERVICE_UNAVAILABLE)
                    .withBody("""
                                |[
                                |  {
                                |    "code": "eis-returned-could-not-be-processed"
                                |  }
                                |]
                                |""".stripMargin)
                )
            )

            val request = Request(
              id = Requests.Id(
                idType = Requests.IdType.NINO,
                value = "AA000000A"
              ),
              firstName = "Patrick",
              middleName = Some("John"),
              lastName = "Dyson",
              dateOfBirth = "1970-04-10"
            )

            val response = await(service.call(request))

            response shouldBe Left(
              CommonResponses.Errors(
                SERVICE_UNAVAILABLE,
                Seq(
                  CommonResponses.Error("eis-returned-could-not-be-processed")
                )
              )
            )
            verifyThatDownstreamApiWasCalled()
          }
          "duplicate submission" in {
            stubFor(
              post(urlEqualTo(connectorPath))
                .withRequestBody(equalToJson("""
                                               |{
                                               |  "id": {
                                               |    "type": "NINO",
                                               |    "value": "AA000000A"
                                               |  },
                                               |  "firstName": "Patrick",
                                               |  "middleName": "John",
                                               |  "lastName": "Dyson",
                                               |  "dateOfBirth": "1970-04-10"
                                               |}
                                               |""".stripMargin))
                .willReturn(
                  aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withStatus(CONFLICT)
                    .withBody("""
                                |[
                                |  {
                                |    "code": "eis-returned-duplicate-submission"
                                |  }
                                |]
                                |""".stripMargin)
                )
            )

            val request = Request(
              id = Requests.Id(
                idType = Requests.IdType.NINO,
                value = "AA000000A"
              ),
              firstName = "Patrick",
              middleName = Some("John"),
              lastName = "Dyson",
              dateOfBirth = "1970-04-10"
            )

            val response = await(service.call(request))

            response shouldBe Left(
              CommonResponses.Errors(
                CONFLICT,
                Seq(
                  CommonResponses.Error("eis-returned-duplicate-submission")
                )
              )
            )
            verifyThatDownstreamApiWasCalled()
          }
          "forbidden" in {
            stubFor(
              post(urlEqualTo(connectorPath))
                .withRequestBody(equalToJson("""
                                               |{
                                               |  "id": {
                                               |    "type": "NINO",
                                               |    "value": "AA000000A"
                                               |  },
                                               |  "firstName": "Patrick",
                                               |  "middleName": "John",
                                               |  "lastName": "Dyson",
                                               |  "dateOfBirth": "1970-04-10"
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

            val request = Request(
              id = Requests.Id(
                idType = Requests.IdType.NINO,
                value = "AA000000A"
              ),
              firstName = "Patrick",
              middleName = Some("John"),
              lastName = "Dyson",
              dateOfBirth = "1970-04-10"
            )

            val response = await(service.call(request))

            response shouldBe Left(
              CommonResponses.Errors(
                FORBIDDEN,
                Seq(
                  CommonResponses.Error("eis-returned-forbidden")
                )
              )
            )
            verifyThatDownstreamApiWasCalled()
          }
          "no match" in {
            stubFor(
              post(urlEqualTo(connectorPath))
                .withRequestBody(equalToJson("""
                                               |{
                                               |  "id": {
                                               |    "type": "NINO",
                                               |    "value": "AA000000A"
                                               |  },
                                               |  "firstName": "Patrick",
                                               |  "middleName": "John",
                                               |  "lastName": "Dyson",
                                               |  "dateOfBirth": "1970-04-10"
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

            val request = Request(
              id = Requests.Id(
                idType = Requests.IdType.NINO,
                value = "AA000000A"
              ),
              firstName = "Patrick",
              middleName = Some("John"),
              lastName = "Dyson",
              dateOfBirth = "1970-04-10"
            )

            val response = await(service.call(request))

            response shouldBe Left(
              CommonResponses.Errors(
                NOT_FOUND,
                Seq(
                  CommonResponses.Error("eis-returned-not-found")
                )
              )
            )
            verifyThatDownstreamApiWasCalled()
          }
          "unauthorised" in {
            stubFor(
              post(urlEqualTo(connectorPath))
                .withRequestBody(equalToJson("""
                                               |{
                                               |  "id": {
                                               |    "type": "NINO",
                                               |    "value": "AA000000A"
                                               |  },
                                               |  "firstName": "Patrick",
                                               |  "middleName": "John",
                                               |  "lastName": "Dyson",
                                               |  "dateOfBirth": "1970-04-10"
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

            val request = Request(
              id = Requests.Id(
                idType = Requests.IdType.NINO,
                value = "AA000000A"
              ),
              firstName = "Patrick",
              middleName = Some("John"),
              lastName = "Dyson",
              dateOfBirth = "1970-04-10"
            )

            val response = await(service.call(request))

            response shouldBe Left(
              CommonResponses.Errors(
                UNAUTHORIZED,
                Seq(
                  CommonResponses.Error("eis-returned-unauthorised")
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
