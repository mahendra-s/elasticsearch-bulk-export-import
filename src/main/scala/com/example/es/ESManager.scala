package com.example.es

import com.example.util.Logging
import org.elasticsearch.client.Client

object ESManager extends Logging {
  def getClient: Client =
    ThreadLocal.withInitial[Client] {
      // needs to be synchronized because netty gets upset when you create concurrently too
      () =>
        ImportProcessor.synchronized {
          (new ESConfig with TransportClientGenerator).getTransportClient
        }
    }.get

}


