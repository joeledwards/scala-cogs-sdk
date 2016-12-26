package io.cogswell.pubsub.requests

import play.api.libs.json.JsValue
import play.api.libs.json.Json

case class UnsubscribeRequest(
  sequence: Long,
  channel: String
) {
  def toJson: JsValue = {
    Json.obj(
        "seq" -> sequence,
        "action" -> "unsubscribe",
        "chan" -> channel
    )
  }
}