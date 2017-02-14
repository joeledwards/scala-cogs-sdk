package io.cogswell.pubsub

import java.util.HashMap
import java.util.UUID
import java.util.concurrent.atomic.AtomicLong

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.concurrent.Promise
import scala.util.Failure
import scala.util.Success

import io.cogswell.pubsub.requests.PublishRequest
import io.cogswell.util.MarkOnce
import io.cogswell.util.SetOnce
import play.api.libs.json.JsArray
import play.api.libs.json.JsString
import play.api.libs.json.JsValue
import io.cogswell.pubsub.requests.PublishRequest
import io.cogswell.pubsub.requests.SessionUuidRequest
import io.cogswell.pubsub.requests.SubscribeRequest
import io.cogswell.pubsub.requests.SubscriptionsRequest
import io.cogswell.pubsub.requests.UnsubscribeAllRequest
import io.cogswell.pubsub.requests.UnsubscribeRequest
import scala.util.Try
import io.cogswell.util.Scheduler
import scala.concurrent.duration.Duration
import java.util.concurrent.TimeUnit
import io.cogswell.pubsub.responses.ServerResponse
import io.cogswell.pubsub.records.ServerRecord
import io.cogswell.pubsub.records.MessageRecord
import io.cogswell.exceptions.PubSubException
import io.cogswell.pubsub.responses.SequencedResponse
import io.cogswell.pubsub.responses.InvalidFormatResponse
import io.cogswell.pubsub.responses.SequencedResponse

/**
 * This is the class through which all interactions with the
 * pub/sub service are performed. It wraps a single socket,
 * and manages its connection.
 */
