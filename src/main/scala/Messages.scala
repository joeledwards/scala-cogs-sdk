import io.cogswell.pubsub.CogsPubSub
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Failure
import scala.util.Success

object Messages extends App {
  val channel = "messages"
  val keys = Seq(
    "R-2cf87f44ca4b21a1fdd38e7553022075-b351ea1fc356b9af28978862c0fb6b72cfdfa8e01da9c38c7ff238dc8b804f71",
    "W-2cf87f44ca4b21a1fdd38e7553022075-c2fe40dc8c31cde3304eb29337661d5969edb5c9b70f4107c0dea7ff9eaf2a3c"
  )
  
  CogsPubSub.connect(keys) onComplete {
    case Success(handle) => {
      handle.subscribe(channel) { message =>
        println(s"Message: $message")
        handle.close()
      } andThen {
        case Success(_) => {
          println("Subscribed.")
          handle.publish(channel, "hi")
        }
      }
    }
    case Failure(error) => {
      println(s"Error: $error")
    }
  }
}