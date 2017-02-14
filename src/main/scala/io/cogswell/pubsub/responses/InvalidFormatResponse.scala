package io.cogswell.pubsub.responses

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import scala.util.Try
import scala.util.Success
import scala.util.Failure
import io.cogswell.pubsub.records.ServerRecord

case class InvalidFormatResponse(
    sequence: Long,
    code: Int,
    action: String,
    message: String,
    details: Option[String] = None
) extends ServerRecord(
    recordSequence = Some(sequence),
    recordAction = Some(action),
    recordCode = Some(code),
    requiredAction = None,
    requiredCode = Some(400)
)

object InvalidFormatResponse {
  lazy implicit val eventRecordReads: Reads[InvalidFormatResponse] = (
    (JsPath \ "seq").read[Long]  and
    (JsPath \ "code").read[Int]  and
    (JsPath \ "action").read[String]  and
    (JsPath \ "message").read[String]  and
    (JsPath \ "details").readNullable[String]
  )(InvalidFormatResponse.apply _)
  
  def parse(json: JsValue): JsResult[InvalidFormatResponse] = {
    json.validate[InvalidFormatResponse]
  }
}