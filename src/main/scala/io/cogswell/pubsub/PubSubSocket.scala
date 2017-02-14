package io.cogswell.pubsub

import akka.Done
import akka.actor.ActorSystem
import akka.http.impl.engine.ws.WebSocket
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.Message
import akka.http.scaladsl.model.ws.TextMessage
import akka.http.scaladsl.model.ws.WebSocketRequest
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Flow
import akka.stream.scaladsl.Keep
import akka.stream.scaladsl.Sink
import akka.stream.scaladsl.Source
import play.api.libs.json.JsValue
import play.api.libs.json.Json
import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import scala.util.Failure
import scala.util.Success
import org.reactivestreams.Publisher
import org.reactivestreams.Subscriber
import scala.collection.mutable.MutableList
import scala.collection.immutable.{Seq => ImmuteSeq}
import scala.util.Try
import akka.http.scaladsl.model.HttpHeader
import akka.http.scaladsl.model.headers.RawHeader
import java.util.UUID
import io.cogswell.exceptions.PubSubException
import io.cogswell.util.Futures

class PubSubSocket(
    val keys: Seq[String],
    val options: PubSubOptions = PubSubOptions.default
) {
  implicit val system = PubSubSocket.system
  implicit val materializer = PubSubSocket.materializer
  
  type EventHandler = PartialFunction[SocketEvent, Unit]
  
  private var eventHandler: Option[EventHandler] = None
  
  def handleEvent(event: SocketEvent): Unit = {
    eventHandler.foreach { handler =>
      if (handler.isDefinedAt(event)) {
        handler(event)
      }
    }
  }
  
  object Publisher extends Publisher[Message] {
    val subscribers = new MutableList[Subscriber[_ >: Message]]
    
    override def subscribe(subscriber: Subscriber[_ >: Message]): Unit = {
      subscribers += subscriber
    }
    
    def close(cause: Option[Throwable]): Unit = {
      subscribers.foreach { subscriber =>
        Try {
          cause match {
            case None => subscriber.onComplete()
            case Some(error) => subscriber.onError(error)
          }
        } match {
          case Failure(error) => handleEvent(SocketErrorEvent(error))
          case _ =>
        }
      }
    }
    
    def publish(message: Message): Unit = {
      subscribers.foreach { subscriber =>
        Try {
          subscriber.onNext(message)
        } match {
          case Failure(error) => handleEvent(SocketErrorEvent(error))
          case _ =>
        }
      }
    }
  }
  
  private val recordSource = Source.fromPublisher(Publisher)
  
  private val recordSink: Sink[Message, Future[Done]] = {
    Sink.foreach { case message: TextMessage.Strict =>
      Try {
        val json = Json.parse(message.text)
        handleEvent(SocketRecordEvent(json))
      } match {
        case Failure(error) => handleEvent(SocketErrorEvent(error))
        case _ =>
      }
    }
  }
  
  private val messageFlow: Flow[Message, Message, Future[Done]] = {
    Flow.fromSinkAndSourceMat(recordSink, recordSource)(Keep.left)
  }
  
  def connect(sessionUuid: Option[UUID])(implicit ec: ExecutionContext): Future[Unit] = {
    CogsAuth.authContent(keys, sessionUuid) match {
      case None => Future.failed(new PubSubException("Could not construct auth content."))
      case Some(CogsAuthData(hmac, payload)) => {
        val headers = ImmuteSeq(Seq(
          RawHeader("Payload", payload),
          RawHeader("PayloadHMAC", hmac)
        ): _*)
        
        val (upgrade, closed) = Http().singleWebSocketRequest(
          WebSocketRequest(options.url, headers), messageFlow
        )
        
        closed onComplete {
          case Success(_) => handleEvent(SocketCloseEvent(None))
          case Failure(error) => handleEvent(SocketCloseEvent(Some(error)))
        }
        
        Futures.translate(upgrade) {
          case Success(ug) => {
            println(s"Upgrade result: $ug")
            
            val status = ug.response.status
            val code = status.intValue
            val message = status.defaultMessage()
            val reason = status.reason
            
            code match {
              case 101 => Future.successful(101)
              case code => Future.failed(PubSubException(
                  s"Error establishing the WebSocket: [$code] $reason: $message"
              ))
            }
          }
          case Failure(error) => Future.failed(error)
        }
      }
    }
    
  }
  
  def close(): Unit = {
    Publisher.close(None)
  }
  
  def send(json: JsValue): Unit = {
    Publisher.publish(TextMessage(json.toString))
  }
  
  def onEvent(handler: EventHandler): Unit = eventHandler = Option(handler)
}

object PubSubSocket {
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
}
