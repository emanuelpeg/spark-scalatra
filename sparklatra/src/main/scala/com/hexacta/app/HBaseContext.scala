package com.hexacta.app

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.hbase.HBaseConfiguration


object HBaseContext {

  val conf : Configuration = HBaseConfiguration.create()
  // Se debe agregar en el archivo host la siguiente entrada:
  // 10.60.1.27   quickstart.cloudera
  conf.set("hbase.zookeeper.quorum", "quickstart.cloudera")
  conf.set("hbase.zookeeper.property.clientPort", "2181")

  conf.set("hbase.master.port", "60000") //16000
  conf.set("hbase.master.info.port", "60010")//16010
  conf.set("hbase.regionserver.info.port", "60030")
  conf.set("hbase.regionserver.port", "60020")

  def getConf() :Configuration = conf

}
