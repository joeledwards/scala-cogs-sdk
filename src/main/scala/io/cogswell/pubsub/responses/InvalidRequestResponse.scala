package io.cogswell.pubsub.responses

import io.cogswell.pubsub.records.ServerRecord
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.JsPath
import play.api.libs.json.JsResult
import play.api.libs.json.JsValue
import play.api.libs.json.Reads
import play.api.libs.json.Reads.IntReads
import play.api.libs.json.Reads.StringReads

case class InvalidRequestResponse(
    action: String,
    code: Int,
    message: String,
    details: Option[String],
    badRequest: Option[String]
) extends ServerRecord(
    recordSequence = None,
    recordAction = Some(action),
    recordCode = Some(code),
    requiredAction = None,
    requiredCode = Some(400)
)

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