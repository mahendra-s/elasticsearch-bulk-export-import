package com.example.es

import com.example.util.Logging
import org.elasticsearch.client.Client

object ExtractProcessor extends App with Logging {

  if (args.length < 1)
    error(s"Please provide index name")
  else {
    val indexName = args(0)
    val directory = s"data/$indexName"
    def client: Client = ESManager.getClient

    val extractWorker = new ExtractWorker(client, ESUtility)

    val extractedFiles = extractWorker.startExtract(directory, indexName)

    info("Extraction completed " + extractedFiles.length)
  }

}
