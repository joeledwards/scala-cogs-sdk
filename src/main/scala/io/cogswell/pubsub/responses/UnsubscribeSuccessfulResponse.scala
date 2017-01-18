package io.cogswell.pubsub.responses

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import scala.util.Try
import java.util.UUID

case class UnsubscribeSuccessfulResponse(
    sequence: Long,
    action: String,
    code: Int,
    channels: List[String]
) extends ServerResponse[UnsubscribeSuccessfulResponse] {
  override val requiredAction = Some("unsubscribe")
  override val requiredCode = Some(200)
  override def self = this
}

object UnsubscribeSuccessfulResponse {
  lazy implicit val eventRecordReads: Reads[UnsubscribeSuccessfulResponse] = (
    (JsPath \ "sequence").read[Long]  and
    (JsPath \ "action").read[String]  and
    (JsPath \ "code").read[Int]  and
    (JsPath \ "channels").read[List[String]]
  )(UnsubscribeSuccessfulResponse.apply _)
  
  def parse(json: JsValue): JsResult[UnsubscribeSuccessfulResponse] = {
    json.validate[UnsubscribeSuccessfulResponse]
  }
}