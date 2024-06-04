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

package converters.subscription.read

import base.BaseSpec
import connectors.subscription.SubscriptionConnector
import connectors.subscription.read.SubscriptionReadConnector
import services.subscription.SubscriptionService
import services.subscription.read.SubscriptionReadService

class SubscriptionReadConverterSpec extends BaseSpec {

  private val converter = new SubscriptionReadConverter

  "when updating a subscription, the converter returns the expected value, for a" - {
    "service request" in {
      val serviceRequest = SubscriptionReadService.Requests.Request(
        id = "a7405c8d-06ee-46a3-b5a0-5d65176360ed"
      )

      val connectorRequest = converter.convertServiceRequest(serviceRequest)

      connectorRequest shouldBe SubscriptionReadConnector.Requests.Request(
        id = "a7405c8d-06ee-46a3-b5a0-5d65176360ed"
      )
    }
    "connector response" in {
      val connectorResponse = SubscriptionReadConnector.Responses.Response(
        id = "a7405c8d-06ee-46a3-b5a0-5d65176360ed",
        name = "Harold Winter",
        contacts = Seq(
          SubscriptionConnector.RequestOrResponse.Individual(
            typeCode = "I",
            firstName = "Patrick",
            middleName = Some("John"),
            lastName = "Dyson",
            landline = Some("747663966"),
            mobile = Some("38390756243"),
            emailAddress = "Patrick.Dyson@example.com"
          ),
          SubscriptionConnector.RequestOrResponse.Organisation(
            typeCode = "O",
            name = "Dyson",
            landline = Some("847663966"),
            mobile = Some("48390756243"),
            emailAddress = "info@example.com"
          )
        )
      )

      val serviceResponse = converter.convertSuccessfulConnectorResponse(Some(connectorResponse))

      serviceResponse shouldBe Some(
        SubscriptionReadService.Responses.Response(
          name = "Harold Winter",
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
      )
    }
  }
}
