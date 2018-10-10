package com.example.es

import java.net.InetAddress

import com.example.util.Logging
import org.elasticsearch.client.Client
import org.elasticsearch.common.settings.Settings
import org.elasticsearch.common.transport.TransportAddress
import org.elasticsearch.xpack.client.PreBuiltXPackTransportClient

trait TransportClientGenerator extends Logging {
  val clusterName: String
  val nodes: Array[String]
  val port: Int
  val user: String
  val password: String
  val enableSsl: Boolean

  def getTransportClient: Client = {
    val addresses = nodes.flatMap { host => InetAddress.getAllByName(host) }.map {
      new TransportAddress(_, port)
    }
    val client = new PreBuiltXPackTransportClient(settings).addTransportAddresses(addresses: _*)
    info(s"ElasticClient config => Nodes = ${nodes.toList} , port = $port clusterName $clusterName")
    client
  }

  private def settings = Settings.builder()
    .put("client.transport.nodes_sampler_interval", "5s")
    .put("client.transport.sniff", false)
    .put("transport.tcp.compress", true)
    .put("xpack.security.transport.ssl.enabled", enableSsl)
    .put("cluster.name", clusterName)
    .put("request.headers.X-Found-Cluster", "${cluster.name}")
    .put("xpack.security.user", s"$user:$password")
    .build
}