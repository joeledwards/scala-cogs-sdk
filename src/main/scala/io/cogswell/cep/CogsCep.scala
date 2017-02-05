package io.cogswell.cep

import scala.concurrent.Future
import java.io.File
import java.io.FileReader
import java.io.FileInputStream
import java.io.InputStreamReader
import java.io.BufferedInputStream
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import play.api.libs.json.Json
import play.api.libs.json.JsObject
import play.api.libs.json.JsValue
import play.api.libs.json.JsSuccess
import play.api.libs.json.JsError
import io.cogswell.cep.exceptions.CepConfigException
import scala.concurrent.ExecutionContext
import scala.util.Failure

object CogsCep {
  def getHandle(config: CepConfig): CepHandle = CepHandle(config)
  
  def getHandle(configFile: File)(implicit ec: ExecutionContext): Future[CepHandle] = {
    Future {
      Json.parse(new BufferedInputStream(new FileInputStream(configFile))) match {
        case json: JsValue => CepConfig.validate(json) match {
          case JsSuccess(config, _) => getHandle(config)
          case error:JsError =>
            throw new CepConfigException("Config file format is invalid", None, Some(error))
        }
        case _ => throw new CepConfigException("Config file does not contain valid JSON.")
      }
    } recover { case error =>
      throw new CepConfigException("Error reading config file:", Some(error))
    }
  }
}