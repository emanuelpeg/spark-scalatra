package com.hexacta.app.model

import scala.collection.mutable.ListBuffer

class Repo (val name: String) {

  val commits: ListBuffer[Commit] = ListBuffer[Commit]();

  override def toString: String = name

}
