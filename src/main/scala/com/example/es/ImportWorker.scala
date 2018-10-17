package com.example.es

import java.io.File

import com.example.json.JsonHelper._
import com.example.util.Logging
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.client.RestHighLevelClient

import scala.collection.mutable.ListBuffer
import scala.io.Source
import scala.util.control.NonFatal

class ImportWorker(client: RestHighLevelClient, esUtility: ESUtility) extends Logging {

  val batchSize = 1600

  def startImport(importFile: File, queryIndex: String): Int = {
    info(s"Starting import for ${importFile.getName} . ...")
    val listBuffer = scala.collection.mutable.ListBuffer[IndexRequest]()
    val insertResponse: Int =
      try {
        val count = Source.fromFile(importFile).getLines().foldLeft(0) { case (successCount, oneLine) =>
          parseJson(oneLine, queryIndex).foreach(listBuffer.append(_))
          if (listBuffer.size >= batchSize) {
            val count = successCount + insert(listBuffer)
            listBuffer.clear()
            count
          } else {
            successCount
          }

        }
        if (listBuffer.isEmpty) count else count + insert(listBuffer)
      } catch {
        case NonFatal(th) =>
          warn("Some error has occurred... Aborting insert  ", th)
          0
      }
    info(s"Completed file ${importFile.getName}.. SuccessCount:$insertResponse")
    insertResponse
  }

  private def insert(list: ListBuffer[IndexRequest]): Int = {
    info(s"Inserting ${list.size} documents into ElasticSearch")
    if (list.nonEmpty) {
      val bulkResponse = esUtility.getBulkResponse(client, list)
      bulkResponse.getItems.count(!_.isFailed)
    } else {
      0
    }
  }

  private def parseJson(oneLine: String, index: String): Option[IndexRequest] =
    try {
      val parsedLine = parse(oneLine)
      val docId = (parsedLine \ "id").extract[String]
      Some(esUtility.getIndexRequest(client, index, "my_data", docId, oneLine))
    } catch {
      case ex: Exception =>
        warn(s"Error in parsing json [$oneLine]", ex)
        None
    }


}
