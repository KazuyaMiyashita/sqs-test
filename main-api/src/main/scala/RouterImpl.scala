package mainApi

import scala.util.matching.Regex
import akka.stream.Materializer
import akka.stream.scaladsl.{Source, Flow, Sink}
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, HttpMethod, Uri, HttpEntity}
import akka.http.scaladsl.model.HttpMethods._
import akka.NotUsed

class RouterImpl extends Router {

  object Root extends Router {
    val Root = "/"
    val route = {
      case GET -> Uri.Path(Root) =>
        Flow.fromFunction((_: HttpRequest) => HttpResponse(200, entity = HttpEntity("main-api")))
    }
  }

  val route = Root.route

}
