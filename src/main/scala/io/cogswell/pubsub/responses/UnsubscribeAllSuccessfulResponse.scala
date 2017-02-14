package io.cogswell.pubsub.responses

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import scala.util.Try
import java.util.UUID

case class UnsubscribeAllSuccessfulResponse(
    sequence: Long,
    action: String,
    code: Int,
    channels: List[String]
) extends ServerResponse[UnsubscribeAllSuccessfulResponse] with SequencedResponse {
  override val requiredAction = Some("unsubscribe-all")
  override val requiredCode = Some(200)
  override def self = this
}

object UnsubscribeAllSuccessfulResponse {
  lazy implicit val eventRecordReads: Reads[UnsubscribeAllSuccessfulResponse] = (
    (JsPath \ "sequence").read[Long]  and
    (JsPath \ "action").read[String]  and
    (JsPath \ "code").read[Int]  and
    (JsPath \ "channels").read[List[String]]
  )(UnsubscribeAllSuccessfulResponse.apply _)
  
  def parse(json: JsValue): JsResult[UnsubscribeAllSuccessfulResponse] = {
    json.validate[UnsubscribeAllSuccessfulResponse]
  }
}