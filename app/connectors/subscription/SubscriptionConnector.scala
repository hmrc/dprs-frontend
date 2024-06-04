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

import config.FrontendAppConfig
import connectors.BaseBackendConnector
import play.api.libs.functional.syntax.{toFunctionalBuilderOps, unlift}
import play.api.libs.json._
import play.api.libs.ws.WSClient

abstract class SubscriptionConnector[REQUEST, RESPONSE](frontendAppConfig: FrontendAppConfig, wsClient: WSClient)
    extends BaseBackendConnector[REQUEST, RESPONSE](frontendAppConfig, wsClient) {

  final override def connectorPath: String = SubscriptionConnector.connectorPath
}

object SubscriptionConnector {

  val connectorPath: String = "/subscriptions"

  object RequestOrResponse {

    sealed trait Contact {
      def typeCode: String
      def landline: Option[String]
      def mobile: Option[String]
      def emailAddress: String
    }

    object Contact {
      private val invalidTypeError               = JsError(JsPath(List(KeyPathNode("type"))), "error.invalid")
      implicit lazy val writes: OWrites[Contact] = Json.writes[Contact].transform(jsObject => jsObject - "_type")
      implicit lazy val reads: Reads[Contact] = (json: JsValue) =>
        (json \ "type").toOption
          .map {
            _.as[JsString].value.trim.toUpperCase match {
              case "I" => json.validate[Individual]
              case "O" => json.validate[Organisation]
              case _   => invalidTypeError
            }
          }
          .getOrElse(invalidTypeError)
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

      implicit lazy val reads: Reads[Individual] =
        ((JsPath \ "type").read[String] and
          (JsPath \ "firstName").read[String] and
          (JsPath \ "middleName").readNullable[String] and
          (JsPath \ "lastName").read[String] and
          (JsPath \ "landline").readNullable[String] and
          (JsPath \ "mobile").readNullable[String] and
          (JsPath \ "emailAddress").read[String])(Individual.apply _)
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

    implicit lazy val reads: Reads[Organisation] =
      ((JsPath \ "type").read[String] and
        (JsPath \ "name").read[String] and
        (JsPath \ "landline").readNullable[String] and
        (JsPath \ "mobile").readNullable[String] and
        (JsPath \ "emailAddress").read[String])(Organisation.apply _)
  }
}
