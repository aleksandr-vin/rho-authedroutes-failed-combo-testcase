package com.example.rhoauthedroutesfailedcombotestcase

import cats.effect.{ConcurrentEffect, Sync}
import cats.implicits._
import com.example.rhoauthedroutesfailedcombotestcase.RhoAuthedRoutesFailedComboTestCaseServer.{Auth, AuthInfo}
import org.http4s.{HttpRoutes, HttpService}
import org.http4s.rho.swagger.SwaggerSupport
import org.http4s.rho.{RhoMiddleware, RhoRoutes}
import org.http4s.server.AuthMiddleware


object RhoAuthedRoutesFailedComboTestCaseRoutes {

  def swaggerMiddleware[F[_]: Sync]: RhoMiddleware[F] = SwaggerSupport.apply[F].createRhoMiddleware()

  def jokeRoutes[F[_]: Sync](J: Jokes[F]) = new RhoRoutes[F] {
    GET / "joke" |>> { () =>
      for {
        joke <- J.get
        resp <- Ok(joke)
      } yield resp
    }
  }.toRoutes(swaggerMiddleware)

  def helloWorldRoutes[F[_]: Sync : ConcurrentEffect](H: HelloWorld[F], auth: Auth[F]) = new RhoRoutes[F] {
    GET / "hello" / pathVar[String]("name", "parameter description") >>> auth.auth |>> {
      (name: String, au: AuthInfo) => for {
        greeting <- H.hello(HelloWorld.Name(name), HelloWorld.Name(au.user))
        resp <- Ok(greeting)
      } yield resp
    }
  }.toRoutes(swaggerMiddleware)
}
