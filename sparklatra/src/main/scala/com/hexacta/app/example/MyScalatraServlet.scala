package com.hexacta.app.example

import com.hexacta.app.SparkContext
import org.json4s.JsonDSL._
import org.json4s.jackson.JsonMethods._
import org.scalatra._

import scala.collection.mutable.ListBuffer

import org.apache.hadoop.hbase.client._
import org.apache.hadoop.hbase.util.Bytes
import org.apache.hadoop.hbase.{CellUtil, HBaseConfiguration, TableName}
import org.apache.hadoop.conf.Configuration

class MyScalatraServlet extends ScalatraServlet {

  get("/") {
   // views.html.hello()
    "HOLA HX!!"
  }

  get(s"/contarAll/:str") {

    //word count
    val counts = List({
      params("str")
    }).flatMap(line => line.split(" "))
      .map(word => (word, 1))

    val countsRdd = SparkContext.getSc.parallelize(counts).reduceByKey(_ + _) //(a,b) => a + b

    countsRdd.foreach(println)
    Ok(compact(render(countsRdd.count())))

  }


  get(s"/contar/:str") {

    //word count
    val counts = List({
      params("str")
    }).flatMap(line => line.split(" "))
      .map(word => (word, 1))

    val countsRdd = SparkContext.getSc.parallelize(counts).reduceByKey(_ + _).collect()

    var result = ListBuffer[(String, Int)]()

    countsRdd.foreach(line => result += line)

    Ok(compact(render(result)))

  }

  get(s"/guardar/:rowKey/:str") {

    val conf : Configuration = HBaseConfiguration.create()
    // Se debe agregar en el archivo host la siguiente entrada:
    // 10.60.1.27   quickstart.cloudera
    conf.set("hbase.zookeeper.quorum", "quickstart.cloudera")
    conf.set("hbase.zookeeper.property.clientPort", "2181")

    conf.set("hbase.master.port", "60000") //16000
    conf.set("hbase.master.info.port", "60010")//16010
    conf.set("hbase.regionserver.info.port", "60030")
    conf.set("hbase.regionserver.port", "60020")
    // cree la tabla
    // create 'TableTest', 'info'
    // put 'TableTest', 'rowkey1', 'info:test', 'ejemplo'
    val connection = ConnectionFactory.createConnection(conf)
    val table = connection.getTable(TableName.valueOf( "TableTest" ) )

    // Put example
    val put = new Put(Bytes.toBytes(params("rowKey")))
    put.addColumn(Bytes.toBytes("info"), Bytes.toBytes("test"), Bytes.toBytes(params("str")))

    table.put(put)

    Ok("ok")
  }

}