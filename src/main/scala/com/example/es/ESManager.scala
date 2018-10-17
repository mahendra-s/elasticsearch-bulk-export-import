package com.example.es

import com.example.util.Logging
import org.elasticsearch.client.{Client, RestHighLevelClient}

object ESManager extends Logging {
  def getClient: Client = (new ESConfig with TransportClientGenerator).getTransportClient
  def getRestClient: RestHighLevelClient = (new ESConfig with HighlevelRestClientGenerator).getRestClient
}


