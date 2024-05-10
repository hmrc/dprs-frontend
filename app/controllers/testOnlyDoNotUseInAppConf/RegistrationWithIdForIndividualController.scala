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

package controllers.testOnlyDoNotUseInAppConf

import com.google.inject.Inject
import controllers.testOnlyDoNotUseInAppConf.RegistrationWithIdForIndividualController._
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json._
import play.api.mvc.{Action, MessagesControllerComponents}
import services.BaseService
import services.registration.BaseRegistrationService.Responses.Id
import services.registration.withId.BaseRegistrationWithIdService.Responses
import services.registration.withId.BaseRegistrationWithIdService.Responses.{Address, ContactDetails}
import services.registration.withId.RegistrationWithIdForIndividualService
import services.registration.withId.RegistrationWithIdForIndividualService.Requests.Request
import services.registration.withId.RegistrationWithIdForIndividualService.Responses.Response
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import scala.Function.unlift
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RegistrationWithIdForIndividualController @Inject() (service: RegistrationWithIdForIndividualService,
                                                           override val controllerComponents: MessagesControllerComponents
) extends FrontendBaseController {

  def call(): Action[JsValue] = Action(parse.json).async { implicit request =>
    // TODO: Read in request.
    request.body.validate[Request] match {
      case JsSuccess(serviceRequest, _) =>
        service.call(serviceRequest) map {
          case Right(response) => Ok(Json.toJson(response))
          case Left(errors)    => InternalServerError(Json.toJson(errors))
        }
      case JsError(errors) => Future.successful(BadRequest(errors.toString()))
    }

  }
}

object RegistrationWithIdForIndividualController {

  implicit lazy val requestReads: Reads[Request] =
    ((JsPath \ "id").read[RegistrationWithIdForIndividualService.Requests.Id] and
      (JsPath \ "firstName").read[String] and
      (JsPath \ "middleName").readNullable[String] and
      (JsPath \ "lastName").read[String] and
      (JsPath \ "dateOfBirth").read[String])(Request.apply _)

  implicit lazy val idReads: Reads[RegistrationWithIdForIndividualService.Requests.Id] =
    ((JsPath \ "type")
      .read[String]
      .map(rawType =>
        RegistrationWithIdForIndividualService.Requests.IdType.all
          .find(_.toString == rawType)
          .getOrElse(RegistrationWithIdForIndividualService.Requests.IdType.UNKNOWN)
      ) and
      (JsPath \ "value").read[String])(RegistrationWithIdForIndividualService.Requests.Id.apply _)

  implicit lazy val responseWrites: OWrites[Response] =
    ((JsPath \ "ids").write[Seq[Id]] and
      (JsPath \ "firstName").write[String] and
      (JsPath \ "middleName").writeNullable[String] and
      (JsPath \ "lastName").write[String] and
      (JsPath \ "dateOfBirth").writeNullable[String] and
      (JsPath \ "address").write[Address] and
      (JsPath \ "contactDetails").write[ContactDetails])(unlift(Response.unapply))

  implicit lazy val idWrites: OWrites[Id] =
    ((JsPath \ "type").write[String].contramap[services.registration.BaseRegistrationService.Responses.IdType](_.toString) and
      (JsPath \ "value").write[String])(unlift(Id.unapply))

  implicit lazy val addressWrites: OWrites[Address] =
    ((JsPath \ "lineOne").write[String] and
      (JsPath \ "lineTwo").writeNullable[String] and
      (JsPath \ "lineThree").writeNullable[String] and
      (JsPath \ "lineFour").writeNullable[String] and
      (JsPath \ "postalCode").write[String] and
      (JsPath \ "countryCode").write[String])(unlift(Responses.Address.unapply))

  implicit lazy val contactDetailWrites: OWrites[ContactDetails] =
    ((JsPath \ "landline").writeNullable[String] and
      (JsPath \ "mobile").writeNullable[String] and
      (JsPath \ "fax").writeNullable[String] and
      (JsPath \ "emailAddress").writeNullable[String])(unlift(ContactDetails.unapply))

  implicit lazy val errorWrites: OWrites[BaseService.Responses.Error] =
    (JsPath \ "code").write[String].contramap(_.code)

  implicit lazy val errorsWrites: OWrites[BaseService.Responses.Errors] =
    ((JsPath \ "status").write[Int] and
      (JsPath \ "errors").write[Seq[BaseService.Responses.Error]])(unlift(BaseService.Responses.Errors.unapply))

}
