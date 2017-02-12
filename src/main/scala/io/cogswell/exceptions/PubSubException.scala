package io.cogswell.exceptions

case class PubSubException(
    message: String,
    cause: Option[Throwable] = None
) extends RuntimeException(
    message, cause.getOrElse(null)
)