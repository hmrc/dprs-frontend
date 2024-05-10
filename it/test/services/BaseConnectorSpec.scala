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

package services

import com.github.tomakehurst.wiremock.client.WireMock.getAllServeEvents
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.time.SpanSugar.convertIntToGrainOfTime
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.http.test.WireMockSupport

import scala.concurrent.{Await, Awaitable, ExecutionContext}
import scala.jdk.CollectionConverters.CollectionHasAsScala

abstract class BaseConnectorSpec extends AnyFreeSpec with Matchers with ScalaFutures with IntegrationPatience with GuiceOneServerPerSuite with WireMockSupport {

  implicit val executionContext: ExecutionContext = app.injector.instanceOf[ExecutionContext]

  def connectorName: String

  def extraApplicationConfig: Map[String, Any] = Map(
    s"microservice.services.$connectorName.host"    -> wireMockHost,
    s"microservice.services.$connectorName.port"    -> wireMockPort,
    s"microservice.services.$connectorName.context" -> ""
  )

  def await[T](awaitable: Awaitable[T]): T = Await.result(awaitable, 1.second)

  override def fakeApplication(): Application =
    baseApplicationBuilder()
      .configure(extraApplicationConfig)
      .build()

  def verifyThatDownstreamApiWasCalled(): Unit =
    withClue("We expected a single downstream API (stub) to be called, but it wasn't.") {
      getAllServeEvents.asScala.count(_.getWasMatched) shouldBe 1
    }

  private def baseApplicationBuilder(): GuiceApplicationBuilder =
    GuiceApplicationBuilder()
      .configure(
        "metrics.enabled" -> false
      )

}
