package io.cogswell.pubsub

import play.api.libs.json.JsValue
import scala.concurrent.Future

class PubSubSocket(
    val keys: Seq[String],
    val options: PubSubOptions = PubSubOptions.default
) {
  def connect(): Future[Unit] = {
    Future.successful()
  }
  
  def close(): Unit = {
    
  }
  
  def send(json: JsValue): Unit = {
    
  }
}