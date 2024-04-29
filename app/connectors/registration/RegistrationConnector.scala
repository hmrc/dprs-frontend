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

package connectors.registration

import config.FrontendAppConfig
import connectors.BaseBackendConnector
import play.api.libs.functional.syntax.{toFunctionalBuilderOps, unlift}
import play.api.libs.json.{JsPath, OWrites, Reads}
import play.api.libs.ws.WSClient

abstract class RegistrationConnector[REQUEST, RESPONSE](frontendAppConfig: FrontendAppConfig, wsClient: WSClient)
    extends BaseBackendConnector[REQUEST, RESPONSE](frontendAppConfig, wsClient)

object RegistrationConnector {

  object Requests {

    final case class Address(lineOne: String,
                             lineTwo: Option[String],
                             lineThree: Option[String],
                             lineFour: Option[String],
                             postalCode: String,
                             countryCode: String
    )

    object Address {
      implicit lazy val writes: OWrites[Address] =
        ((JsPath \ "lineOne").write[String] and
          (JsPath \ "lineTwo").writeNullable[String] and
          (JsPath \ "lineThree").writeNullable[String] and
          (JsPath \ "lineFour").writeNullable[String] and
          (JsPath \ "postalCode").write[String] and
          (JsPath \ "countryCode").write[String])(unlift(Address.unapply))
    }

    final case class ContactDetails(landline: Option[String], mobile: Option[String], fax: Option[String], emailAddress: Option[String])

    object ContactDetails {
      implicit lazy val writes: OWrites[ContactDetails] =
        ((JsPath \ "landline").writeNullable[String] and
          (JsPath \ "mobile").writeNullable[String] and
          (JsPath \ "fax").writeNullable[String] and
          (JsPath \ "emailAddress").writeNullable[String])(unlift(ContactDetails.unapply))
    }

  }

  object Responses {

    object Response {
      implicit lazy val reads: Reads[Response] =
        (JsPath \ "ids").read[Seq[Id]].map(Response(_))
    }

    final case class Id(idType: String, value: String)

    object Id {
      implicit lazy val reads: Reads[Id] =
        ((JsPath \ "type").read[String] and
          (JsPath \ "value").read[String])(Id.apply _)
    }

    final case class Response(ids: Seq[Id])
  }
}
