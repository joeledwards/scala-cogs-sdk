package io.cogswell.pubsub

import java.util.UUID
import io.cogswell.pubsub.records.MessageRecord
import play.api.libs.json.JsValue
import io.cogswell.pubsub.records.ServerRecord

sealed trait PubSubEvent

case object PubSubReconnectEvent extends PubSubEvent
case class PubSubNewSessionEvent(sessionId: UUID) extends PubSubEvent
case class PubSubMessageEvent(message: MessageRecord) extends PubSubEvent
case class PubSubRawRecordEvent(record: JsValue) extends PubSubEvent
case class PubSubCloseEvent(cause: Option[Throwable]) extends PubSubEvent

case class PubSubErrorResponseEvent(
    record: ServerRecord
) extends RuntimeException(
    s"Error response from server: $record", null
) with PubSubEvent

case class PubSubErrorEvent(
    cause: Throwable, sequence: Option[Long], channel: Option[String]
) extends PubSubEvent
