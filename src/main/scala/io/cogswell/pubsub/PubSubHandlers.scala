package io.cogswell.pubsub

object PubSubHandlers {
  type EventHandler = PartialFunction[PubSubEvent, Unit]
  type MessageHandler = PubSubMessageEvent => Unit
}