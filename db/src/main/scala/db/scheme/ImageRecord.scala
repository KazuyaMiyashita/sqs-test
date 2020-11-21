package db.scheme

import java.time.Instant
import java.util.UUID

import anorm.{RowParser, SqlParser, SqlStringInterpolation}
import db.JdbcIO

case class ImageRecord(
    id: UUID,
    url: String,
    createdAt: Instant
)

object ImageRecord {

  val parser: RowParser[ImageRecord] = for {
    id        <- SqlParser.str("id").map(UUID.fromString)
    url       <- SqlParser.str("url")
    createdAt <- SqlParser.date("createdAt").map(_.toInstant)
  } yield ImageRecord(
    id = id,
    url = url,
    createdAt = createdAt
  )

  def insert(record: ImageRecord): JdbcIO[Unit] = JdbcIO.withConnection { implicit c =>
    import record._
    SQL"""insert into Images (id, path, createdAt) values ($id, $url, $createdAt)""".executeUpdate()
  }

}
