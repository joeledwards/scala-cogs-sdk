package io.cogswell.pubsub.responses

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import scala.util.Try
import java.util.UUID
import io.cogswell.pubsub.records.ServerRecord

case class SessionUuidSuccessResponse(
    sequence: Long,
    action: String,
    code: Int,
    message: UUID
) extends ServerRecord(
    recordSequence = Some(sequence),
    recordAction = Some(action),
    recordCode = Some(code),
    requiredAction = Some("session-uuid"),
    requiredCode = Some(200)
)

object SessionUuidSuccessResponse {
  lazy implicit val eventRecordReads: Reads[SessionUuidSuccessResponse] = (
    (JsPath \ "seq").read[Long]  and
    (JsPath \ "action").read[String]  and
    (JsPath \ "code").read[Int]  and
    (JsPath \ "uuid").read[UUID]
  )(SessionUuidSuccessResponse.apply _)
  
  def parse(json: JsValue): JsResult[SessionUuidSuccessResponse] = {
    json.validate[SessionUuidSuccessResponse]
  }
}