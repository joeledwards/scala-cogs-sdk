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

class PubSubHandle(
    val keys: Seq[String],
    val options: PubSubOptions
)(implicit ec: ExecutionContext) {
  type Channel = String
  type Message = String
  type Record = String
  
  type MessageHandler = (Channel, Message) => Unit
  type RecordHandler = (Record) => Unit
  type ErrorHandler = (Throwable) => Unit
  type CloseHandler = () => Unit
  
  private var messageHandler: Option[MessageHandler] = None
  private var recordHandler: Option[RecordHandler] = None
  private var errorHandler: Option[ErrorHandler] = None
  private var closeHandler: Option[CloseHandler] = None
  
  private val setupPromise: Promise[PubSubHandle] = Promise[PubSubHandle]
  private val hasConnected: MarkOnce = new MarkOnce
  private val done: MarkOnce = new MarkOnce
  private val sequence: AtomicLong = new AtomicLong(0)
  private var socket: Option[PubSubSocket] = None
  
  private val outstanding = new HashMap[Long, Promise[JsValue]]
  private val channelHandlers = new HashMap[String, MessageHandler]
  
  reconnect()
  
  private def reconnect()(implicit ec: ExecutionContext): Unit = {
    if (!done.isMarked) {
      val sock = new PubSubSocket(keys, options)
      socket = Some(sock)
      
      sock.connect() onComplete {
        case Success(_) => setupPromise.success(this)
        case Failure(error) => setupPromise.failure(error)
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
  
  def close(): Unit = {
    if (done.mark) {
      socket.foreach(_.close)
      socket = None
      closeHandler.foreach(_())
    }
  }
  
  def publish(channel: String, message: String): Unit = {
    val (seq, None) = newRequest(false)
    socket.foreach(_.send(PublishRequest(seq, channel, message).toJson))
  }
  
  def onMessage(handler: MessageHandler): Unit = messageHandler = Option(handler)
  def onError(handler: ErrorHandler): Unit = errorHandler = Option(handler)
  def onRecord(handler: RecordHandler): Unit = recordHandler = Option(handler)
  def onClose(handler: CloseHandler): Unit = closeHandler = Option(handler)
}
