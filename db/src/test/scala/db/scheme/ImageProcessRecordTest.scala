package db.scheme

import java.time.Instant
import java.util.UUID
import java.util.concurrent.Executors

import anorm.SqlStringInterpolation
import db.resource.{DataSourceFactory, JdbcConfig}
import db.{JdbcIO, JdbcIORunner, TestRollbackJdbcIORunner}
import javax.sql.DataSource
import org.scalatest.flatspec.AnyFlatSpec

import scala.concurrent.ExecutionContext

class ImageProcessRecordTest extends AnyFlatSpec {

  implicit val ec = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(4))

  val config                 = JdbcConfig.load()
  val dataSource: DataSource = DataSourceFactory.create(config)

  val runner: JdbcIORunner = new TestRollbackJdbcIORunner(dataSource, ec)

  it should "bar" in {

    val image        = ImageRecord(UUID.randomUUID(), "dummy-url", Instant.now())
    val imageProcess = ImageProcessRecord(image.id, success = true, Instant.now())

    val io: JdbcIO[ImageProcessRecord] = for {
      _ <- ImageRecord.insert(image)
      _ <- ImageProcessRecord.insert(imageProcess)
      record <- JdbcIO.withConnection { implicit c =>
        SQL"select * from ImageProcess where id = ${imageProcess.imageId.toString}".as(ImageProcessRecord.parser.single)
      }
    } yield record

    runner.runTx(io).unsafeToFuture().map { result => assert(result == imageProcess) }

  }

}
