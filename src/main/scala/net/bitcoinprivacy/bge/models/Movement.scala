package net.bitcoinprivacy.bge.models


import scala.slick.driver.PostgresDriver.simple._
import scala.slick.jdbc.JdbcBackend.Database.dynamicSession
import scala.slick.jdbc.{ StaticQuery => Q }
import scala.slick.jdbc.meta.MTable
import util.Hash

case class Movement(tx: String, value:Long, spentInTx: String)

object Movement extends core.BitcoinDB
{
  def getMovements(address: Array[Byte], page: Int) =
    transactionDBSession{

      val mvmnts = for (b<- movements.filter(_.address===address).drop((page-1)*1000).take(1000)) 
                       yield (b.transaction_hash ,b.value, b.spent_in_transaction_hash)

      mvmnts.run.toVector map (p => Movement(Hash(p._1).toString, p._2, Hash(p._3).toString))

    }

  def getInputs(transactionHash: Array[Byte], page: Int) =
    transactionDBSession{

      val inputs = for (b<-movements.filter(_.spent_in_transaction_hash === transactionHash))
                   yield (b.transaction_hash, b.value, b.spent_in_transaction_hash )

      inputs.run.toVector map (p => Movement(Hash(p._1).toString, p._2, Hash(p._3).toString))
      

    }

  def getOutputs(transactionHash: Array[Byte], page: Int) =
    transactionDBSession {
      val outputs = for(b<-movements.filter(_.transaction_hash===transactionHash))
                    yield (b.transaction_hash, b.value, b.spent_in_transaction_hash )

     
      outputs.run.toVector map (p => Movement(Hash(p._1).toString, p._2, Hash(p._3).toString))



    }
    
  
}



