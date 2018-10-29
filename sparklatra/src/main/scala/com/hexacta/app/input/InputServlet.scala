package com.hexacta.app.input

import com.hexacta.app.model.{Repo, User}
import org.json4s.JsonAST.{JArray, JObject, JString}
import org.scalatra._
import org.json4s.JsonDSL._
import org.json4s.jackson.JsonMethods._

class InputServlet extends ScalatraServlet {

  get("/") {
    try {
       val content = getRest("https://api.github.com/users?since=0")
       val json = parse(content)
       var users = List[User]()
       for {JArray(objList) <- json
                       JObject(obj) <- objList
                       } {
                 val kvList = for ((key, JString(value)) <- obj) yield (key, value)
          users =  new User(kvList.toList(0)._2) :: users
        }
      for  (user <- users) {
        val contentUser = getRest("https://api.github.com/users/%s/repos".format(user.userName))
        val jsonUser = parse(contentUser)

        for {JArray(objList) <- jsonUser
             JObject(obj) <- objList
        } {
          val kvList = for ((key, JString(value)) <- obj) yield (key, value)
          user.repos =  new Repo(kvList.toList(0)._2) :: user.repos
        }
      }
      Ok(users)
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