package db.scheme

import java.time.Instant
import java.util.UUID

import anorm.{RowParser, SqlParser, SqlStringInterpolation}
import db.JdbcIO

case class ImageProcessRecord(
    imageId: UUID,
    success: Boolean,
    createdAt: Instant
)

object ImageProcessRecord {

  val parser: RowParser[ImageProcessRecord] = for {
    imageId   <- SqlParser.str("imageId").map(UUID.fromString)
    success   <- SqlParser.bool("success")
    createdAt <- SqlParser.date("createdAt").map(_.toInstant)
  } yield ImageProcessRecord(
    imageId = imageId,
    success = success,
    createdAt = createdAt
  )

  def insert(record: ImageProcessRecord): JdbcIO[Unit] = JdbcIO.withConnection { implicit c =>
    import record._
    SQL"""insert into ImageProcess (imageId, success, createdAt) values ($imageId, $success, $createdAt)"""
      .executeUpdate()
  }

}
