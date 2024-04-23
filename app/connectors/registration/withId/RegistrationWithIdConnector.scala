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

package connectors.registration.withId

import play.api.libs.functional.syntax.{toFunctionalBuilderOps, unlift}
import play.api.libs.json.{JsPath, OWrites, Reads}

object RegistrationWithIdConnector {

  object Requests {

    final case class Id(idType: String, value: String)

    object Id {
      implicit lazy val writes: OWrites[Id] =
        ((JsPath \ "type").write[String] and
          (JsPath \ "value").write[String])(unlift(Id.unapply))
    }

  }

  object Responses {

    final case class Address(lineOne: String,
                             lineTwo: Option[String],
                             lineThree: Option[String],
                             lineFour: Option[String],
                             postalCode: String,
                             countryCode: String
    )

    object Address {
      implicit lazy val reads: Reads[Address] =
        ((JsPath \ "lineOne").read[String] and
          (JsPath \ "lineTwo").readNullable[String] and
          (JsPath \ "lineThree").readNullable[String] and
          (JsPath \ "lineFour").readNullable[String] and
          (JsPath \ "postalCode").read[String] and
          (JsPath \ "countryCode").read[String])(Address.apply _)
    }

    final case class ContactDetails(landline: Option[String], mobile: Option[String], fax: Option[String], emailAddress: Option[String])

    object ContactDetails {
      implicit lazy val reads: Reads[ContactDetails] =
        ((JsPath \ "landline").readNullable[String] and
          (JsPath \ "mobile").readNullable[String] and
          (JsPath \ "fax").readNullable[String] and
          (JsPath \ "emailAddress").readNullable[String])(ContactDetails.apply _)
    }

  }

}
