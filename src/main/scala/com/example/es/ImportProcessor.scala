package com.example.es

import java.io.File

import com.example.util.Logging
import org.elasticsearch.client.{Client, RestHighLevelClient}

import scala.util.Try

object ImportProcessor extends App with Logging {

  if (args.length < 2)
    error(s"Please supply data directory and index name")
  else {
    val directory = args(0)
    val indexName = args(1)
    val client: RestHighLevelClient = ESManager.getRestClient
    try {
      val files = new File(directory).listFiles()
      info(s"reading files form ${new File(directory).toString}")
      if (files.length > 0)
        files.par.map {
          info(s"Import Processing started for ${files.length} files")
          file => new ImportWorker(client, ESUtility).startImport(file, indexName)
        }
      info("Import process completed")
    }
    finally {
    client.close()
      info("connection closed")
    }
  }
}
