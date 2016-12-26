package io.cogswell.pubsub.requests

import play.api.libs.json.JsValue
import play.api.libs.json.Json

case class PublishRequest (
  sequence: Long,
  channel: String,
  message: String
) {
  def toJson: JsValue = {
    Json.obj(
        "seq" -> sequence,
        "action" -> "pub",
        "chan" -> channel,
        "msg" -> message
    )
  }
}