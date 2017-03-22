package io.cogswell.pubsub.records

import scala.util.Try
import scala.util.Failure
import scala.util.Success
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import scala.util.Try
import scala.util.Failure
import scala.util.Success
import io.cogswell.exceptions.PubSubException
import io.cogswell.pubsub.responses._
import io.cogswell.exceptions.CogsParseException

/**
 * This class represents any type of record received from the server.
 */
abstract class ServerRecord(
  val recordSequence: Option[Long],
  val recordAction: Option[String],
  val recordCode: Option[Int],
  val requiredAction: Option[String],
  val requiredCode: Option[Int]
) {
  def isErrorResponse: Boolean = recordCode.map(_ != 200).getOrElse(false)
  
  def validate: Try[Unit] = {
    val v: Try[Unit] = requiredAction match {
      case None | `recordAction` => Success(Unit)
      case _ => Failure(CogsParseException(
          s"Invalid action '$recordAction' for type ${this.getClass.getName}", None
      ))
    }
    
    val w: Try[Unit] = v flatMap { _ =>
      requiredCode match {
        case None | `recordCode` => Success(Unit)
        case _ => Failure(CogsParseException(
            s"Invalid code '$recordCode' for type ${this.getClass.getName}", None
        ))
      }
    }
    
    w
  }
}

object ServerRecord {
  def parseResponse(
      json: JsValue
  ): Try[ServerRecord] = {
    { ((json \ "action"), (json \ "code"), (json \ "bad_request)")) match {
      case (JsDefined(JsString("msg")), _, _:JsUndefined) =>
        Success(MessageRecord.parse(json))
      case (JsDefined(JsString(action)), JsDefined(code: JsNumber), _:JsUndefined) => {
        (action, code.value.intValue) match {
          case ("pub", 200) =>
            Success(PublishSuccessfulResponse.parse(json))
          case ("pub", 401) =>
            Success(PublishNotAuthorizedResponse.parse(json))
          case ("pub", 404) =>
            Success(PublishNotFoundResponse.parse(json))
            
          case ("subscribe", 200) =>
            Success(SubscribeSuccessfulResponse.parse(json))
          case ("subscribe", 401) =>
            Success(SubscribeNotAuthorizedResponse.parse(json))
            
          case ("unsubscribe", 200) =>
            Success(UnsubscribeSuccessfulResponse.parse(json))
          case ("unsubscribe", 401) =>
            Success(UnsubscribeNotAuthorizedResponse.parse(json))
          case ("unsubscribe", 404) =>
            Success(UnsubscribeNotFoundResponse.parse(json))
            
          case ("unsubscribe-all", 200) =>
            Success(UnsubscribeAllSuccessfulResponse.parse(json))
          case ("unsubscribe-all", 401) =>
            Success(UnsubscribeAllNotAuthorizedResponse.parse(json))
            
          case ("subscriptions", 200) =>
            Success(SubscriptionsSuccessfulResponse.parse(json))
          case ("subscriptions", 401) =>
            Success(SubscriptionsNotAuthorizedResponse.parse(json))
            
          case ("session-uuid", 200) =>
            Success(SessionUuidSuccessfulResponse.parse(json))
        }
      }
      case (_, JsDefined(code: JsNumber), JsDefined(_)) if code.value.toInt == 400 =>
        Success(MessageRecord.parse(json))
      case (_, _, _) => Failure(PubSubException(s"Could not identify record from server: $json"))
    }} match {
      case Success(JsSuccess(record: ServerRecord, _)) => Success(record)
      case Success(err:JsError) => Failure(
        CogsParseException(
          "Error identifying JSON record from server.", Some(err)
        )
      )
      case Failure(error) => Failure(
        CogsParseException(
          "Error parsing JSON record from server.", None, Some(error)
        )
      )
    }
  }
}