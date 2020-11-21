package api

import akka.NotUsed
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorSystem, Behavior, Terminated}
import akka.stream.Materializer
import api.service.RegistrationImage
import api.web.{RouterImpl, WebServer}

import scala.io.StdIn

object Main extends App {

  def init(): Behavior[NotUsed] = {
    Behaviors.setup { context =>
      val mat: Materializer = Materializer.matFromSystem(context.system.classicSystem)
      val registrationImage = new RegistrationImage()(mat)
      val router            = new RouterImpl(registrationImage)
      context.spawn(WebServer(router), "webserver")

      Behaviors.receiveSignal {
        case (_, Terminated(_)) =>
          Behaviors.stopped
      }
    }
  }

  val system = ActorSystem(init(), "main")

  println("\nServer online at http://localhost:8080/\nPress RETURN to stop...\n")
  StdIn.readLine()
  system.terminate()

}
