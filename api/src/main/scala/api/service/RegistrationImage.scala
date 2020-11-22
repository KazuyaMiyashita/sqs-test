package api.service

import java.nio.charset.StandardCharsets
import java.util.UUID

import akka.NotUsed
import akka.http.scaladsl.model.Multipart.FormData
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpRequest, HttpResponse, Multipart}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.alpakka.s3.MultipartUploadResult
import akka.stream.alpakka.s3.scaladsl.S3
import akka.stream.Materializer
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.util.ByteString
import api.service.RegistrationImage.ResultDTO
import io.circe.Encoder

import scala.concurrent.{ExecutionContext, Future}

class RegistrationImage()(implicit mat: Materializer) {

  implicit val ec: ExecutionContext = mat.executionContext

  private def extractImageSource(request: HttpRequest): Future[Source[ByteString, Any]] = {
    for {
      formData <- Unmarshal(request.entity).to[Multipart.FormData]
    } yield {
      val bodyParts: Source[FormData.BodyPart, Any] = formData.parts
      val imageBodyPart: Source[FormData.BodyPart, Any] =
        bodyParts.collect { case b if b.name == "image" => b }.take(1) // ListのheadOptionみたいなことをしたい
      val imageBytes
          : Source[ByteString, Any] = imageBodyPart.flatMapConcat(_.entity.dataBytes) // Option[NonEmptyList[_]] みたいにしたい
      imageBytes
    }
  }

  private def uploadImage(imageSource: Source[ByteString, Any]): Future[ResultDTO] = {
    val bucket: String                                          = "images"
    val key: String                                             = UUID.randomUUID().toString
    val s3Sink: Sink[ByteString, Future[MultipartUploadResult]] = S3.multipartUpload(bucket, key)

    for {
      s3 <- imageSource.runWith(s3Sink)
    } yield ResultDTO(s3.location.toString, s3.bucket)
  }

  private def toResponse(resultDTO: ResultDTO): HttpResponse = {
    HttpResponse(200, entity = HttpEntity.apply(ContentTypes.`application/json`, resultDTO.toJson))
  }

  val execute: Flow[HttpRequest, HttpResponse, NotUsed] = Flow[HttpRequest].mapAsync(1) { request =>
    for {
      imageSource <- extractImageSource(request)
      resultDto   <- uploadImage(imageSource)
    } yield toResponse(resultDto)
  }

}

object RegistrationImage {

  case class ResultDTO(
      location: String,
      bucket: String
  ) {
    def toJson: ByteString = ByteString(ResultDTO.encoder(this).noSpaces, StandardCharsets.UTF_8)
  }

  object ResultDTO {
    import io.circe.generic.semiauto._
    implicit lazy val encoder: Encoder[ResultDTO] = deriveEncoder
  }

}
