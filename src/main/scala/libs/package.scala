/**
 * Created by yzark on 12/16/13.
 */

import com.typesafe.config.ConfigFactory
import scala.slick.driver.SQLiteDriver.simple._
import Database.threadLocalSession
import scala.slick.jdbc.{StaticQuery => Q}


package object libs
{
  var db_file = "blockchain/bitcoin.db"
  val conf = ConfigFactory.load()

  var stepClosure = conf.getInt("closureStep")
  var stepPopulate = conf.getInt("populateStep");

  def databaseSession(f: => Unit): Unit =
  {
    Database.forURL(
      url = "jdbc:sqlite:"+db_file,
      driver = "org.sqlite.JDBC"
    ) withSession
    {
      (Q.u + "PRAGMA main.page_size = 4096;           ").execute
      (Q.u + "PRAGMA main.cache_size=10000;           ").execute
      (Q.u + "PRAGMA main.locking_mode=EXCLUSIVE;     ").execute
      (Q.u + "PRAGMA main.synchronous=NORMAL;         ").execute
      (Q.u + "PRAGMA main.journal_mode=WAL;           ").execute
      (Q.u + "analyze;                                ").execute
      f
    }

  }


}
