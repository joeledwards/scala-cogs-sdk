package io.cogswell.pubsub.responses

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import scala.util.Try
import java.util.UUID

case class UnsubscribeNotAuthorizedResponse(
    sequence: Long,
    action: String,
    code: Int,
    message: String,
    details: Option[String]
) extends ServerResponse[UnsubscribeNotAuthorizedResponse] {
  override val requiredAction = Some("unsubscribe")
  override val requiredCode = Some(401)
  override def self = this
}

object UnsubscribeNotAuthorizedResponse {
  lazy implicit val eventRecordReads: Reads[UnsubscribeNotAuthorizedResponse] = (
    (JsPath \ "sequence").read[Long]  and
    (JsPath \ "action").read[String]  and
    (JsPath \ "code").read[Int]  and
    (JsPath \ "message").read[String] and
    (JsPath \ "details").readNullable[String]
  )(UnsubscribeNotAuthorizedResponse.apply _)
  
  def parse(json: JsValue): JsResult[UnsubscribeNotAuthorizedResponse] = {
    json.validate[UnsubscribeNotAuthorizedResponse]
  }
}