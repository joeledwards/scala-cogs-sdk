package io.cogswell.exceptions

import play.api.libs.json.JsError

case class CogsParseException(
    message: String,
    jsError: Option[JsError],
    cause: Option[Throwable] = null
) extends RuntimeException(message, cause.getOrElse(null))