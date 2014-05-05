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
  //var stepClosure = 25000
  //var stepPopulate = 100000

  val conf = ConfigFactory.load()

  var stepClosure = conf.getInt("closureStep")
  var stepPopulate = conf.getInt("populateStep");

  def databaseSession(f: => Unit): Unit = {
    Database.forURL(
      url = "jdbc:sqlite:"+db_file,
      driver = "org.sqlite.JDBC"
     // user = "root",
      //password = "12345"
    ) withSession
    {
      (Q.u + "PRAGMA main.page_size = 4096;  ").execute
      (Q.u + "PRAGMA main.cache_size=10000;          ").execute
      (Q.u + "PRAGMA main.locking_mode=EXCLUSIVE;").execute
      (Q.u + "PRAGMA main.synchronous=NORMAL;").execute
      (Q.u + "PRAGMA main.journal_mode=WAL;").execute
      //(Q.u + "PRAGMA main.cache_size=5000;").execute
     // (Q.u + "PRAGMA temp_store=OFF").execute
   //   (Q.u + "PRAGMA page_size = 39600;").execute
   //   (Q.u + "PRAGMA journal_mode=off;").execute
  //    (Q.u + "PRAGMA synchronous=0;").execute
//      (Q.u + "PRAGMA cache_size= 39600;").execute
      f
    }

  }


}
