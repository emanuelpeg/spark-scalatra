package com.hexacta.app.input

import java.util

import com.hexacta.app.HBaseContext
import com.hexacta.app.model.{Change, Commit, Repo, User}
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


  get(s"/addMockUser/:username") {
    try {

      val change1 = new Change(5,4,6,"java", "filesha1")
      val change2 = new Change(3,4,6,"ts", "filesha2")

      var commit = new Commit("commmitsha")
      commit.changes.append(change1)
      commit.changes.append(change2)

      var repo = new Repo("repositorio")
      repo.commits.append(commit)

      var user = new User(params("username"))
      user.repos.append(repo)

      this.saveUser(user)

      Ok(user)

    } catch {
      case ioe: java.io.IOException => InternalServerError("Error! " + ioe.toString )
      case ste: java.net.SocketTimeoutException => InternalServerError("Error!"+ ste.toString)
    }
  }

  def saveUser(user: User): Unit = {

    // cree la tabla
    // create 'TableTest', 'info'
    // put 'TableTest', 'rowkey1', 'info:test', 'ejemplo'
    val connection = ConnectionFactory.createConnection(HBaseContext.getConf())
    val table = connection.getTable(TableName.valueOf( "changes" ))

    for {
      repo <- user.repos;
      commit <- repo.commits;
      change <- commit.changes
    } {
      val key = "%s:%s:%s:%s".format(user.userName, repo.name, commit.sha, change.fileSha)
      val put = new Put(Bytes.toBytes(key))

      put.addColumn(Bytes.toBytes("user"), Bytes.toBytes("name"), Bytes.toBytes(user.userName))
      put.addColumn(Bytes.toBytes("repo"), Bytes.toBytes("name"), Bytes.toBytes(repo.name))
      put.addColumn(Bytes.toBytes("commit"), Bytes.toBytes("sha"), Bytes.toBytes(commit.sha))
      put.addColumn(Bytes.toBytes("change"), Bytes.toBytes("filesha"), Bytes.toBytes(change.fileSha))
      put.addColumn(Bytes.toBytes("change"), Bytes.toBytes("delete"), Bytes.toBytes(change.delete.toString))
      put.addColumn(Bytes.toBytes("change"), Bytes.toBytes("addition"), Bytes.toBytes(change.addition.toString))
      put.addColumn(Bytes.toBytes("change"), Bytes.toBytes("change"), Bytes.toBytes(change.changes.toString))
      put.addColumn(Bytes.toBytes("change"), Bytes.toBytes("fileextension"), Bytes.toBytes(change.extension))

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