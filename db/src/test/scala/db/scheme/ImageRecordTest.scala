package db.scheme

import java.time.Instant
import java.util.UUID
import java.util.concurrent.Executors

import anorm.SqlStringInterpolation
import db.{JdbcIO, JdbcIORunner, TestRollbackJdbcIORunner}
import javax.sql.DataSource
import db.resource.{DataSourceFactory, JdbcConfig}
import org.scalatest.flatspec.AnyFlatSpec

import scala.concurrent.ExecutionContext

class ImageRecordTest extends AnyFlatSpec {

  implicit val ec = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(4))

  val config                 = JdbcConfig.load()
  val dataSource: DataSource = DataSourceFactory.create(config)

  val runner: JdbcIORunner = new TestRollbackJdbcIORunner(dataSource, ec)

  it should "bar" in {

    val image = ImageRecord(UUID.randomUUID(), "dummy-url", Instant.now())

    val io: JdbcIO[ImageRecord] = for {
      _ <- ImageRecord.insert(image)
      record <- JdbcIO.withConnection { implicit c =>
        SQL"select * from Images where id = ${image.id.toString}".as(ImageRecord.parser.single)
      }
    } yield record

    runner.runTx(io).unsafeToFuture().map { result => assert(result == image) }

  }

}
