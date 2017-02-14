package io.cogswell.pubsub

import play.api.libs.json.JsValue

sealed trait SocketEvent

case class SocketErrorEvent(error: Throwable) extends RuntimeException(error) with SocketEvent
case class SocketCloseEvent(cause: Option[Throwable]) extends SocketEvent
case class SocketRecordEvent(record: JsValue) extends SocketEvent
