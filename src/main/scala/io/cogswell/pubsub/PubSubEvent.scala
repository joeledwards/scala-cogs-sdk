package io.cogswell.pubsub

import java.util.UUID
import io.cogswell.pubsub.records.MessageRecord

sealed trait PubSubEvent

case object PubSubReconnectEvent extends PubSubEvent
case class PubSubNewSessionEvent(sessionId: UUID) extends PubSubEvent
case class PubSubMessageEvent(message: MessageRecord) extends PubSubEvent
case class PubSubRawRecordEvent(record: String) extends PubSubEvent
case class PubSubCloseEvent(cause: Option[Throwable]) extends PubSubEvent
case class PubSubErrorEvent(
    cause: Throwable, sequence: Option[Long], channel: Option[String]
) extends PubSubEvent
