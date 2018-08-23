package com.hexacta.app

import org.json4s.jackson.Json
import org.scalatra._

import scala.collection.mutable.ListBuffer

class MyScalatraServlet extends ScalatraServlet {

  get("/") {
    views.html.hello()
  }

  get(s"/contarAll/:str") {

    //word count
    val counts = List({
      params("str")
    }).flatMap(line => line.split(" "))
      .map(word => (word, 1))

    val countsRdd = SparkContext.getSc.parallelize(counts).reduceByKey(_ + _) //(a,b) => a + b

    countsRdd.foreach(println)
    Ok(Json.formatted(countsRdd.count().toString))

  }


  get(s"/contar/:str") {

    //word count
    val counts = List({
      params("str")
    }).flatMap(line => line.split(" "))
      .map(word => (word, 1))

    val countsRdd = SparkContext.getSc.parallelize(counts).reduceByKey(_ + _).collect()

    var result = ListBuffer[String]()

    countsRdd.foreach(line => result += line._1 + " , " + line._2)

    Ok(Json.formatted(result.toString()))

  }

}