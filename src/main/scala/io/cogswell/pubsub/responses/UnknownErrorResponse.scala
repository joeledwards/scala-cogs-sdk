package io.cogswell.pubsub.responses

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import io.cogswell.pubsub.records.ServerRecord
import play.api.libs.json.JsValue
import play.api.libs.json.Json

case class UnknownErrorResponse(
    sequence: Long,
    action: String,
    message: String = "Internal Error",
    details: Option[String] = None
) extends ServerRecord(
    recordSequence = Some(sequence),
    recordAction = Some(action),
    recordCode = None,
    requiredAction = None,
    requiredCode = None
) {
  /*
  def toJson: JsValue = {
    val json = Json.obj(
        "seq" -> sequence,
        "action" -> action,
        "code" -> 500,
        "message" -> message
    )
    
    details match {
      case Some(d) => json ++ Json.obj("details" -> details)
      case None => json
    }
  }
  */
}

object UnknownErrorResponse {
  lazy implicit val eventRecordReads: Reads[UnknownErrorResponse] = (
    (JsPath \ "seq").read[Long]  and
    (JsPath \ "action").read[String]  and
    (JsPath \ "message").read[String]  and
    (JsPath \ "details").readNullable[String]
  )(UnknownErrorResponse.apply _)
  
  def parse(json: JsValue): JsResult[UnknownErrorResponse] = {
    json.validate[UnknownErrorResponse]
  }
}