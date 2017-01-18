package io.cogswell.pubsub.responses

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import scala.util.Try
import java.util.UUID

case class UnsubscribeNotFoundResponse(
    sequence: Long,
    action: String,
    code: Int,
    message: String,
    details: Option[String]
) extends ServerResponse[UnsubscribeNotFoundResponse] {
  override val requiredAction = Some("unsubscribe")
  override val requiredCode = Some(404)
  override def self = this
}

object UnsubscribeNotFoundResponse {
  lazy implicit val eventRecordReads: Reads[UnsubscribeNotFoundResponse] = (
    (JsPath \ "sequence").read[Long]  and
    (JsPath \ "action").read[String]  and
    (JsPath \ "code").read[Int]  and
    (JsPath \ "message").read[String] and
    (JsPath \ "details").readNullable[String]
  )(UnsubscribeNotFoundResponse.apply _)
  
  def parse(json: JsValue): JsResult[UnsubscribeNotFoundResponse] = {
    json.validate[UnsubscribeNotFoundResponse]
  }
}