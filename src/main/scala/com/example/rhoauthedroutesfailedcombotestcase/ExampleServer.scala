package com.example.rhoauthedroutesfailedcombotestcase

import cats.data.{Kleisli, OptionT}
import cats.effect.{ConcurrentEffect, ContextShift, Sync, Timer}
import cats.implicits._
import com.example.rhoauthedroutesfailedcombotestcase.ExampleRoutes.{helloWorldRoutes, jokeRoutes}
import fs2.Stream
import org.http4s.{AuthedRoutes, HttpRoutes, Request}
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.implicits._
import org.http4s.server.AuthMiddleware
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.Logger
import org.http4s.util.CaseInsensitiveString

import scala.concurrent.ExecutionContext.global

object ExampleServer {

  case class AuthInfo(user: String)

  def authUser[F[_]: Sync]: Kleisli[OptionT[F, *], Request[F], AuthInfo] =
    Kleisli(request => OptionT.fromOption {
      request.headers.get(CaseInsensitiveString("api-key")).map(_.value).flatMap {
        case "123" => Some(AuthInfo("jay"))
        case "321" => Some(AuthInfo("bob"))
        case _ => None
      }
    })

  def authInfoMiddleware[F[_]: Sync]: AuthMiddleware[F, AuthInfo] = AuthMiddleware(authUser)

  class Auth[F[_]: ConcurrentEffect] extends org.http4s.rho.AuthedContext[F, AuthInfo]

  def stream[F[_]: ConcurrentEffect](implicit T: Timer[F], C: ContextShift[F]): Stream[F, Nothing] = {

    val auth = new Auth[F]

    def authHelloWorldRoutes(H: HelloWorld[F]): AuthedRoutes[AuthInfo, F] = auth.toService(helloWorldRoutes[F](H, auth))

    for {
      client <- BlazeClientBuilder[F](global).stream
      helloWorldAlg = HelloWorld.impl[F]
      jokeAlg = Jokes.impl[F](client)

      // Combine Service Routes into an HttpApp.
      // Can also be done via a Router if you
      // want to extract a segments not checked
      // in the underlying routes.
      httpApp = (
        jokeRoutes[F](jokeAlg) <+> authInfoMiddleware[F].apply(authHelloWorldRoutes(helloWorldAlg))
      ).orNotFound

      // With Middlewares in place
      finalHttpApp = Logger.httpApp(true, true)(httpApp)

      exitCode <- BlazeServerBuilder[F](global)
        .bindHttp(8080, "0.0.0.0")
        .withHttpApp(finalHttpApp)
        .serve
    } yield exitCode
  }.drain
}
