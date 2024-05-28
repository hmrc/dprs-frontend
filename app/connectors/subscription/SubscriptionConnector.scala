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

package connectors.subscription

import play.api.libs.functional.syntax.{toFunctionalBuilderOps, unlift}
import play.api.libs.json.{JsPath, Json, OWrites, Reads}

object SubscriptionConnector {
  object Requests {

    sealed trait Contact {
      def typeCode: String
      def landline: Option[String]
      def mobile: Option[String]
      def emailAddress: String
    }

    object Contact {
      implicit lazy val writes: OWrites[Contact] = Json.writes[Contact].transform(jsObject => jsObject - "_type")
    }

    final case class Individual(
      typeCode: String,
      firstName: String,
      middleName: Option[String],
      lastName: String,
      landline: Option[String],
      mobile: Option[String],
      emailAddress: String
    ) extends Contact

    object Individual {
      implicit lazy val writes: OWrites[Individual] =
        ((JsPath \ "type").write[String] and
          (JsPath \ "firstName").write[String] and
          (JsPath \ "middleName").writeNullable[String] and
          (JsPath \ "lastName").write[String] and
          (JsPath \ "landline").writeNullable[String] and
          (JsPath \ "mobile").writeNullable[String] and
          (JsPath \ "emailAddress").write[String])(unlift(Individual.unapply))
    }

    final case class Organisation(
      typeCode: String,
      name: String,
      landline: Option[String],
      mobile: Option[String],
      emailAddress: String
    ) extends Contact

    object Organisation {
      implicit lazy val writes: OWrites[Organisation] =
        ((JsPath \ "type").write[String] and
          (JsPath \ "name").write[String] and
          (JsPath \ "landline").writeNullable[String] and
          (JsPath \ "mobile").writeNullable[String] and
          (JsPath \ "emailAddress").write[String])(unlift(Organisation.unapply))
    }
  }

  object Responses {

    final case class Response(id: String)

    object Response {
      implicit lazy val reads: Reads[Response] =
        (JsPath \ "id").read[String].map(Response(_))

    }
  }
}
