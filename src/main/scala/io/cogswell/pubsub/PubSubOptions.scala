package io.cogswell.pubsub

import io.cogswell.util.LogLevel
import io.cogswell.util.LogLevelError

case class PubSubOptions (
  url: String,
  autoReconnect: Boolean = true
)

object PubSubOptions {
  def default: PubSubOptions = PubSubOptions(
      "wss://api.cogswell.io/pubsub"
  )
}