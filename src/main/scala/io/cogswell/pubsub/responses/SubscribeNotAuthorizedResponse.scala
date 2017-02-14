package io.cogswell.pubsub.responses

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import scala.util.Try
import io.cogswell.pubsub.records.ServerRecord

case class SubscribeNotAuthorizedResponse(
    sequence: Long,
    action: String,
    code: Int,
    message: String,
    details: Option[String]
) extends ServerRecord(
    recordSequence = Some(sequence),
    recordAction = Some(action),
    recordCode = Some(code),
    requiredAction = Some("subscribe"),
    requiredCode = Some(401)
)

object SubscribeNotAuthorizedResponse {
  lazy implicit val eventRecordReads: Reads[SubscribeNotAuthorizedResponse] = (
    (JsPath \ "seq").read[Long]  and
    (JsPath \ "action").read[String]  and
    (JsPath \ "code").read[Int]  and
    (JsPath \ "message").read[String]  and
    (JsPath \ "details").readNullable[String]
  )(SubscribeNotAuthorizedResponse.apply _)
  
  def parse(json: JsValue): JsResult[SubscribeNotAuthorizedResponse] = {
    json.validate[SubscribeNotAuthorizedResponse]
  }
}