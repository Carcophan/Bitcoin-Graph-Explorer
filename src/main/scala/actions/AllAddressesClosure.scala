package actions

/**
 * Created with IntelliJ IDEA.
 * User: yzark
 * Date: 11/19/13
 * Time: 1:03 PM
 * To change this template use File | Settings | File Templates.
 */
import libs._
import scala.slick.driver.MySQLDriver.simple._
import scala.slick.session.Database
import Database.threadLocalSession
import scala.slick.jdbc.{GetResult, StaticQuery => Q}
import scala.slick.jdbc.meta.MTable
import scala.collection.mutable.HashMap

class AllAddressesClosure(args:List[String]){

  def generateTree (firstElement: Int, elements: Int): HashMap[Array[Byte], DisjointSetOfAddresses]  =
  {

    val mapDSOA:HashMap[Array[Byte], DisjointSetOfAddresses] = HashMap.empty
    val mapAddresses:HashMap[Array[Byte], Array[Array[Byte]]] = HashMap.empty
    var startTime = System.currentTimeMillis

    val query = """ select spent_in_transaction_hash, address from
        movements where spent_in_transaction_hash NOT NULL limit """ + firstElement + ',' + elements + """ ; """
        
    println("Reading " +elements+ " elements")

    implicit val GetByteArr = GetResult(r => r.nextBytes())
    val q2 = Q.queryNA[(Array[Byte],Array[Byte])](query)

    for (t <- q2) if (t._2 != "0")
    {
      val list:Array[Array[Byte]] = mapAddresses.getOrElse(t._1, Array()  )
      mapAddresses.update(t._1, list :+ t._2 )
    }

    println("Data read in "+ (System.currentTimeMillis - startTime )+" ms")
    println("Calculating address dependencies...")
    startTime = System.currentTimeMillis

    for (t <- mapAddresses)
    {
      val dSOAs= t._2 map(a => mapDSOA.getOrElseUpdate(a, {DisjointSetOfAddresses(a)}) )

      def union(l:Array[DisjointSetOfAddresses]): Unit = l match
      {
        case Array() =>
        case Array(x) =>
        case ar => ar(0).union(ar(1)) ; union(ar.drop(1))
      }

      union(dSOAs)
    }

    println("")
    mapDSOA

  }

  def adaptTreeToDB(mapDSOA: HashMap[Array[Byte], DisjointSetOfAddresses]): HashMap[Array[Byte], DisjointSetOfAddresses] =
  {
    for ( (address, dsoa) <- mapDSOA)
    {
      // weird trick to allow slick using Array Bytes
      implicit val GetByteArr = GetResult(r => r.nextBytes())
      Q.queryNA[Array[Byte]]("""select representant from addresses where hash= """"+address+"""";""").list match
      {
        case representant::xs =>
          dsoa.find.parent = Some(DisjointSetOfAddresses(representant))
          mapDSOA remove address
        case _  =>
      }
    }

    mapDSOA
  }

  def saveTree(mapDSOA: HashMap[Array[Byte], DisjointSetOfAddresses]): Unit =
  {
    val values = (mapDSOA map ( p => ('"'+ p._1.toString + '"' , '"' + p._2.find.address.toString +'"', 0.toDouble))).toList
    println("Copying results to the database...")
    (Q.u + "BEGIN TRANSACTION;").execute

    for (value <- values )
    {
      println(value)
      (Q.u + """insert into addresses values """+ value.toString +""";""").execute
    }

    (Q.u + "COMMIT TRANSACTION;").execute
  }

  databaseSession
  {
    val start = if (args.length>0) args(0).toInt else 0
    val end = if (args.length>1) args(1).toInt else countInputs
    var i = start

    for (i <- start to end by stepClosure)
    {
      println("Closuring inputs from " + i + " to " + ( i + stepClosure ) )
      saveTree(adaptTreeToDB(generateTree(i, stepClosure)))
    }

    println("Wir sind supergeil!!!")
  }
}
