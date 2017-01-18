package io.cogswell.pubsub.records

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import scala.util.Try
import java.util.UUID
import java.util.UUID
import org.joda.time.DateTime
import java.util.UUID

case class MessageRecord(
    id: UUID,
    action: String,
    time: DateTime,
    channel: String,
    message: String
)

object MessageRecord {
  lazy implicit val eventRecordReads: Reads[MessageRecord] = (
    (JsPath \ "id").read[UUID]  and
    (JsPath \ "action").read[String]  and
    (JsPath \ "time").read[DateTime]  and
    (JsPath \ "channel").read[String] and
    (JsPath \ "message").read[String]
  )(MessageRecord.apply _)
  
  def parse(json: JsValue): JsResult[MessageRecord] = {
    json.validate[MessageRecord]
  }
}