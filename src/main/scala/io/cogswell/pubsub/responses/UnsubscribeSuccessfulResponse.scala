package io.cogswell.pubsub.responses

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import scala.util.Try
import java.util.UUID
import io.cogswell.pubsub.records.ServerRecord

case class UnsubscribeSuccessfulResponse(
    sequence: Long,
    action: String,
    code: Int,
    channels: List[String]
) extends ServerRecord(
    recordSequence = Some(sequence),
    recordAction = Some(action),
    recordCode = Some(code),
    requiredAction = Some("unsubscribe"),
    requiredCode = Some(200)
)

object UnsubscribeSuccessfulResponse {
  lazy implicit val eventRecordReads: Reads[UnsubscribeSuccessfulResponse] = (
    (JsPath \ "seq").read[Long]  and
    (JsPath \ "action").read[String]  and
    (JsPath \ "code").read[Int]  and
    (JsPath \ "channels").read[List[String]]
  )(UnsubscribeSuccessfulResponse.apply _)
  
  def parse(json: JsValue): JsResult[UnsubscribeSuccessfulResponse] = {
    json.validate[UnsubscribeSuccessfulResponse]
  }
}