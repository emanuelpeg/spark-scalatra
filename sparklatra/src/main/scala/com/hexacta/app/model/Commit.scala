package com.hexacta.app.model

import scala.collection.mutable.ListBuffer

class Commit (val sha: String){

  val changes: ListBuffer[Change] = ListBuffer[Change]();

}
