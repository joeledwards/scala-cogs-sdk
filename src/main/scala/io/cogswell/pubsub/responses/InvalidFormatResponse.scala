package io.cogswell.pubsub.responses

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import scala.util.Try
import scala.util.Success
import scala.util.Failure

case class InvalidFormatResponse(
    sequence: Long,
    code: Int,
    action: String,
    message: String,
    details: Option[String] = None
) extends ServerResponse[InvalidFormatResponse] {
  override val requiredAction: Option[String] = None
  override val requiredCode: Option[Int] = Some(400)
  override def self = this
}

object InvlalidFormatResponse {
  lazy implicit val eventRecordReads: Reads[InvalidFormatResponse] = (
    (JsPath \ "sequence").read[Long]  and
    (JsPath \ "code").read[Int]  and
    (JsPath \ "action").read[String]  and
    (JsPath \ "message").read[String]  and
    (JsPath \ "details").readNullable[String]
  )(InvalidFormatResponse.apply _)
  
  def parse(json: JsValue): JsResult[InvalidFormatResponse] = {
    json.validate[InvalidFormatResponse]
  }
}