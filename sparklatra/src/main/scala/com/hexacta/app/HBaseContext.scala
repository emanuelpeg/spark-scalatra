package com.hexacta.app

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.hbase.HBaseConfiguration


object HBaseContext {

  val conf : Configuration = HBaseConfiguration.create()
  // Se debe agregar en el archivo host la siguiente entrada:
  // 10.60.1.31   quickstart.cloudera
  // 10.30.15.192 sandbox-hdp.hortonworks.com
  conf.set("hbase.zookeeper.quorum", "sandbox-hdp.hortonworks.com")
  conf.set("hbase.zookeeper.property.clientPort", "2181")

  conf.set("hbase.master", "sandbox-hdp.hortonworks.com:16000")
  conf.set("hbase.master.port", "16000") //16000
  conf.set("hbase.master.info.port", "16010")//16010
  conf.set("hbase.regionserver.info.port", "16030")
  conf.set("hbase.regionserver.port", "16020")
  conf.set("zookeeper.znode.parent", "/hbase-unsecure") //se configura en hortonworks porque corre en cluster

  def getConf() :Configuration = conf

}
