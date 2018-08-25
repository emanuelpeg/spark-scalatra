package com.hexacta.app

import org.json4s.jackson.Json
import org.scalatra._
import org.json4s.jackson.JsonMethods._
import org.json4s.JsonDSL._

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

    var result = ListBuffer[(String, Int)]()

    countsRdd.foreach(line => result += line)

    Ok(compact(render(result)))

  }

}