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

package converters.registration.withoutId

import base.BaseSpec
import connectors.registration.RegistrationConnector
import connectors.registration.withoutId.RegistrationWithoutIdForIndividualConnector
import services.registration.BaseRegistrationService.{Responses => CommonRegistrationResponses}
import services.registration.withoutId.BaseRegistrationWithoutIdService.{Requests => CommonRegistrationWithoutIdRequests}
import services.registration.withoutId.RegistrationWithoutIdForIndividualService

class RegistrationWithoutIdForIndividualConverterSpec extends BaseSpec {

  private val converter = new RegistrationWithoutIdForIndividualConverter

  "when registering as an individual, the converter returns the expected value, for a " - {
    "service request" in {
      val serviceRequest = RegistrationWithoutIdForIndividualService.Requests.Request(
        firstName = "Patrick",
        middleName = Some("John"),
        lastName = "Dyson",
        dateOfBirth = "Dyson",
        address = CommonRegistrationWithoutIdRequests.Address(lineOne = "34 Park Lane",
                                                              lineTwo = Some("Building A"),
                                                              lineThree = Some("Suite 100"),
                                                              lineFour = Some("Manchester"),
                                                              postalCode = "M54 1MQ",
                                                              countryCode = "GB"
        ),
        contactDetails = CommonRegistrationWithoutIdRequests.ContactDetails(landline = Some("747663966"),
                                                                            mobile = Some("38390756243"),
                                                                            fax = Some("58371813020"),
                                                                            emailAddress = Some("Patrick.Dyson@example.com")
        )
      )

      val connectorRequest = converter.convertServiceRequest(serviceRequest)

      connectorRequest shouldBe RegistrationWithoutIdForIndividualConnector.Requests.Request(
        firstName = "Patrick",
        middleName = Some("John"),
        lastName = "Dyson",
        dateOfBirth = "Dyson",
        address = RegistrationConnector.Requests.Address(lineOne = "34 Park Lane",
                                                         lineTwo = Some("Building A"),
                                                         lineThree = Some("Suite 100"),
                                                         lineFour = Some("Manchester"),
                                                         postalCode = "M54 1MQ",
                                                         countryCode = "GB"
        ),
        contactDetails = RegistrationConnector.Requests.ContactDetails(landline = Some("747663966"),
                                                                       mobile = Some("38390756243"),
                                                                       fax = Some("58371813020"),
                                                                       emailAddress = Some("Patrick.Dyson@example.com")
        )
      )

    }
    "connector response" in {
      val connectorResponse = RegistrationConnector.Responses.Response(
        Seq(
          RegistrationConnector.Responses.Id("ARN", "WARN3849921"),
          RegistrationConnector.Responses.Id("SAFE", "XE0000200775706"),
          RegistrationConnector.Responses.Id("SAP", "1960629967"),
          RegistrationConnector.Responses.Id("CAT", "25562288-ae0d-447a-863a-aac881b287a9")
        )
      )

      val serviceResponse = converter.convertSuccessfulConnectorResponse(Some(connectorResponse))

      serviceResponse shouldBe Some(
        CommonRegistrationResponses.Response(
          Seq(
            CommonRegistrationResponses.Id(CommonRegistrationResponses.IdType.ARN, "WARN3849921"),
            CommonRegistrationResponses.Id(CommonRegistrationResponses.IdType.SAFE, "XE0000200775706"),
            CommonRegistrationResponses.Id(CommonRegistrationResponses.IdType.SAP, "1960629967"),
            CommonRegistrationResponses.Id(CommonRegistrationResponses.IdType.UNKNOWN, "25562288-ae0d-447a-863a-aac881b287a9")
          )
        )
      )
    }
  }
}
