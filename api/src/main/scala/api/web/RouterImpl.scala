package api.web

import akka.NotUsed
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model._
import akka.stream.scaladsl.Flow
import api.service.RegistrationImage

class RouterImpl(
    registrationImage: RegistrationImage
) extends Router {

  val route: PartialFunction[(HttpMethod, Uri.Path), Flow[HttpRequest, HttpResponse, NotUsed]] = {
    case POST -> Uri.Path("/images") => registrationImage.execute
    case POST -> Uri.Path("/print")  => registrationImage.print
    case GET -> Uri.Path("/") =>
      Flow.fromFunction(_ => HttpResponse(200, entity = HttpEntity("main-api")))
  }

}
