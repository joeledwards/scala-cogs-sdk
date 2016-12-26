package io.cogswell.pubsub.requests

import play.api.libs.json.JsValue
import play.api.libs.json.Json

case class UnsubscribeAllRequest (
  sequence: Long
) {
  def toJson: JsValue = {
    Json.obj(
        "seq" -> sequence,
        "action" -> "unsubscribe-all"
    )
  }
}