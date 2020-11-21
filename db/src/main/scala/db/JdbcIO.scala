package db

import java.sql.Connection

import cats.data.Kleisli
import cats.effect.IO

object JdbcIO {

  def apply[A](a: => A): JdbcIO[A] = Kleisli(_ => IO(a))

  def withConnection[A](f: Connection => A): JdbcIO[A] =
    Kleisli({ case (c, _) => IO(f(c)) })

}
