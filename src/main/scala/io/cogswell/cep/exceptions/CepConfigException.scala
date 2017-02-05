package io.cogswell.cep.exceptions

import play.api.libs.json.JsError

class CepConfigException(
  message: String,
  error: Option[Throwable] = None,
  jsonError: Option[JsError] = None
) extends RuntimeException(message, error.getOrElse(null))