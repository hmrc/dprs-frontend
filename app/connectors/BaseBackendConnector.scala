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

package connectors

import config.FrontendAppConfig
import play.api.libs.ws.WSClient
import uk.gov.hmrc.http.StringContextOps

import java.net.URL

abstract class BaseBackendConnector[REQUEST, RESPONSE](frontendAppConfig: FrontendAppConfig, wsClient: WSClient)
    extends BaseConnector[REQUEST, RESPONSE](wsClient) {

  final override def url(): URL =
    url"${frontendAppConfig.baseUrlForBackendConnector + connectorPath}"

  def connectorPath: String

}

object BaseBackendConnector {
  val connectorName: String = "backend"
}
