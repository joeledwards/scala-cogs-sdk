package io.cogswell.pubsub

import scala.concurrent.Future
import scala.concurrent.ExecutionContext

object CogsPubSub {
  def connect(
      keys: Seq[String], options: PubSubOptions
  )(implicit ec: ExecutionContext): Future[PubSubHandle] = {
    new PubSubHandle(keys, options) settle
  }
}