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

package converters.subscription.update

import base.BaseSpec
import connectors.subscription.SubscriptionConnector
import connectors.subscription.update.SubscriptionUpdateConnector
import services.subscription.SubscriptionService
import services.subscription.update.SubscriptionUpdateService

import java.util.UUID

class SubscriptionUpdateConverterSpec extends BaseSpec {

  private val converter = new SubscriptionUpdateConverter

  "when updating a subscription, the converter returns the expected value, for a" - {
    "service request" in {
      val id1 = UUID.randomUUID().toString
      val id2 = UUID.randomUUID().toString
      val id3 = UUID.randomUUID().toString

      val ids = Table(
        ("Id", "Expected Id (Raw)"),
        (id1, id1),
        (id2, id2),
        (id3, id3)
      )

      forAll(ids) { (id, expectedRawId) =>
        val serviceRequest = SubscriptionUpdateService.Requests.Request(
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

        val connectorRequest = converter.convertServiceRequest(serviceRequest)

        connectorRequest shouldBe SubscriptionUpdateConnector.Requests.Request(
          id = expectedRawId,
          name = Some("Harold Winter"),
          contacts = Seq(
            SubscriptionConnector.Requests.Individual(
              typeCode = "I",
              firstName = "Patrick",
              middleName = Some("John"),
              lastName = "Dyson",
              landline = Some("747663966"),
              mobile = Some("38390756243"),
              emailAddress = "Patrick.Dyson@example.com"
            ),
            SubscriptionConnector.Requests.Organisation(
              typeCode = "O",
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
      val connectorResponse = SubscriptionConnector.Responses.Response("")

      val serviceResponse = converter.convertSuccessfulConnectorResponse(connectorResponse)

      serviceResponse shouldBe SubscriptionService.Responses.Response("OK")
    }
  }
}
