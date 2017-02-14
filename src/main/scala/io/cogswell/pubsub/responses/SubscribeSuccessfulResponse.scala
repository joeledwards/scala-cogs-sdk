package io.cogswell.pubsub.responses

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import scala.util.Try
import io.cogswell.pubsub.records.ServerRecord

case class SubscribeSuccessfulResponse(
    sequence: Long,
    action: String,
    code: Int,
    channels: List[String]
) extends ServerRecord(
    recordSequence = Some(sequence),
    recordAction = Some(action),
    recordCode = Some(code),
    requiredAction = Some("subscribe"),
    requiredCode = Some(200)
)

object SubscribeSuccessfulResponse {
  lazy implicit val eventRecordReads: Reads[SubscribeSuccessfulResponse] = (
    (JsPath \ "seq").read[Long]  and
    (JsPath \ "action").read[String]  and
    (JsPath \ "code").read[Int]  and
    (JsPath \ "channels").read[List[String]]
  )(SubscribeSuccessfulResponse.apply _)
  
  def parse(json: JsValue): JsResult[SubscribeSuccessfulResponse] = {
    json.validate[SubscribeSuccessfulResponse]
  }
}