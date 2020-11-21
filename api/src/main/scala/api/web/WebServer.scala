package api.web

import akka.NotUsed
import akka.actor.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{Behavior, Terminated}
import akka.http.scaladsl.Http
import akka.stream.scaladsl.{Sink, Source}

import scala.concurrent.{ExecutionContextExecutor, Future}

object WebServer {

  def apply(router: Router): Behavior[NotUsed] = {
    Behaviors.setup { context =>
      implicit val system: ActorSystem          = context.system.classicSystem
      implicit val ec: ExecutionContextExecutor = system.dispatcher

      val serverSource: Source[Http.IncomingConnection, Future[Http.ServerBinding]] =
        Http().newServerAt("localhost", 8080).connectionSource()
      val bindingFuture: Future[Http.ServerBinding] =
        serverSource
          .to(Sink.foreach { connection => // foreach materializes the source
            println("Accepted new connection from " + connection.remoteAddress)
            connection.handleWith(router.handleRequest)
          })
          .run()

      Behaviors.receiveSignal {
        case (_, Terminated(_)) =>
          bindingFuture.flatMap(_.unbind())
          Behaviors.stopped
      }
    }
  }

}
