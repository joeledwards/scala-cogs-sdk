package io.cogswell.cep

import play.api.libs.json.JsValue
import java.util.UUID
import scala.concurrent.Future

sealed trait PushEvent

case object PushReconnect extends PushEvent
case class PushClose(cause: Option[Throwable]) extends PushEvent
case class PushError(cause: Throwable) extends PushEvent
case class PushMessage(
    id: UUID,
    message: String,
    event: JsValue
) extends PushEvent

case class SubscribeOptions(autoReconnect: Boolean = true)
case class EventOptions(debugEcho: Boolean = false)

/**
 * Handle which is used to interact with the Cogwell CEP API.
 */
case class CepHandle(config: CepConfig) {
  /**
   * Subscribe to a push WebSocket on the specified channel.
   * 
   * @param project the name of the project
   * @param attributes the channel key attributes which match the schema
   * of the identified project, and specify the subscription channel.
   * @param options customize the behavior of this push socket
   * 
   * @return a Future which will succeed when the socket is first established
   */
  def subscribe(
      project: String, attributes: JsValue,
      options: SubscribeOptions = SubscribeOptions()
  )(handler: PartialFunction[PushEvent, Unit]): Future[Unit] = {
    // TODO: subscribe to Cogs
    Future.successful(Unit)
  }
  
  /**
   * Send an event to the specified channel.
   * 
   * @param project the name of the project
   * @param attributes the event attributes the names and types of which
   * must match the schema of the specified project and must include
   * a value for each of the project's channel key attributes.
   * @param options customize the behavior of this event
   * 
   * @return a Future which, if successful, will contain the UUID of the event
   */
  def sendEvent(
      project: String, attributes: JsValue,
      options: EventOptions = EventOptions()
  ): Future[UUID] = {
    // TODO: send event to Cogs
    Future.successful(UUID.randomUUID)
  }
}
