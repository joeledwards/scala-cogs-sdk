package io.cogswell.pubsub.responses

import play.api.libs.json.JsError

case class ParseException(
    message: String,
    jsError: Option[JsError],
    cause: Option[Throwable] = null
) extends Throwable(message, cause.getOrElse(null)) {
  
}