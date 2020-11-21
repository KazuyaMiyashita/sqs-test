package db.resource

import com.mysql.cj.jdbc.MysqlDataSource
import javax.sql.DataSource

object DataSourceFactory {

  def create(config: JdbcConfig): DataSource = {
    val ds: MysqlDataSource = new MysqlDataSource()
    ds.setUrl(config.url)
    ds.setUser(config.user)
    ds.setPassword(config.password)
    ds
  }

}