class PubSubHandle(val keys: Seq[String], val options: PubSubOptions)(
    implicit ec: ExecutionContext
) {
  type EventHandler = PartialFunction[PubSubEvent, Unit]
  type MessageHandler = PubSubMessageEvent => Unit
  
  private var eventHandler: Option[EventHandler] = None
  
  private val setupPromise: Promise[PubSubHandle] = Promise[PubSubHandle]
  private val done: MarkOnce = new MarkOnce
  private val sequence: AtomicLong = new AtomicLong(0)
  
  // TODO: implement back-off reconnect delay
  private val maxReconnectDelay: Duration = Duration(300, TimeUnit.SECONDS)
  private val defaultReconnectDelay: Duration = Duration(15, TimeUnit.SECONDS)
  private val reconnectDelay: Option[Duration] = None
  
  private var socket: Option[PubSubSocket] = None
  private var sessionId: Option[UUID] = None
  
  private val outstanding = new HashMap[Long, Promise[JsValue]]
  private val channelHandlers = new HashMap[String, MessageHandler]
  
  reconnect()
  
  /**
   * Send events to event handler if it is populated and defined for the value.
   */
  private def sendEvent(event: PubSubEvent): Unit = {
    eventHandler.foreach { handler =>
      if (handler.isDefinedAt(event)) {
        handler(event)
      }
    }
  }
  
  private def reconnect()(
      implicit ec: ExecutionContext
  ): Unit = {
    if (!done.isMarked) {
      val sock = new PubSubSocket(keys, options)
      
      sock.onEvent {
        case SocketCloseEvent(cause) => {
          println(s"Socket closed: $cause")
          
          if (!done.isMarked) {
            Scheduler.schedule(defaultReconnectDelay)(reconnect)
          }
          
          Try(sendEvent(PubSubCloseEvent(cause))) match {
            case Failure(error) => sendEvent(PubSubErrorEvent(error, None, None))
            case Success(_) =>
          }
        }
        case SocketErrorEvent(error) => {
          println(s"Socket error: $error")
          
          Try(sendEvent(PubSubErrorEvent(error, None, None))) match {
            case Failure(error) => sendEvent(PubSubErrorEvent(error, None, None))
            case Success(_) =>
          }
        }
        case SocketRecordEvent(record) => {
          println(s"Socket record: $record")
          
          { ServerRecord.parseResponse(record) flatMap {
            case r: MessageRecord => Try(sendEvent(PubSubMessageEvent(r)))
            //case _ => Failure(new PubSubException(
            //    s"Server record could not be categorized: $record"
            //))
          } } match {
            case Failure(error) => sendEvent(PubSubErrorEvent(error, None, None))
            case Success(_) =>
          }
        }
      }
      
      socket = Some(sock)
      
      sock.connect(sessionId) onComplete {
        case Success(_) => {
          if (!setupPromise.isCompleted) {
            setupPromise.success(this)
          }
          
          getSessionUuid() onComplete {
            case Failure(error) => {
              sessionId = None
            }
            case Success(uuid) => {
              sessionId match {
                case Some(`uuid`) =>
                case _ => sendEvent(PubSubNewSessionEvent(uuid))
              }
              
              sessionId = Some(uuid)
            }
          }
        }
        case Failure(error) => {
          if (!setupPromise.isCompleted) {
            setupPromise.failure(error)
          }
          
          done.mark
        }
      }
    }
  }
  
  private def newRequest(track: Boolean): (Long, Option[Future[JsValue]]) = {
    val seq = sequence.incrementAndGet
    val promise = new SetOnce[Future[JsValue]]
    
    if (track) {
      val p = Promise[JsValue]
      outstanding.put(seq, p)
      promise.set(p.future)
    }
    
    (seq, promise.value)
  }
  
  /**
   * Supplies the Future which resolves with this handle when
   * the initial socket setup completes.
   */
  def settle: Future[PubSubHandle] = setupPromise.future
  
  /**
   * Requests the session UUID from the server.
   * 
   * @return a Future which, if successful, will contain the session UUID
   */
  def getSessionUuid()(
      implicit ec: ExecutionContext
  ): Future[UUID] = {
    val (seq, Some(future)) = newRequest(true)
    socket.foreach(_.send(SessionUuidRequest(seq).toJson))
    
    future map { json =>
      (json \ "uuid").get match {
        case JsString(id) => UUID.fromString(id)
        case _ => throw new Exception("")
      }
    }
  }
  
  /**
   * Requests a subscription to a channel.
   * 
   * @param channel the channel to which to subscribe
   * @param handler the handler for messages received on this channel
   * 
   * @return a Future which, if successful, will contain a Seq of all the
   * channels to which this session is subscribed
   */
  def subscribe(channel: String)(handler: MessageHandler)(
      implicit ec: ExecutionContext
  ): Future[Seq[String]] = {
    channelHandlers.put(channel, handler)
    val (seq, Some(future)) = newRequest(true)
    socket.foreach(_.send(SubscribeRequest(seq, channel).toJson))
    
    future map { json =>
      (json \ "channels").get match {
        case JsArray(channels) => channels.map(_.toString)
        case _ => throw new Exception("")
      }
    }
  }
  
  /**
   * Requests to un-subscribe from a channel.
   * 
   * @channel the channel from which to unsubscribe
   * 
   * @return a Future which, if successful, will contain a Seq of all the
   * channels to which this session is still subscribed
   */
  def unsubscribe(channel: String)(
      implicit ec: ExecutionContext
  ): Future[Seq[String]] = {
    val (seq, Some(future)) = newRequest(true)
    socket.foreach(_.send(UnsubscribeRequest(seq, channel).toJson))
    
    future map { json =>
      (json \ "channels").get match {
        case JsArray(channels) => channels.map(_.toString)
        case _ => throw new Exception("")
      }
    }
  }
  
  /**
   * Requests to un-subscribe from all channels.
   * 
   * @return a Future which, if successful, will contain a Seq of all the
   * channels to which this session used to be subscribed.
   */
  def unsubscribeAll()(
      implicit ec: ExecutionContext
  ): Future[Seq[String]] = {
    val (seq, Some(future)) = newRequest(true)
    socket.foreach(_.send(UnsubscribeAllRequest(seq).toJson))
    
    future map { json =>
      (json \ "channels").get match {
        case JsArray(channels) => channels.map(_.toString)
        case _ => throw new Exception("")
      }
    }
  }
  
  /**
   * Requests a list of all channel subscriptions.
   * 
   * @return a Future which, if successful, will contain a Seq of all the
   * channels to which this session is subscribed.
   */
  def listSubscriptions()(
      implicit ec: ExecutionContext
  ): Future[Seq[String]] = {
    val (seq, Some(future)) = newRequest(true)
    socket.foreach(_.send(SubscriptionsRequest(seq).toJson))
    
    future map { json =>
      (json \ "channels").get match {
        case JsArray(channels) => channels.map(_.toString)
        case _ => throw new Exception("")
      }
    }
  }
  
  /**
   * Publishes a message to the server. If an error occurs publishing the message,
   * it will be reported to the error handler if it has been set.
   */
  def publish(channel: String, message: String): Unit = {
    val (seq, None) = newRequest(false)
    socket.foreach(_.send(PublishRequest(seq, channel, message).toJson))
  }
  
  /**
   * Closes the connection if it is open.
   */
  def close(): Unit = {
    if (done.mark) {
      socket.foreach(_.close)
      socket = None
      sendEvent(PubSubCloseEvent(None))
    }
  }
  
  /**
   * Supply a handler for any of the events which may be supplied by the
   * 
   * @param handler a PartialFunction which can handle any number of the
   * sub types of PubSubEvent
   */
  def onEvent(handler: EventHandler): Unit = eventHandler = Option(handler)
}
