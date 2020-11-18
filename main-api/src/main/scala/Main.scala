package mainApi

import akka.actor.typed.{ActorSystem, Behavior, Terminated}
import akka.actor.typed.scaladsl.Behaviors
import akka.stream.Materializer
import akka.NotUsed
import scala.io.StdIn

object Main extends App {

  def init(): Behavior[NotUsed] = {
    Behaviors.setup { context =>
      val router = new RouterImpl
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
