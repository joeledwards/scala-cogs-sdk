package io.cogswell.pubsub

import scala.concurrent.Future
import scala.concurrent.ExecutionContext

object CogsPubSub {
  /**
   * Open a connection to the Cogswell Pub/Sub system.
   * 
   * @param keys the auth keys for the connection
   * @param options the PubSubOptions for customization
   * 
   * @return a Future which, if successful, will contain a new PubSubHandle
   * for interacting with the server
   */
  def connect(
      keys: Seq[String], options: PubSubOptions = PubSubOptions.default
  )(implicit ec: ExecutionContext): Future[PubSubHandle] = {
    new PubSubHandle(keys, options) settle
  }
}
