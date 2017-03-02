package io.cogswell.pubsub.responses

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import io.cogswell.pubsub.records.ServerRecord
import java.util.UUID

case class PublishSuccessfulResponse(
    sequence: Long,
    action: String,
    code: Int,
    id: UUID
) extends ServerRecord(
    recordSequence = Some(sequence),
    recordAction = Some(action),
    recordCode = Some(code),
    requiredAction = Some("pub"),
    requiredCode = Some(200)
)

object PublishSuccessfulResponse {
  lazy implicit val eventRecordReads: Reads[PublishSuccessfulResponse] = (
    (JsPath \ "seq").read[Long]  and
    (JsPath \ "action").read[String]  and
    (JsPath \ "code").read[Int]  and
    (JsPath \ "id").read[UUID]
  )(PublishSuccessfulResponse.apply _)
  
  def parse(json: JsValue): JsResult[PublishSuccessfulResponse] = {
    json.validate[PublishSuccessfulResponse]
  }
}