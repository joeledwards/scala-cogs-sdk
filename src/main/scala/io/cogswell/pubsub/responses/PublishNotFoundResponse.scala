package io.cogswell.pubsub.responses

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import scala.util.Try
import java.util.UUID

case class PublishNotFoundResponse(
    sequence: Long,
    action: String,
    code: Int,
    message: String,
    details: Option[String]
) extends ServerResponse[PublishNotFoundResponse] {
  override val requiredAction = Some("pub")
  override val requiredCode = Some(404)
  override def self = this
}

object PublishNotFoundResponse {
  lazy implicit val eventRecordReads: Reads[PublishNotFoundResponse] = (
    (JsPath \ "sequence").read[Long]  and
    (JsPath \ "action").read[String]  and
    (JsPath \ "code").read[Int]  and
    (JsPath \ "message").read[String] and
    (JsPath \ "details").readNullable[String]
  )(PublishNotFoundResponse.apply _)
  
  def parse(json: JsValue): JsResult[PublishNotFoundResponse] = {
    json.validate[PublishNotFoundResponse]
  }
}