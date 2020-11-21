package db.resource

import com.typesafe.config.ConfigFactory

case class JdbcConfig(
    url: String,
    user: String,
    password: String
)

object JdbcConfig {

  def load(): JdbcConfig = {
    val config = ConfigFactory.load()
    JdbcConfig(
      config.getString("db.url"),
      config.getString("db.user"),
      config.getString("db.password")
    )
  }

}
