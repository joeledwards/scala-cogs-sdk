import io.cogswell.pubsub.CogsPubSub
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Failure
import scala.util.Success
import io.cogswell.pubsub.PubSubOptions
import io.cogswell.pubsub.PubSubNewSessionEvent
import io.cogswell.pubsub.PubSubErrorEvent
import io.cogswell.pubsub.PubSubErrorResponseEvent
import io.cogswell.pubsub.PubSubRawRecordEvent
import com.google.common.base.Throwables
import io.cogswell.exceptions.CogsParseException
import java.util.Timer
import java.util.TimerTask
import scala.concurrent.duration.Duration
import scala.util.Try
import java.util.concurrent.TimeUnit
import io.cogswell.util.Scheduler

object Messages extends App {
  val url = "wss://gamqa-api.aviatainc.com/pubsub"
  //val input = "in"
  val input = "out"
  val output = "out"
  
  val scheduler = new Timer()
  
  val keys = Seq(
    "R-2cf87f44ca4b21a1fdd38e7553022075-b351ea1fc356b9af28978862c0fb6b72cfdfa8e01da9c38c7ff238dc8b804f71",
    "W-2cf87f44ca4b21a1fdd38e7553022075-c2fe40dc8c31cde3304eb29337661d5969edb5c9b70f4107c0dea7ff9eaf2a3c"
  )
  
  val options = PubSubOptions(
    url = url,
    eventHandler = Some({
      case PubSubNewSessionEvent(sessionId) => println(s"New session [${sessionId}]")
      //case PubSubRawRecordEvent(record) => println(s"Raw record: ${record}")
      case PubSubErrorEvent(error, _, _) => {
        error match {
          case CogsParseException(errorMessage, Some(jsonError), _) =>
            println(s"Connection error: ${errorMessage}\n${jsonError}")
          case _ =>
            println(s"Connection error: ${error}\n${Throwables.getStackTraceAsString(error)}")
        }
      }
    })
  )
  
  CogsPubSub.connect(keys, options) onComplete {
    case Success(handle) => {
      Scheduler.repeat(Duration(2, TimeUnit.SECONDS)) {
        handle.publish(output, "Hello from Scala")
      }
      
      println(s"Connected to Pub/Sub server: $url")
      
      handle.subscribe(input) { message =>
        println(s"Received a Message: $message")
        //handle.close()
      } andThen {
        case Success(_) => println(s"Subscribed to channel '$input'.")
        case Failure(error) => println(s"Error subscribing to channel '$input' : $error")
      }
    }
    case Failure(error) => {
      println(s"Error: $error")
    }
  }
}