package com.hexacta.app.output

import com.hexacta.app.model.{Repo, User}
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.hbase.client.ConnectionFactory
import org.apache.hadoop.hbase.{HBaseConfiguration, TableName}
import org.json4s.JsonAST.{JArray, JObject, JString}
import org.json4s.jackson.JsonMethods._
import org.scalatra._

class GitHubServlet extends ScalatraServlet {

  get("/") {
    try {
      val conf : Configuration = HBaseConfiguration.create()
      // Se debe agregar en el archivo host la siguiente entrada:
      // 10.60.1.27   quickstart.cloudera
      conf.set("hbase.zookeeper.quorum", "10.60.1.50")
      conf.set("hbase.zookeeper.property.clientPort", "2181")

      conf.set("hbase.master.port", "60000") //16000
      conf.set("hbase.master.info.port", "60010")//16010
      conf.set("hbase.regionserver.info.port", "60030")
      conf.set("hbase.regionserver.port", "60020")

      val connection = ConnectionFactory.createConnection(conf)
      val table = connection.getTable(TableName.valueOf( "changes" ) )

      // Read data

    } catch {
      case ioe: java.io.IOException =>  InternalServerError("Error!")
      case ste: java.net.SocketTimeoutException =>  InternalServerError("Error!")
    }
  }


  @throws(classOf[java.io.IOException])
  @throws(classOf[java.net.SocketTimeoutException])
  def getRest(url: String,
          connectTimeout: Int = 5000,
          readTimeout: Int = 5000,
          requestMethod: String = "GET") =
  {
    import java.net.{HttpURLConnection, URL}
    val connection = (new URL(url)).openConnection.asInstanceOf[HttpURLConnection]
    connection.setConnectTimeout(connectTimeout)
    connection.setReadTimeout(readTimeout)
    connection.setRequestMethod(requestMethod)
    val inputStream = connection.getInputStream
    val content = scala.io.Source.fromInputStream(inputStream).mkString
    if (inputStream != null) inputStream.close
    content
  }

}