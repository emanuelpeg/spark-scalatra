package com.hexacta.app.input

import org.scalatra._


class InputServlet extends ScalatraServlet {

  get("/") {
    try {
      val content = getRest("https://api.github.com/users?since=0")
      println(content)
      Ok(content)
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