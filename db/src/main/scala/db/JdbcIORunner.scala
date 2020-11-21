package db

import cats.effect.IO
import javax.sql.DataSource

import scala.concurrent.ExecutionContext

trait JdbcIORunner {
  def runTx[A](io: JdbcIO[A]): IO[A]
}

class DefaultJdbcIORunner(dataSource: DataSource, ec: ExecutionContext) extends JdbcIORunner {

  override def runTx[A](io: JdbcIO[A]): IO[A] = {
    for {
      connection <- IO(dataSource.getConnection())
      _          <- IO(connection.setAutoCommit(false))
      result <- io
        .run((connection, ec))
        .redeemWith(
          { e => connection.rollback(); IO.raiseError(e) }, { other: A => connection.commit(); IO(other) }
        )
        .guarantee(IO(connection.close()))
    } yield result
  }

}

class TestRollbackJdbcIORunner(dataSource: DataSource, ec: ExecutionContext) extends JdbcIORunner {

  override def runTx[A](io: JdbcIO[A]): IO[A] = {
    for {
      connection <- IO(dataSource.getConnection())
      _          <- IO(connection.setAutoCommit(false))
      result <- io
        .run((connection, ec))
        .redeemWith(
          { e => connection.rollback(); IO.raiseError(e) }, { other: A => connection.rollback(); IO(other) }
        )
        .guarantee(IO(connection.close()))
    } yield result
  }

}
