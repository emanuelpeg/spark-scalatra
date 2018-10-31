package com.hexacta.app.input

import java.util

import com.hexacta.app.model.{Change, Repo, User}
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.hbase.client.{ConnectionFactory, Put}
import org.apache.hadoop.hbase.util.Bytes
import org.apache.hadoop.hbase.{HBaseConfiguration, TableName}
import org.json4s.JsonAST.{JArray, JObject, JString}
import org.scalatra._
import org.json4s.JsonDSL._
import org.json4s.jackson.JsonMethods._

import scala.collection.mutable.ListBuffer

class InputServlet extends ScalatraServlet {

  get("/") {
    try {


      val content = getRest("https://api.github.com/users?since=0")
      val json = parse(content)
      var users = ListBuffer[User]()
      for {JArray(objList) <- json
           JObject(obj) <- objList} {
        val kvList = for ((key, JString(value)) <- obj) yield (key, value)
        users.append(new User(kvList.toList(0)._2))
      }

      for (user <- users) {
        val contentUser = getRest("https://api.github.com/users/%s/repos".format(user.userName))
        val jsonUser = parse(contentUser)

        for {JArray(objList) <- jsonUser
             JObject(obj) <- objList
        } {
          val kvList = for ((key, JString(value)) <- obj) yield (key, value)
          user.repos.append(new Repo(kvList.toList(2)._2))
        }
      }
      Ok(users)
    } catch {
      case ioe: java.io.IOException => InternalServerError("Error! " + ioe.toString )
      case ste: java.net.SocketTimeoutException => InternalServerError("Error!"+ ste.toString)
    }
  }

  def saveUser(user: User): Unit = {
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
    val table = connection.getTable(TableName.valueOf( "changes" ))

    for {
      repo <- user.repos;
      commit <- repo.commits;
      change <- commit.changes
    } {
      val key = "%s:%s:%s:%s".format(user.userName, repo.name, commit.sha, change.fileSha)
      val put = new Put(Bytes.toBytes(params(key)))

      put.addColumn(Bytes.toBytes("user"), Bytes.toBytes("name"), Bytes.toBytes(params(user.userName)))
      put.addColumn(Bytes.toBytes("repo"), Bytes.toBytes("name"), Bytes.toBytes(params(repo.name)))
      put.addColumn(Bytes.toBytes("commit"), Bytes.toBytes("sha"), Bytes.toBytes(params(commit.sha)))
      put.addColumn(Bytes.toBytes("change"), Bytes.toBytes("filesha"), Bytes.toBytes(params(change.fileSha)))
      put.addColumn(Bytes.toBytes("change"), Bytes.toBytes("delete"), Bytes.toBytes(params(change.delete.toString)))
      put.addColumn(Bytes.toBytes("change"), Bytes.toBytes("addition"), Bytes.toBytes(params(change.addition.toString)))
      put.addColumn(Bytes.toBytes("change"), Bytes.toBytes("change"), Bytes.toBytes(params(change.changes.toString)))
      put.addColumn(Bytes.toBytes("change"), Bytes.toBytes("fileextension"), Bytes.toBytes(params(change.extension)))

      table.put(put)
    }
  }

  @throws(classOf[java.io.IOException])
  @throws(classOf[java.net.SocketTimeoutException])
  def getRest(url: String,
              connectTimeout: Int = 5000,
              readTimeout: Int = 5000,
              requestMethod: String = "GET") = {
    import java.net.{URL, HttpURLConnection}
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