package com.example.es

import java.net.InetAddress

import com.example.util.Logging
import org.apache.http.HttpHost
import org.apache.http.auth.{AuthScope, UsernamePasswordCredentials}
import org.apache.http.impl.client.BasicCredentialsProvider
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder
import org.elasticsearch.client.{RestClient, RestClientBuilder, RestHighLevelClient}

trait HighlevelRestClientGenerator extends Logging {
  val clusterName: String
  val nodes: Array[String]
  val restport: Int
  val user: String
  val password: String
  val enableSsl: Boolean

  def getRestClient: RestHighLevelClient = {
    val credentialsProvider = new BasicCredentialsProvider
    credentialsProvider.setCredentials(AuthScope.ANY,
      new UsernamePasswordCredentials(user, password))

    val httpHosts: Array[HttpHost] = nodes.flatMap { host => InetAddress.getAllByName(host) }.map {
      new HttpHost(_, restport,if(enableSsl) "https" else "http")
    }
    val builder: RestClientBuilder = RestClient.builder(httpHosts: _*)
      builder.setHttpClientConfigCallback(
        new RestClientBuilder.HttpClientConfigCallback() {
          override def customizeHttpClient(httpClientBuilder: HttpAsyncClientBuilder): HttpAsyncClientBuilder =
          httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider)
      })


    val client = new RestHighLevelClient(builder)
    info(s"ElasticClient config => Nodes = ${nodes.toList} , port = $restport clusterName $clusterName")
    client
  }
}
