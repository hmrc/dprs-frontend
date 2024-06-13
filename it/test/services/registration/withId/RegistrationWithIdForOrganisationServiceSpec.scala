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
import connectors.registration.withId.RegistrationWithIdForOrganisationConnector
import play.api.http.Status._
import services.BaseBackendConnectorSpec
import services.BaseService.{Responses => CommonResponses}
import services.registration.BaseRegistrationService.{Responses => CommonRegistrationResponses}
import services.registration.withId.BaseRegistrationWithIdService.{Responses => CommonRegistrationWithIdResponses}
import services.registration.withId.RegistrationWithIdForOrganisationService.Requests.Request
import services.registration.withId.RegistrationWithIdForOrganisationService.Responses.Response
import services.registration.withId.RegistrationWithIdForOrganisationService.{Requests, Responses}

class RegistrationWithIdForOrganisationServiceSpec extends BaseBackendConnectorSpec {

  private val connectorPath: String = RegistrationWithIdForOrganisationConnector.connectorPath
  private lazy val service          = app.injector.instanceOf[RegistrationWithIdForOrganisationService]

  "attempting to register with an ID, as an organisation, when" - {
    "the response from the connector" - {
      "succeeds" in {
        stubFor(
          post(urlEqualTo(connectorPath))
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

        val request = Request(
          id = Requests.Id(
            idType = Requests.IdType.UTR,
            value = "1234567890"
          ),
          name = "Dyson",
          _type = Requests.Type.CorporateBody
        )

        val response = await(service.call(request))

        response shouldBe Right(
          Some(
            Response(
              ids = Seq(
                CommonRegistrationResponses.Id(CommonRegistrationResponses.IdType.ARN, "WARN1442450"),
                CommonRegistrationResponses.Id(CommonRegistrationResponses.IdType.SAFE, "XE0000586571722"),
                CommonRegistrationResponses.Id(CommonRegistrationResponses.IdType.SAP, "8231791429")
              ),
              name = "Dyson",
              _type = Responses.Type.CorporateBody,
              address = CommonRegistrationWithIdResponses.Address(lineOne = "2627 Gus Hill",
                                                                  lineTwo = Some("Apt. 898"),
                                                                  lineThree = Some(""),
                                                                  lineFour = Some("West Corrinamouth"),
                                                                  postalCode = "OX2 3HD",
                                                                  countryCode = "AD"
              ),
              contactDetails = CommonRegistrationWithIdResponses.ContactDetails(landline = Some("176905117"),
                                                                                mobile = Some("62281724761"),
                                                                                fax = Some("08959633679"),
                                                                                emailAddress = Some("edward.goodenough@example.com")
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

            val request = Request(
              id = Requests.Id(
                idType = Requests.IdType.UTR,
                value = "1234567890"
              ),
              name = "Dyson",
              _type = Requests.Type.CorporateBody
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

            val request = Request(
              id = Requests.Id(
                idType = Requests.IdType.UTR,
                value = "1234567890"
              ),
              name = "Dyson",
              _type = Requests.Type.CorporateBody
            )

            val response = await(service.call(request))

            response shouldBe Left(
              CommonResponses.Errors(
                BAD_REQUEST,
                Seq(
                  CommonResponses.Error("invalid-name"),
                  CommonResponses.Error("invalid-type")
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
                                |    "code": "eis-returned-internal-error"
                                |  }
                                |]
                                |""".stripMargin)
                )
            )

            val request = Request(
              id = Requests.Id(
                idType = Requests.IdType.UTR,
                value = "1234567890"
              ),
              name = "Dyson",
              _type = Requests.Type.CorporateBody
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
                                |    "code": "eis-returned-could-not-be-processed"
                                |  }
                                |]
                                |""".stripMargin)
                )
            )

            val request = Request(
              id = Requests.Id(
                idType = Requests.IdType.UTR,
                value = "1234567890"
              ),
              name = "Dyson",
              _type = Requests.Type.CorporateBody
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
                    .withStatus(CONFLICT)
                    .withBody("""
                                |[
                                |  {
                                |    "code": "eis-returned-duplicate submission"
                                |  }
                                |]
                                |""".stripMargin)
                )
            )

            val request = Request(
              id = Requests.Id(
                idType = Requests.IdType.UTR,
                value = "1234567890"
              ),
              name = "Dyson",
              _type = Requests.Type.CorporateBody
            )

            val response = await(service.call(request))

            response shouldBe Left(
              CommonResponses.Errors(
                CONFLICT,
                Seq(
                  CommonResponses.Error("eis-returned-duplicate submission")
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
                idType = Requests.IdType.UTR,
                value = "1234567890"
              ),
              name = "Dyson",
              _type = Requests.Type.CorporateBody
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
                idType = Requests.IdType.UTR,
                value = "1234567890"
              ),
              name = "Dyson",
              _type = Requests.Type.CorporateBody
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
                idType = Requests.IdType.UTR,
                value = "1234567890"
              ),
              name = "Dyson",
              _type = Requests.Type.CorporateBody
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
