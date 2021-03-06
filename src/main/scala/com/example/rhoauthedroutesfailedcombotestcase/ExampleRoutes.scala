package com.example.rhoauthedroutesfailedcombotestcase

import cats.effect.{ConcurrentEffect, Sync}
import cats.implicits._
import com.example.rhoauthedroutesfailedcombotestcase.ExampleServer.{Auth, AuthInfo}
import org.http4s.HttpRoutes
import org.http4s.rho.swagger.SwaggerSupport
import org.http4s.rho.{RhoMiddleware, RhoRoutes}


object ExampleRoutes {

  def swaggerMiddleware[F[_]: Sync]: RhoMiddleware[F] = SwaggerSupport.apply[F].createRhoMiddleware()

  def nonAuthenticatedRoutes[F[_]: Sync](J: Jokes[F], H: HelloWorld[F]): HttpRoutes[F] = new RhoRoutes[F] {
    GET / "joke" |>> {
      () => for {
        joke <- J.get
        resp <- Ok(joke)
      } yield resp
    }
    GET / "hello" / pathVar[String]("name", "parameter description") |>> {
      (name: String) => for {
        greeting <- H.hello(HelloWorld.Name(name), HelloWorld.Name("???"))
        resp <- Ok(greeting)
      } yield resp
    }
  }.toRoutes(swaggerMiddleware)

  def authenticatedRoutes[F[_]: Sync : ConcurrentEffect](H: HelloWorld[F], auth: Auth[F]): HttpRoutes[F] = new RhoRoutes[F] {
    PUT / "hello" / pathVar[String]("name", "parameter description") >>> auth.auth |>> {
      (name: String, au: AuthInfo) => for {
        greeting <- H.hello(HelloWorld.Name(name), HelloWorld.Name(au.user))
        resp <- Ok(greeting)
      } yield resp
    }
  }.toRoutes(swaggerMiddleware)
}
