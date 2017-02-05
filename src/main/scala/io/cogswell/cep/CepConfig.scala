package io.cogswell.cep

import scala.concurrent.duration.Duration
import java.util.concurrent.TimeUnit
import scala.util.Try
import java.util.UUID

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._

case class CepApiKey(
    access: String,
    secret: Option[String] = None
)

case class CepClientKey(
    salt: String,
    secret: String
)

case class CepConfig(
    apiKey: CepApiKey,
    clientKey: Option[CepClientKey] = None,
    httpRequestTimeout: Duration = CepConfig.defaultTimeout,
    websocketConnectTimeout: Duration = CepConfig.defaultTimeout,
    websocketAutoReconnect: Boolean = true,
    baseUrl: String = CepConfig.defaultBaseUrl
) {
  (apiKey, clientKey) match {
    case (CepApiKey(_, None), None) =>
      throw new IllegalArgumentException(
          "Must supply the API secret key if no client key is supplied"
      )
    case _ =>
  }
}

object CepConfig {
  private val defaultBaseUrl: String = "https://api.cogswell.io"
  private val defaultTimeout: Duration = Duration(30, TimeUnit.SECONDS)
  
  /**
   * Delegate for apply which accepts Option-wrapped instances
   * of all values which have have a default, and applies the
   * default to the returned CepConfig instead when None.
   */
  private def applyDefaults(
      apiKey: CepApiKey,
      clientKey: Option[CepClientKey],
      httpRequestTimeout: Option[Duration],
      websocketConnectTimeout: Option[Duration],
      websocketAutoReconnect: Option[Boolean],
      baseUrl: Option[String]
  ): CepConfig = {
    CepConfig(
        apiKey, clientKey,
        httpRequestTimeout.getOrElse(defaultTimeout),
        websocketConnectTimeout.getOrElse(defaultTimeout),
        websocketAutoReconnect.getOrElse(true),
        baseUrl.getOrElse(defaultBaseUrl)
    )
  }
  
  lazy implicit val cepApiKeyReads: Reads[CepApiKey] = (
    (JsPath \ "access").read[String] and
    (JsPath \ "secret").readNullable[String]
  )(CepApiKey.apply _)
  
  lazy implicit val cepClientKeyReads: Reads[CepClientKey] = (
    (JsPath \ "salt").read[String] and
    (JsPath \ "secret").read[String]
  )(CepClientKey.apply _)
  
  lazy implicit val cepConfigReads: Reads[CepConfig] = (
    (JsPath \ "api_key").read[CepApiKey] and
    (JsPath \ "client_key").readNullable[CepClientKey] and
    (JsPath \ "http_request_timeout").readNullable[Long].map(_.map(Duration(_, TimeUnit.MILLISECONDS))) and
    (JsPath \ "websocket_connect_timeout").readNullable[Long].map(_.map(Duration(_, TimeUnit.MILLISECONDS))) and
    (JsPath \ "websocket_auto_reconnect").readNullable[Boolean] and
    (JsPath \ "base_url").readNullable[String]
  )(CepConfig.applyDefaults _)
  
  def validate(json: JsValue): JsResult[CepConfig] = {
    json.validate[CepConfig]
  }
}
