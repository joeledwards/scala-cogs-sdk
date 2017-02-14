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
abstract class ServerRecord[Self <: ServerRecord[Self]] {
  val requiredAction: Option[String]
  val action: String
  
  def self: Self
  
  /**
   * Validate that the action matches if it is required.
   */
  def validate: Try[Self] = {
    val v: Try[Self] = requiredAction match {
      case Some(reqAction) => reqAction match {
        case `action` => Success(self)
        case _ => Failure(CogsParseException(
            s"Invalid action '$action' for type ${this.getClass.getName}", None
        ))
      }
      case None => Success(self)
    }
    
    v
  }
}

object ServerRecord {
  def parseResponse[T <: ServerRecord[T]](
      json: JsValue
  ): Try[T] = {
    { ((json \ "action"), (json \ "code"), (json \ "bad_request)")) match {
      case (JsDefined(JsString("msg")), _, _:JsUndefined) =>
        Success(MessageRecord.parse(json))
      case (JsDefined(JsString(action)), JsDefined(code: JsNumber), _:JsUndefined) => {
        (action, code.value.intValue) match {
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
            
          case ("pub", 401) =>
            Success(PublishNotAuthorizedResponse.parse(json))
          case ("pub", 404) =>
            Success(PublishNotFoundResponse.parse(json))
        }
      }
      case (_, JsDefined(code: JsNumber), JsDefined(_)) if code.value.toInt == 400 =>
        Success(MessageRecord.parse(json))
      case (_, _, _) => Failure(PubSubException(s"Could not identify record from server: $json"))
    }} match {
      case Success(JsSuccess(record:T, _)) => Success(record)
      case Success(err:JsError) => Failure(
        CogsParseException(
          "Error parsing JSON record from server.", Some(err)
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