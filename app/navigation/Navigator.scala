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

package navigation

import javax.inject.{Inject, Singleton}

import play.api.mvc.Call
import controllers.routes
import pages._
import models._

@Singleton
class Navigator @Inject() () {

/*
 * I think it will be cleaner to have this class as abstract
 * and to have each controller have its own concrete page
 * navigator.
 *
 * We can add the following to be implemented in each of
 * the concrete classes.
 *
 * def navigateInNormalMode(answers: UserAnswers): Call
 *
 * In CheckMode, we always go back to CYA if the data has not changed
 * so this can be implemented here. If the data has changed then we may
 * or nat not go back to CYA so we need a method like the following
 *
 * def navigateInCheckMode(answers: UserAnswers): Call
 */

  private val normalRoutes: Page => UserAnswers => Call = { case _ =>
    _ => routes.IndexController.onPageLoad
  }

  private val checkRouteMap: Page => UserAnswers => Call = { case _ =>
    _ => routes.CheckYourAnswersController.onPageLoad
  }

  def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call = mode match {
    case NormalMode =>
      normalRoutes(page)(userAnswers)
    case CheckMode =>
      checkRouteMap(page)(userAnswers)
  }
}
