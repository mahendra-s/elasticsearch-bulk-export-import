package com.example.es

import java.io.File

import com.example.util.Logging

object ImportProcessor extends App with Logging {

  if (args.length < 2)
    error(s"Please supply data directory and index name")
  else {
    val directory = args(0)
    val indexName = args(1)
    val client = ESManager.getClient

    val files = new File(directory).listFiles()
    info(s"reading files form ${new File(directory).toString}")
    if (files.length > 0)
      files.par.map {
        info(s"Import Processing started for ${files.length} files")
        file => new ImportWorker(client, ESUtility).startImport(file, indexName)
      }

  }
}
