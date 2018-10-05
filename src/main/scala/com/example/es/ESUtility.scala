package com.example.es

import com.example.util.Logging
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest
import org.elasticsearch.action.bulk.BulkResponse
import org.elasticsearch.action.index.IndexRequestBuilder
import org.elasticsearch.action.search.{ClearScrollResponse, SearchResponse}
import org.elasticsearch.client.Client
import org.elasticsearch.common.xcontent.XContentType
import org.elasticsearch.index.query.QueryBuilders._

trait ESUtility extends Logging {

  val retryWaitTime = 10 * 1000

  implicit val retryCount = 3

  def refreshIndex(client: Client, indexName: String): Boolean =
    withRetry{
      client.admin().indices().refresh(new RefreshRequest(indexName)).get
      true
    }


  def createSearchForDataExtraction(client: Client, actualIndex: String, extractSize: Int, scrollKeepAlive: String): SearchResponse =
    withRetry{
      val searchQuery = client.prepareSearch(actualIndex)
        .setSize(extractSize)
        .setQuery(boolQuery.must(matchAllQuery()))
        .setScroll(scrollKeepAlive)
      val searchResponse = searchQuery.get
      info(s"Response Time [ createSearchForBrandExtraction ] ==== [${searchResponse.getTook}]")
      searchResponse
    }

  def prepareSearchWithScroll(client: Client, scrollId: String, scrollKeepAlive: String): SearchResponse =
    withRetry{
      val searchResponse = client.prepareSearchScroll(scrollId).setScroll(scrollKeepAlive).get
      info(s"ResponseTime::: prepareSearchWithScroll---- [${searchResponse.getTook}ms]")
      searchResponse
    }

  def clearSearchScroll(client: Client, response: SearchResponse): ClearScrollResponse =
    withRetry{
      client.prepareClearScroll().addScrollId(response.getScrollId).get
    }

  def checkClusterHealth(client: Client): Byte =
    withRetry{
      client.admin.cluster().prepareClusterStats().get.getStatus.value
    }

  def getIndexRequest(client: Client, ingestIndex: String, docType: String, docId: String, json: String): IndexRequestBuilder =
    client.prepareIndex(ingestIndex, docType, docId)
      .setSource(json, XContentType.JSON)

  def getBulkResponse(client: Client, list: Iterable[IndexRequestBuilder]): BulkResponse =
    withRetry{
      val bulkRequest = client.prepareBulk()
      list.foreach {
        listItem => bulkRequest.add(listItem)
      }
      val searchResponse = bulkRequest.get
      info(s"ResponseTime::: getBulkResponse---- [${searchResponse.getTook}]")
      searchResponse
    }


  private def withRetry[T](block: => T)(implicit n: Int): T =
    try
      block
    catch {
      case ex: Throwable =>
        if (n > 0) {
          warn(s"Retry........ [Remaining retry count : $n] [Getting error : ${ex.getMessage}")
          Thread.sleep(retryWaitTime)
          withRetry(block)(n - 1)
        } else {
          throw ex
        }
    }
}

object ESUtility extends ESUtility