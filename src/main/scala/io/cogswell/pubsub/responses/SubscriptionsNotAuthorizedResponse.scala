package io.cogswell.pubsub.responses

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import scala.util.Try
import java.util.UUID

case class SubscriptionsNotAuthorizedResponse(
    sequence: Long,
    action: String,
    code: Int,
    message: String,
    details: Option[String]
) extends ServerResponse[SubscriptionsNotAuthorizedResponse] with SequencedResponse {
  override val requiredAction = Some("subscriptions")
  override val requiredCode = Some(401)
  override def self = this
}

object SubscriptionsNotAuthorizedResponse {
  lazy implicit val eventRecordReads: Reads[SubscriptionsNotAuthorizedResponse] = (
    (JsPath \ "sequence").read[Long]  and
    (JsPath \ "action").read[String]  and
    (JsPath \ "code").read[Int]  and
    (JsPath \ "message").read[String] and
    (JsPath \ "details").readNullable[String]
  )(SubscriptionsNotAuthorizedResponse.apply _)
  
  def parse(json: JsValue): JsResult[SubscriptionsNotAuthorizedResponse] = {
    json.validate[SubscriptionsNotAuthorizedResponse]
  }
}