package io.cogswell.pubsub.responses

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import scala.util.Try
import java.util.UUID
import io.cogswell.pubsub.records.ServerRecord

case class PublishNotAuthorizedResponse(
    sequence: Long,
    action: String,
    code: Int,
    message: String,
    details: Option[String]
) extends ServerRecord(
    recordSequence = Some(sequence),
    recordAction = Some(action),
    recordCode = Some(code),
    requiredAction = Some("pub"),
    requiredCode = Some(401)
)

object PublishNotAuthorizedResponse {
  lazy implicit val eventRecordReads: Reads[PublishNotAuthorizedResponse] = (
    (JsPath \ "seq").read[Long]  and
    (JsPath \ "action").read[String]  and
    (JsPath \ "code").read[Int]  and
    (JsPath \ "message").read[String] and
    (JsPath \ "details").readNullable[String]
  )(PublishNotAuthorizedResponse.apply _)
  
  def parse(json: JsValue): JsResult[PublishNotAuthorizedResponse] = {
    json.validate[PublishNotAuthorizedResponse]
  }
}