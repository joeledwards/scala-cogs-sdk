package io.cogswell.pubsub.responses

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import scala.util.Try
import java.util.UUID

case class SessionUuidSuccessResponse(
    sequence: Long,
    action: String,
    code: Int,
    message: UUID
) extends ServerResponse[SessionUuidSuccessResponse] with SequencedResponse {
  override val requiredAction = Some("session-uuid")
  override val requiredCode = Some(200)
  override def self = this
}

object SessionUuidSuccessResponse {
  lazy implicit val eventRecordReads: Reads[SessionUuidSuccessResponse] = (
    (JsPath \ "sequence").read[Long]  and
    (JsPath \ "action").read[String]  and
    (JsPath \ "code").read[Int]  and
    (JsPath \ "message").read[UUID]
  )(SessionUuidSuccessResponse.apply _)
  
  def parse(json: JsValue): JsResult[SessionUuidSuccessResponse] = {
    json.validate[SessionUuidSuccessResponse]
  }
}