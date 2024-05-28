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

package services.subscription

object SubscriptionService {

  object Requests {

    sealed trait Contact {
      def landline: Option[String]
      def mobile: Option[String]
      def emailAddress: String
    }

    final case class Individual(
      firstName: String,
      middleName: Option[String],
      lastName: String,
      landline: Option[String],
      mobile: Option[String],
      emailAddress: String
    ) extends Contact

    final case class Organisation(
      name: String,
      landline: Option[String],
      mobile: Option[String],
      emailAddress: String
    ) extends Contact
  }

  object Responses {

    final case class Response(id: String)
  }
}
