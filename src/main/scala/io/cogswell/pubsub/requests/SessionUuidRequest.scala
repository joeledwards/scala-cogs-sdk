package io.cogswell.pubsub.requests

import play.api.libs.json.JsValue
import play.api.libs.json.Json

case class SessionUuidRequest(
    sequence: Long
) {
  def toJson: JsValue = {
    Json.obj(
        "seq" -> sequence,
        "action" -> "session-uuid"
    )
  }
}