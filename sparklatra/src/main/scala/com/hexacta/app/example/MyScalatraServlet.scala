package com.hexacta.app.example

import com.hexacta.app.SparkContext
import com.hexacta.app.HBaseContext
import org.json4s.JsonDSL._
import org.json4s.jackson.JsonMethods._
import org.scalatra._

import scala.collection.mutable.ListBuffer

import org.apache.spark.sql.{SQLContext, _}
import org.apache.spark.sql.datasources.hbase._
import org.apache.hadoop.hbase.spark.HBaseRDDFunctions._

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

    val conf : Configuration = HBaseContext.getConf()
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

  get(s"/testSparkHbase/:key/:value") {
    val conf : Configuration = HBaseContext.getConf()
    // cree la tabla
    // create 'TableTest', 'info'
    // put 'TableTest', 'rowkey1', 'info:test', 'ejemplo'
    val connection = ConnectionFactory.createConnection(conf)

    val hbaseContext = new org.apache.hadoop.hbase.spark.HBaseContext(SparkContext.getSc, conf)

    val tableName = "TableTest"
    val columnFamily = "info"

    val rdd = SparkContext.getSc.parallelize(Array(
      (Bytes.toBytes(params("key")),
        Array((Bytes.toBytes(columnFamily), Bytes.toBytes("test"), Bytes.toBytes(params("value")))))))

    hbaseContext.bulkPut[(Array[Byte], Array[(Array[Byte], Array[Byte], Array[Byte])])](rdd,
      TableName.valueOf(tableName),
      (putRecord) => {
        val put = new Put(putRecord._1)
        putRecord._2.foreach((putValue) =>
          put.addColumn(putValue._1, putValue._2, putValue._3))
        put
      });

    Ok("ok")
  }

  get(s"/testSparkHbase/:key") {

    val conf : Configuration = HBaseContext.getConf()
    // cree la tabla
    // create 'TableTest', 'info'
    // put 'TableTest', 'rowkey1', 'info:test', 'ejemplo'
    val connection = ConnectionFactory.createConnection(conf)

    val hbaseContext = new org.apache.hadoop.hbase.spark.HBaseContext(SparkContext.getSc, conf)

    val tableName = "TableTest"
    val columnFamily = "info"

    val rdd = SparkContext.getSc.parallelize(Array(Bytes.toBytes(params("key"))))

    val getRdd = rdd.hbaseBulkGet[String](hbaseContext, TableName.valueOf(tableName), 2,
      record => {
        System.out.println("making Get"+ record.toString)
        new Get(record)
      },
      (result: Result) => {

        val it = result.listCells().iterator()
        val b = new StringBuilder

        b.append(Bytes.toString(result.getRow) + ":")

        while (it.hasNext) {
          val cell = it.next()
          val q = Bytes.toString(CellUtil.cloneQualifier(cell))
          if (q.equals("counter")) {
            b.append("(" + q + "," + Bytes.toLong(CellUtil.cloneValue(cell)) + ")")
          } else {
            b.append("(" + q + "," + Bytes.toString(CellUtil.cloneValue(cell)) + ")")
          }
        }
        b.toString()
      })

    getRdd.collect().foreach(v => println(v))

    var result = ListBuffer[String]()

    getRdd.collect().foreach(v => result += v)

    Ok(compact(render(result)))
  }

  }