package io.cogswell.pubsub

import play.api.libs.json.Json
import io.cogswell.util.Crypto
import io.cogswell.util.Transcoder
import io.cogswell.util.Time
import java.util.UUID
import play.api.libs.json.JsString

case class CogsAuthData(
    hmac: String,
    payload: String
)

case class CogsKey(
    perm: String,
    identity: String,
    key: String
)

object CogsAuth {
  /**
   * Generate the authentication content for the connect request from
   * a Seq of keys.
   */
  def authContent(
      keys: Seq[String], sessionUuid: Option[UUID] = None
  ): Option[CogsAuthData] = {
    val cogsKeys = keys.map(splitKey(_)).filter(_.isDefined).map(_.get)
    
    if (cogsKeys.size < 1) {
      None
    } else {
      val identity = cogsKeys(0).identity
      val permissions = cogsKeys.map(_.perm).mkString("")
      val timestamp = Time.nowIso
      
      val baseJson =  Json.obj(
        "identity" -> identity,
        "permissions" -> permissions,
        "security_timestamp" -> timestamp
      )
      
      val json = sessionUuid match {
        case Some(uuid) => baseJson ++ Json.obj("session_uuid" -> uuid.toString)
        case None => baseJson
      }
      
      val message = Transcoder.string(json.toString).utf8
      val payload = message.base64
      
      val hmac = cogsKeys.map(_.key).map { hexKey =>
        val key = Transcoder.string(hexKey).hex.bytes
        Crypto.hmacSha256(key, message.bytes)
      }.foldLeft(Array.emptyByteArray) { (a: Array[Byte], b: Array[Byte]) =>
        val combined = new Array[Byte](Math.max(a.length, b.length))
        for (i <- 0 until a.length) combined(i) = (combined(i) ^ a(i)).asInstanceOf[Byte]
        for (i <- 0 until b.length) combined(i) = (combined(i) ^ b(i)).asInstanceOf[Byte]
        combined
      }
      
      val hexHmac = Transcoder.bytes(hmac).hex.string
      
      Some(CogsAuthData(hexHmac, payload.string))
    }
  }
  
  def splitKey(key: String): Option[CogsKey] = {
    key.split("-") match {
      case Array(perm, identity, key) => Some(CogsKey(perm, identity, key))
      case _ => None
    }
  }
}