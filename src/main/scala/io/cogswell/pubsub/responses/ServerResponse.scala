package io.cogswell.pubsub.responses

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import scala.util.Try
import scala.util.Failure
import scala.util.Success
import io.cogswell.pubsub.records.MessageRecord
import io.cogswell.pubsub.records.MessageRecord


abstract class ServerResponse[Self <: ServerResponse[Self]] {
  val requiredAction: Option[String]
  val requiredCode: Option[Int]
  val action: String
  val code: Int
  
  def self: Self
  
  def validate: Try[Self] = {
    val v: Try[Self] = requiredAction match {
      case Some(reqAction) => reqAction match {
        case `action` => Success(self)
        case _ => Failure(ParseException(
            s"Invalid action '$action' for type ${this.getClass.getName}", None
        ))
      }
      case None => Success(self)
    }
    
    val w: Try[Self] = v flatMap { _ =>
      requiredCode match {
        case Some(reqCode) => reqCode match {
          case `code` => Success(self)
          case _ => Failure(ParseException(
              s"Invalid code '$code' for type ${this.getClass.getName}", None
          ))
        }
        case None => Success(self)
      }
    }
    
    w
  }
}

object ServerResponse {
  def parseResponse[T <: ServerResponse[T]](
      response: String
  ): Try[T] = {
    Try {
      Json.parse(response)
    } flatMap { json =>
      ((json \ "action"), (json \ "code")) match {
        case (JsDefined(JsString(a)), JsDefined(c: JsNumber)) => {
          (a, c.value.intValue) match {
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
        case (JsDefined(JsString("msg")), _) =>
          Success(MessageRecord.parse(json))
      }
    } match {
      case Success(JsSuccess(result:T, _)) => Success(result)
      case Success(err:JsError) => Failure(
        ParseException(
          "Error parsing JSON record from server.", Some(err)
        )
      )
      case Failure(error) => Failure(
        ParseException(
          "Error parsing JSON record from server.", None, Some(error)
        )
      )
    }
  }
}