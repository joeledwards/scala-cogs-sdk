package io.cogswell.pubsub

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.Duration
import java.util.UUID

case class PubSubOptions (
  url: String,
  autoReconnect: Boolean = true,
  connectTimeout: Duration = Duration(30, TimeUnit.SECONDS),
  messageHandler: Option[PubSubHandlers.MessageHandler] = None,
  eventHandler: Option[PubSubHandlers.EventHandler] = None,
  sessionUuid: Option[UUID] = None
)

object PubSubOptions {
  def default: PubSubOptions = PubSubOptions(
      "wss://api.cogswell.io/pubsub"
  )
}