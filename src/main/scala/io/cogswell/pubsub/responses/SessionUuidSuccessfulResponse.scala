package io.cogswell.pubsub.responses

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import scala.util.Try
import java.util.UUID
import io.cogswell.pubsub.records.ServerRecord

case class SessionUuidSuccessfulResponse(
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

object SessionUuidSuccessfulResponse {
  lazy implicit val eventRecordReads: Reads[SessionUuidSuccessfulResponse] = (
    (JsPath \ "seq").read[Long]  and
    (JsPath \ "action").read[String]  and
    (JsPath \ "code").read[Int]  and
    (JsPath \ "uuid").read[UUID]
  )(SessionUuidSuccessfulResponse.apply _)
  
  def parse(json: JsValue): JsResult[SessionUuidSuccessfulResponse] = {
    json.validate[SessionUuidSuccessfulResponse]
  }
}