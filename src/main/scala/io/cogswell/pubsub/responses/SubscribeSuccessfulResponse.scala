package io.cogswell.pubsub.responses

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import scala.util.Try

case class SubscribeSuccessfulResponse(
    sequence: Long,
    action: String,
    code: Int,
    channels: List[String]
) extends ServerResponse[SubscribeSuccessfulResponse] with SequencedResponse {
  override val requiredAction = Some("subscribe")
  override val requiredCode = Some(200)
  override def self = this
}

object SubscribeSuccessfulResponse {
  lazy implicit val eventRecordReads: Reads[SubscribeSuccessfulResponse] = (
    (JsPath \ "sequence").read[Long]  and
    (JsPath \ "action").read[String]  and
    (JsPath \ "code").read[Int]  and
    (JsPath \ "channels").read[List[String]]
  )(SubscribeSuccessfulResponse.apply _)
  
  def parse(json: JsValue): JsResult[SubscribeSuccessfulResponse] = {
    json.validate[SubscribeSuccessfulResponse]
  }
}