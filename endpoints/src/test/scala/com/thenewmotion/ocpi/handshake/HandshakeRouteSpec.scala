package com.thenewmotion.ocpi.handshake

import com.thenewmotion.ocpi.msgs.v2_0.CommonTypes.{BusinessDetails => OcpiBusinessDetails, Url}
import com.thenewmotion.ocpi.msgs.v2_0.Credentials.Creds
import org.joda.time.format.ISODateTimeFormat
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.specs2.specification.Scope
import spray.http.MediaTypes._
import spray.http.{ContentType, HttpCharsets, HttpEntity}
import spray.testkit.Specs2RouteTest

import scala.concurrent.Future
import scalaz._

class HandshakeRouteSpec extends Specification with Specs2RouteTest with Mockito {

  "credentials endpoint" should {
    "accept client credentials" in new CredentialsTestScope {

      val data =
        s"""
           |{
           |    "token": "ebf3b399-779f-4497-9b9d-ac6ad3cc44d2",
           |    "url": "https://example.com/ocpi/cpo/",
           |    "business_details": {
           |        "name": "Example Operator",
           |        "logo": "http://example.com/images/logo.png",
           |        "website": "http://example.com"
           |    }
           |}
           |""".stripMargin

      val body = HttpEntity(contentType = ContentType(`application/json`, HttpCharsets.`UTF-8`), string = data)

      Post("/credentials", body) ~> credentialsRoutes.handshakeRoute("2.0", "123") ~> check {
        handled must beTrue
      }
    }
  }

  trait CredentialsTestScope extends Scope {

    val formatter = ISODateTimeFormat.dateTimeNoMillis().withZoneUTC
    val dateTime1 = formatter.parseDateTime("2010-01-01T00:00:00Z")

    val credentialsRoutes = new HandshakeRoutes {
      override val currentTime = mock[CurrentTime]
      currentTime.instance returns dateTime1
      val creds1 = Creds("", "", OcpiBusinessDetails("", None, None))

      override val handshakeService = mock[HandshakeService]
      handshakeService.registerVersionsEndpoint(any, any, any)(any) returns
        Future.successful(\/-(creds1))

      val hdh: HandshakeDataHandler = new HandshakeDataHandler {
        val creds1 = Creds("", "", OcpiBusinessDetails("", None, None))

        def persistClientPrefs(version: String, auth: String, creds: Credentials) = \/-(Unit)

        def persistNewToken(auth: String, newToken: String) = ???

        def config: HandshakeConfig =  HandshakeConfig("",0,"","","","credentials","versions")

        def persistEndpoint(version: String, auth: String, name: String, url: Url) = ???
      }

      def actorRefFactory = system
    }

    val credentials1 = Creds(
      token = "ebf3b399-779f-4497-9b9d-ac6ad3cc44d2",
      url = "https://example.com/ocpi/cpo/",
      business_details = OcpiBusinessDetails(
        "Example Operator",
        Some("http://example.com/images/logo.png"),
        Some("http://example.com")
      )
    )
  }
}
