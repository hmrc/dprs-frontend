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

package converters.subscription.create

import base.BaseSpec
import connectors.subscription.create.SubscriptionCreationConnector
import services.subscription.create.SubscriptionCreationService

class SubscriptionCreationConverterSpec extends BaseSpec {

  private val converter = new SubscriptionCreationConverter

  "when creating a subscription, the converter returns the expected value, for a" - {
    "service request" in {
      val idTypes =
        Table(
          ("Type", "Expected Type (Raw)"),
          ("NINO", "NINO"),
          ("UTR", "UTR"),
          ("SAFE", "SAFE")
        )

      forAll(idTypes) { (rawIdType, expectedRawType) =>
        val idType = SubscriptionCreationService.Requests.IdType.all.find(_.toString == rawIdType).get
        val serviceRequest = SubscriptionCreationService.Requests.Request(
          id = SubscriptionCreationService.Requests.Id(idType, "AA000000A"),
          name = Some("Harold Winter"),
          contacts = Seq(
            SubscriptionCreationService.Requests.Individual(
              firstName = "Patrick",
              middleName = Some("John"),
              lastName = "Dyson",
              landline = Some("747663966"),
              mobile = Some("38390756243"),
              emailAddress = "Patrick.Dyson@example.com"
            ),
            SubscriptionCreationService.Requests.Organisation(name = "Dyson",
                                                              landline = Some("847663966"),
                                                              mobile = Some("48390756243"),
                                                              emailAddress = "info@example.com"
            )
          )
        )

        val connectorRequest = converter.convertServiceRequest(serviceRequest)

        connectorRequest shouldBe SubscriptionCreationConnector.Requests.Request(
          id = SubscriptionCreationConnector.Requests.Id(expectedRawType, "AA000000A"),
          name = Some("Harold Winter"),
          contacts = Seq(
            SubscriptionCreationConnector.Requests.Individual(
              typeCode = "I",
              firstName = "Patrick",
              middleName = Some("John"),
              lastName = "Dyson",
              landline = Some("747663966"),
              mobile = Some("38390756243"),
              emailAddress = "Patrick.Dyson@example.com"
            ),
            SubscriptionCreationConnector.Requests.Organisation(typeCode = "O",
                                                                name = "Dyson",
                                                                landline = Some("847663966"),
                                                                mobile = Some("48390756243"),
                                                                emailAddress = "info@example.com"
            )
          )
        )
      }
    }
    "connector response" in {
      val connectorResponse = SubscriptionCreationConnector.Responses.Response("1cb6d341-4f17-446e-a549-b3e85f8f05f4")

      val serviceResponse = converter.convertSuccessfulConnectorResponse(connectorResponse)

      serviceResponse shouldBe SubscriptionCreationService.Responses.Response("1cb6d341-4f17-446e-a549-b3e85f8f05f4")
    }

  }
}