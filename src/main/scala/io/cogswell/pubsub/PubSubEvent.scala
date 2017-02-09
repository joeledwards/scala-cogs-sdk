package io.cogswell.pubsub

import java.util.UUID

sealed trait PubSubEvent

case object PubSubReconnectEvent extends PubSubEvent
case class PubSubNewSessionEvent(sessionId: UUID) extends PubSubEvent
case class PubSubMessageEvent(channel: String, message: String) extends PubSubEvent
case class PubSubRawRecordEvent(record: String) extends PubSubEvent
case class PubSubCloseEvent(cause: Option[Throwable]) extends PubSubEvent
case class PubSubErrorEvent(
    cause: Throwable, sequence: Option[Long], channel: Option[String]
) extends PubSubEvent
