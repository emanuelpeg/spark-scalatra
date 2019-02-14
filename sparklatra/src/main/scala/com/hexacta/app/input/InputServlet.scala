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

import scala.collection.immutable.HashMap.HashTrieMap
import scala.collection.mutable.ListBuffer

class InputServlet extends ScalatraServlet {

  get("/") {

    val thread = new Thread {

      override def run {

          for(i <- 4 to 50) {
            try {
              val since = i * 10
              val content = getRest("https://api.github.com/users?since=%s&per_page=10&client_id=86b4a37b53d4a2bd0ef2&client_secret=b31dacf33683b6e9b4f5bf8540e89e430f1da879".format(since))
              val json = parse(content)
              val users = ListBuffer[User]()
              for {JArray(objList) <- json
                   JObject(obj) <- objList} {
                val kvList = for ((key, JString(value)) <- obj) yield (key, value)
                users.append(new User(kvList.toList(0)._2))
              }

              for (user <- users) {
                println(" +++ Starting : " + user)
                val contentRepo = getRest("https://api.github.com/users/%s/repos?type=all&client_id=86b4a37b53d4a2bd0ef2&client_secret=b31dacf33683b6e9b4f5bf8540e89e430f1da879".format(user.userName))
                val jsonRepo = parse(contentRepo)

                for {JArray(objList) <- jsonRepo
                     JObject(obj) <- objList
                } {
                  val kvList = for ((key, JString(value)) <- obj) yield (key, value)

                  val repo = new Repo(kvList.toList(2)._2)

                  val contentCommit = getRest("https://api.github.com/repos/%s/commits?author=%s&client_id=86b4a37b53d4a2bd0ef2&client_secret=b31dacf33683b6e9b4f5bf8540e89e430f1da879".format(repo.name, user.userName))
                  val jsonCommit = parse(contentCommit)

                  for {JArray(objList) <- jsonCommit
                       JObject(obj) <- objList
                  } {
                    val kvCommitList = for ((key, JString(value)) <- obj) yield (key, value)
                    val commit = new Commit(kvCommitList.toList(0)._2)

                    val contentChange = getRest("https://api.github.com/repos/%s/commits/%s?client_id=86b4a37b53d4a2bd0ef2&client_secret=b31dacf33683b6e9b4f5bf8540e89e430f1da879".format(repo.name, commit.sha))
                    val jsonChange = parse(contentChange)

                    for {JObject(obj) <- jsonChange
                    } {
                      if (obj.values.contains("files")) {
                        val jsonFiles = obj.values("files").asInstanceOf[scala.collection.immutable.List[HashTrieMap[String, Any]]]

                        if (!jsonFiles.isEmpty) {
                          val jsonFile = jsonFiles.iterator.next()
                          val change = new Change(jsonFile.get("additions").get.asInstanceOf[BigInt].toInt, jsonFile.get("deletions").get.asInstanceOf[BigInt].toInt, jsonFile.get("changes").get.asInstanceOf[BigInt].toInt, jsonFile.get("filename").get.asInstanceOf[String], jsonFile.get("sha").get.asInstanceOf[String])
                          commit.changes.append(change)
                        }

                      }

                    }
                    repo.commits.append(commit)
                  }
                  user.repos.append(repo)
                }
                saveUser(user)
                println(" +++ Se guardo : " + user)
                Thread.sleep(100)
              }

            } catch {
              case ioe: java.io.IOException => print("Error! " + ioe.toString)
              case ste: java.net.SocketTimeoutException => print("Error!" + ste.toString)
            }
          }
        }
    }

    thread.start

    Ok("Starting...")

  }


  get(s"/addMockUser/:username") {
    try {

      val change1 = new Change(5, 4, 6, "java", "filesha1")
      val change2 = new Change(3, 4, 6, "ts", "filesha2")

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
      case ioe: java.io.IOException => InternalServerError("Error! " + ioe.toString)
      case ste: java.net.SocketTimeoutException => InternalServerError("Error!" + ste.toString)
    }
  }

  def saveUser(user: User): Unit = {

    // cree la tabla
    // create 'TableTest', 'info'
    // put 'TableTest', 'rowkey1', 'info:test', 'ejemplo'
    val connection = ConnectionFactory.createConnection(HBaseContext.getConf())
    val table = connection.getTable(TableName.valueOf("changes"))

    for {
      repo <- user.repos
      commit <- repo.commits
      change <- commit.changes
    } {

      if (!user.userName.isEmpty && !repo.name.isEmpty && !commit.sha.isEmpty && !change.fileSha.isEmpty) {
        val key = "%s:%s:%s:%s".format(user.userName, repo.name, commit.sha, change.fileSha)
        //println(key)

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