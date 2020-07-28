package com.example.rhoauthedroutesfailedcombotestcase

import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._

object Main extends IOApp {
  def run(args: List[String]) =
    RhoAuthedRoutesFailedComboTestCaseServer.stream[IO].compile.drain.as(ExitCode.Success)
}