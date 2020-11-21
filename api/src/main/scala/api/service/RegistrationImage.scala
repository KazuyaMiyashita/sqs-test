package api.service

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path}
import java.util.UUID

import akka.NotUsed
import akka.http.scaladsl.model.Multipart.FormData
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpRequest, HttpResponse, Multipart}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.alpakka.s3.MultipartUploadResult
import akka.stream.alpakka.s3.scaladsl.S3
import akka.stream.{IOResult, Materializer}
import akka.stream.scaladsl.{FileIO, Flow, Sink, Source}
import akka.util.ByteString
import api.service.RegistrationImage.ResultDTO
import io.circe.Encoder

import scala.concurrent.{ExecutionContext, Future}

class RegistrationImage()(implicit mat: Materializer) {

  implicit val ec: ExecutionContext = mat.executionContext

  val execute: Flow[HttpRequest, HttpResponse, NotUsed] = {
    val imageFlow: Flow[HttpRequest, ByteString, NotUsed] = Flow[HttpRequest].flatMapConcat { request =>
      println("imageFlow")
      val formDataAsync: Future[FormData]                             = Unmarshal(request.entity).to[Multipart.FormData]
      val fromDataSourceAsync: Future[Source[FormData.BodyPart, Any]] = formDataAsync.map(_.parts)
      val futureSource: Future[Source[ByteString, Any]] = fromDataSourceAsync.map {
        source: Source[FormData.BodyPart, Any] =>
          val imageBodyPart: Source[FormData.BodyPart, Any] = source.collect {
            case b if b.name.contains("image") => b
          }
          val imageDataBytes: Source[ByteString, Any] =
            imageBodyPart.flatMapConcat(_.entity.dataBytes.fold(ByteString.empty)(_ ++ _))
          imageDataBytes
      }
      Source.futureSource(futureSource)
    }

    val handleImage: Flow[ByteString, HttpResponse, NotUsed] = Flow[ByteString].mapAsync(1) { bs =>
      println("handleImage")
      val tempPath: Path                               = Files.createTempFile("prefix", ".suffix")
      val fileSink: Sink[ByteString, Future[IOResult]] = FileIO.toPath(tempPath)

      val bucket: String                                          = "images"
      val key: String                                             = UUID.randomUUID().toString
      val s3Sink: Sink[ByteString, Future[MultipartUploadResult]] = S3.multipartUpload(bucket, key)

      val source = Source.single(bs)
      for {
        _  <- source.runWith(fileSink)
        s3 <- source.runWith(s3Sink)
      } yield {
        val dto = ResultDTO(tempPath.toAbsolutePath.toString, s3.location.toString, s3.bucket)
        println(dto)
        HttpResponse(200, entity = HttpEntity.apply(ContentTypes.`application/json`, dto.toJson))
      }
    }

    Flow[HttpRequest].via(imageFlow).via(handleImage)
  }

  val print: Flow[HttpRequest, HttpResponse, NotUsed] = Flow[HttpRequest].mapAsync(1) { request =>
    request.entity.dataBytes.runWith(Sink.foreach(bs => println(bs.decodeString(StandardCharsets.UTF_8)))).map { _ =>
      HttpResponse(200)
    }
  }

}

object RegistrationImage {

  case class ResultDTO(
      tempPath: String,
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
