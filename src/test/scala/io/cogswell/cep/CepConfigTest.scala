package io.cogswell.cep

import org.scalatest._
import java.util.concurrent.TimeUnit
import play.api.libs.json.JsError
import play.api.libs.json.JsSuccess
import play.api.libs.json.Json
import scala.concurrent.duration.Duration

class CepConfigTest extends FlatSpec with Matchers {
  "CepConfig.validate()" should "validate a config with all values supplied" in {
    CepConfig.validate(Json.obj(
      "api_key" -> Json.obj(
        "access" -> "aa",
        "secret" -> "bb"
      ),
      "client_key" -> Json.obj(
        "salt" -> "cc",
        "secret" -> "dd"
      ),
      "http_request_timeout" -> 5000,
      "websocket_connect_timeout" -> 5000,
      "websocket_auto_reconnect" -> false,
      "base_url" -> "https://qa-api.cogswell.io"
    )) should be (JsSuccess(
      CepConfig(
        CepApiKey("aa", Some("bb")),
        Some(CepClientKey("cc", "dd")),
        Duration(5, TimeUnit.SECONDS),
        Duration(5, TimeUnit.SECONDS),
        false, "https://qa-api.cogswell.io"
      )
    ))
  }
  
  it should "validate a tools config with only the required options supplied" in {
    CepConfig.validate(Json.obj(
      "api_key" -> Json.obj(
        "access" -> "aa",
        "secret" -> "bb"
      )
    )) should be (JsSuccess(
      CepConfig(CepApiKey("aa", Some("bb")))
    ))
  }
  
  it should "validate a client config with only the required options supplied" in {
    CepConfig.validate(Json.obj(
      "api_key" -> Json.obj(
        "access" -> "aa"
      ),
      "client_key" -> Json.obj(
        "salt" -> "cc",
        "secret" -> "dd"
      )
    )) should be (JsSuccess(
      CepConfig(
        CepApiKey("aa", None),
        Some(CepClientKey("cc", "dd"))
      )
    ))
  }
  
  it should "not validate a config with an invalid type for a timeout" in {
    CepConfig.validate(Json.obj(
      "api_key" -> Json.obj(
        "access" -> "aa",
        "secret" -> Some("bb")
      ),
      "http_request_timeout" -> "5000"
    )) match {
      case JsSuccess(_, _) => fail()
      case JsError(_) => 
    }
  }
  
  it should "not validate a config with a client key but no access component of the api key" in {
    CepConfig.validate(Json.obj(
      "client_key" -> Json.obj(
        "salt" -> "cc",
        "secret" -> "dd"
      )
    )) match {
      case JsSuccess(_, _) => fail()
      case JsError(_) => 
    }
  }
}