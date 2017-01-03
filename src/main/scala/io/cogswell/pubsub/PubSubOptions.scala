package io.cogswell.pubsub

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.Duration

case class PubSubOptions (
  url: String,
  autoReconnect: Boolean = true,
  connectTimeout: Duration = Duration(30, TimeUnit.SECONDS)
)

object PubSubOptions {
  def default: PubSubOptions = PubSubOptions(
      "wss://api.cogswell.io/pubsub"
  )
}