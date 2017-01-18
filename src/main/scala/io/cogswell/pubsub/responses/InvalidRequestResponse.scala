package io.cogswell.pubsub.responses

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import scala.util.Try

case class InvalidRequestResponse(
    action: String,
    code: Int,
    message: String,
    details: Option[String],
    badRequest: Option[String]
) extends ServerResponse[InvalidRequestResponse] {
  override val requiredAction = None
  override val requiredCode = Some(400)
  override def self = this
}

object InvalidRequestResponse {
  lazy implicit val eventRecordReads: Reads[InvalidRequestResponse] = (
    (JsPath \ "action").read[String]  and
    (JsPath \ "code").read[Int]  and
    (JsPath \ "message").read[String]  and
    (JsPath \ "details").readNullable[String]  and
    (JsPath \ "bad_request").readNullable[String]
  )(InvalidRequestResponse.apply _)
  
  def parse(json: JsValue): JsResult[InvalidRequestResponse] = {
    json.validate[InvalidRequestResponse]
  }
}