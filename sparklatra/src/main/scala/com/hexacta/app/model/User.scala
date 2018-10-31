package com.hexacta.app.model

import scala.collection.mutable.ListBuffer

class User (val userName:String){

  var repos: ListBuffer[Repo] = ListBuffer[Repo]();

  override def toString: String = userName + " repos : " + repos.mkString(" , ")

  //https://api.github.com/users/cotyq/repos?type=all

  //https://api.github.com/repos/emanuelpeg/spark-scalatra/commits?author=emanuelpeg

  //https://developer.github.com/v3/repos/commits/

}
