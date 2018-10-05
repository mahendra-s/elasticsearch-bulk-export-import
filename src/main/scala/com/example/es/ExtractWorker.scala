package com.example.es

import java.io.{File, FileOutputStream, OutputStreamWriter}
import java.util.UUID

import com.example.util.Logging
import org.elasticsearch.client.Client

import scala.util.control.NonFatal

class ExtractWorker(client: Client, esUtility: ESUtility) extends Logging {

  private val extractSize = 200
  private val scrollKeepAlive = "1m"
  private val RED_CLUSTER_STATUS = 2
  private val NEW_LINE = "\n"

  def startExtract(directory: String, indexName: String): List[String] = {
    info(s"Starting extraction from alias/index ==== [$indexName] ")
    try {
      if (esUtility.checkClusterHealth(client) != RED_CLUSTER_STATUS) {
        esUtility.refreshIndex(client, indexName)
        extractJson(directory, indexName)
      } else {
        throw new IllegalArgumentException("Cluster health is not yellow or green")
      }
    } catch {
      case NonFatal(th) =>
        error(s"Some error has occurred... Aborting extract: ", th)
        throw th
    }
  }

  private def extractJson(directory: String, indexName: String): List[String] = {
    val outputFiles = scala.collection.mutable.ListBuffer[String]()
    info(s"Extract Json file in destination ==== $directory")
    createIfNotExist(s"$directory")
    var scrollResponse =
      esUtility.createSearchForDataExtraction(client, indexName, extractSize, scrollKeepAlive)
    var keepGoing = true
    var (key, writer) = createFileWriter(directory)
    while (keepGoing) {
      try {
        if (scrollResponse.getHits.getHits.length != 0) {
          val documents = new StringBuilder()
          scrollResponse.getHits.getHits.foreach { document => documents.append(document.getSourceAsString).append(NEW_LINE) }
          val documentsToBeWrite = documents.toString()
          writer.write(documentsToBeWrite)
          outputFiles.append(key)
          flushAndClose(writer)
          val (newKey, newFileWriter) = createFileWriter(directory)
          key = newKey
          writer = newFileWriter
        } else {
          info(s"Number of Extracted File ==== ${outputFiles.distinct.size}")
          keepGoing = false
          flushAndClose(writer)
        }
        scrollResponse = esUtility.prepareSearchWithScroll(client, scrollResponse.getScrollId, scrollKeepAlive)
      } catch {
        case NonFatal(th) =>
          error(s"Error occurred in es data extraction: ", th)
      }
    }
    outputFiles.toList.distinct
  }

  private def flushAndClose(writer: OutputStreamWriter): Unit = {
    writer.flush()
    writer.close()
  }

  private def createFileWriter(directory: String): (String, OutputStreamWriter) = {
    val fileName = UUID.randomUUID() + ".extract.json"
    val key = s"$directory/$fileName"
    val outputFile = new File(key)
    (key, new OutputStreamWriter(new FileOutputStream(outputFile)))
  }


  private def createIfNotExist(dir: String): Unit = {
    val file = new File(dir)
    if (file.exists() && file.isDirectory) {
      info("Dir already exist ")
    } else {
      info(s"creating dir $dir")
      file.mkdirs()
    }
  }

}
