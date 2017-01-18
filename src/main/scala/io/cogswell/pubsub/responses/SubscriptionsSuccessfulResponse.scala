package io.cogswell.pubsub.responses

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import scala.util.Try
import java.util.UUID

case class SubscriptionsSuccessfulResponse(
    sequence: Long,
    action: String,
    code: Int,
    channels: List[String]
) extends ServerResponse[SubscriptionsSuccessfulResponse] {
  override val requiredAction = Some("subscriptions")
  override val requiredCode = Some(200)
  override def self = this
}

object SubscriptionsSuccessfulResponse {
  lazy implicit val eventRecordReads: Reads[SubscriptionsSuccessfulResponse] = (
    (JsPath \ "sequence").read[Long]  and
    (JsPath \ "action").read[String]  and
    (JsPath \ "code").read[Int]  and
    (JsPath \ "channels").read[List[String]]
  )(SubscriptionsSuccessfulResponse.apply _)
  
  def parse(json: JsValue): JsResult[SubscriptionsSuccessfulResponse] = {
    json.validate[SubscriptionsSuccessfulResponse]
  }
}