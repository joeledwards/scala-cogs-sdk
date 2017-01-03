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
import io.cogswell.pubsub.requests.UnsubscribeAllRequest
import io.cogswell.pubsub.requests.ClientUuidRequest
import io.cogswell.pubsub.requests.SubscribeRequest
import io.cogswell.pubsub.requests.UnsubscribeRequest
import io.cogswell.pubsub.requests.SubscriptionsRequest
import io.cogswell.pubsub.requests.UnsubscribeAllRequest
import io.cogswell.pubsub.requests.SubscribeRequest
import io.cogswell.pubsub.requests.PublishRequest
import io.cogswell.pubsub.requests.UnsubscribeRequest
import io.cogswell.pubsub.requests.SubscriptionsRequest
import scala.util.Try
import io.cogswell.util.Scheduler
import scala.concurrent.duration.Duration
import java.util.concurrent.TimeUnit

class PubSubHandle(
    val keys: Seq[String],
    val options: PubSubOptions
)(implicit ec: ExecutionContext) {
  type Channel = String
  type Message = String
  type Record = String
  type Sequence = String
  
  type MessageHandler = (Channel, Message) => Unit
  type RecordHandler = (Record) => Unit
  type ErrorHandler = (Throwable, Option[Sequence], Option[Channel]) => Unit
  type CloseHandler = (Option[Throwable]) => Unit
  type ReconnectHandler = () => Unit
  
  private var messageHandler: Option[MessageHandler] = None
  private var recordHandler: Option[RecordHandler] = None
  private var errorHandler: Option[ErrorHandler] = None
  private var closeHandler: Option[CloseHandler] = None
  private var reconnectHandler: Option[ReconnectHandler] = None
  
  private val setupPromise: Promise[PubSubHandle] = Promise[PubSubHandle]
  private val done: MarkOnce = new MarkOnce
  private val sequence: AtomicLong = new AtomicLong(0)
  private var socket: Option[PubSubSocket] = None
  private val reconnectDelay: Duration = Duration(15, TimeUnit.SECONDS)
  
  private val outstanding = new HashMap[Long, Promise[JsValue]]
  private val channelHandlers = new HashMap[String, MessageHandler]
  
  reconnect()
  
  private def reconnect()(implicit ec: ExecutionContext): Unit = {
    if (!done.isMarked) {
      val sock = new PubSubSocket(keys, options)
      
      sock.onClose(cause => {
        if (!done.isMarked) {
          Scheduler.schedule(reconnectDelay)(reconnect)
        }
        
        Try(closeHandler.foreach(_(cause))) match {
          case Failure(error) => errorHandler.foreach(_(error, None, None))
          case Success(_) =>
        }
      })
      
      socket = Some(sock)
      
      sock.connect() onComplete {
        case Success(_) => setupPromise.success(this)
        case Failure(error) => {
          setupPromise.failure(error)
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
  
  def settle: Future[PubSubHandle] = setupPromise.future
  
  /**
   * Requests the session UUID from the server.
   * 
   * @return a Future which, if successful, will contain the session UUID
   */
  def getClientId()(implicit ec: ExecutionContext): Future[UUID] = {
    val (seq, Some(future)) = newRequest(true)
    socket.foreach(_.send(ClientUuidRequest(seq).toJson))
    
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
   * @return a Future which, if successful, will contain a Seq of all the
   * channels to which this session is subscribed
   */
  def subscribe(
      channel: String, handler: MessageHandler
  )(implicit ec: ExecutionContext): Future[Seq[String]] = {
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
   * @return a Future which, if successful, will contain a Seq of all the
   * channels to which this session is still subscribed
   */
  def unsubscribe(
      channel: String
  )(implicit ec: ExecutionContext): Future[Seq[String]] = {
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
  def unsubscribeAll()(implicit ec: ExecutionContext): Future[Seq[String]] = {
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
  def listSubscriptions()(implicit ec: ExecutionContext): Future[Seq[String]] = {
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
      closeHandler.foreach(_(None))
    }
  }
  
  /**
   * Registers the general message handler, which will be called when a message is
   * received from any channel.
   * 
   * @param handler a MessageHandler
   */
  def onMessage(handler: MessageHandler): Unit = messageHandler = Option(handler)
  
  /**
   * Register an error handler, which will be called when an error occurs.
   * 
   * @param handler an ErrorHandler
   */
  def onError(handler: ErrorHandler): Unit = errorHandler = Option(handler)
  
  /**
   * Register an general record handler, which will be called with every record
   * that is received from the Pub/Sub server.
   * 
   * @param handler a RecordHandler
   */
  def onRecord(handler: RecordHandler): Unit = recordHandler = Option(handler)
  
  /**
   * Register the reconnect handler, which will be called whenever the connection
   * is automatically replaced.
   * 
   * @param handler a RecordHandler
   */
  def onReconnect(handler: ReconnectHandler): Unit = reconnectHandler = Option(handler)
  
  /**
   * Register an error handler, which will be called when the connection is closed,
   * whether cleanly, or as the result of an error.
   * 
   * @param handler a CloseHandler
   */
  def onClose(handler: CloseHandler): Unit = closeHandler = Option(handler)
}
