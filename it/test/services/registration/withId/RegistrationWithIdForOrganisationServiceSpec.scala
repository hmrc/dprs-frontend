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
import connectors.BaseConnector
import connectors.registration.withId.RegistrationWithIdForOrganisationConnector
import play.api.http.Status.{BAD_REQUEST, INTERNAL_SERVER_ERROR, OK, SERVICE_UNAVAILABLE}
import services.BaseService.Responses.Error
import services.registration.RegistrationService
import services.registration.withId.RegistrationWithIdForOrganisationService.{Requests => ServiceRequests, Responses => ServiceResponses}
import services.registration.withId.RegistrationWithIdService.{Responses => CommonServiceResponses}
import services.{BaseBackendConnectorIntSpec, BaseService}

class RegistrationWithIdForOrganisationServiceSpec extends BaseBackendConnectorIntSpec {

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

        val request = ServiceRequests.Request(
          id = ServiceRequests.Id(
            idType = ServiceRequests.IdType.UTR,
            value = "1234567890"
          ),
          name = "Dyson",
          _type = ServiceRequests.Type.CorporateBody
        )

        val response = await(service.call(request))

        response shouldBe Right(
          ServiceResponses.Response(
            ids = Seq(
              RegistrationService.Responses.Id(RegistrationService.Responses.IdType.ARN, "WARN1442450"),
              RegistrationService.Responses.Id(RegistrationService.Responses.IdType.SAFE, "XE0000586571722"),
              RegistrationService.Responses.Id(RegistrationService.Responses.IdType.SAP, "8231791429")
            ),
            name = "Dyson",
            _type = ServiceResponses.Type.CorporateBody,
            address = CommonServiceResponses.Address(lineOne = "2627 Gus Hill",
                                                     lineTwo = Some("Apt. 898"),
                                                     lineThree = Some(""),
                                                     lineFour = Some("West Corrinamouth"),
                                                     postalCode = "OX2 3HD",
                                                     countryCode = "AD"
            ),
            contactDetails = CommonServiceResponses.ContactDetails(landline = Some("176905117"),
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

            val request = ServiceRequests.Request(
              id = ServiceRequests.Id(
                idType = ServiceRequests.IdType.UTR,
                value = "1234567890"
              ),
              name = "Dyson",
              _type = ServiceRequests.Type.CorporateBody
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

            val request = ServiceRequests.Request(
              id = ServiceRequests.Id(
                idType = ServiceRequests.IdType.UTR,
                value = "1234567890"
              ),
              name = "Dyson",
              _type = ServiceRequests.Type.CorporateBody
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

            val request = ServiceRequests.Request(
              id = ServiceRequests.Id(
                idType = ServiceRequests.IdType.UTR,
                value = "1234567890"
              ),
              name = "Dyson",
              _type = ServiceRequests.Type.CorporateBody
            )

            val response = await(service.call(request))

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
                                |    "codes": "eis-returned-service-unavailable"
                                |  }
                                |]
                                |""".stripMargin)
                )
            )

            val request = ServiceRequests.Request(
              id = ServiceRequests.Id(
                idType = ServiceRequests.IdType.UTR,
                value = "1234567890"
              ),
              name = "Dyson",
              _type = ServiceRequests.Type.CorporateBody
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

            val request = ServiceRequests.Request(
              id = ServiceRequests.Id(
                idType = ServiceRequests.IdType.UTR,
                value = "1234567890"
              ),
              name = "Dyson",
              _type = ServiceRequests.Type.CorporateBody
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
