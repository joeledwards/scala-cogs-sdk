package io.cogswell.pubsub.responses

import play.api.libs.json.JsValue
import play.api.libs.json.Json

case class UnknownErrorResponse(
    sequence: Long,
    action: String,
    message: String = "Internal Error",
    details: Option[String] = None
) {
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
}