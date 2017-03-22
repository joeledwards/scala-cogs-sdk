package io.cogswell.pubsub.records

import java.util.UUID

import scala.util.Failure
import scala.util.Success
import scala.util.Try

import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._



case class MessageRecord(
    id: UUID,
    action: String,
    time: DateTime,
    channel: String,
    message: String
) extends ServerRecord(
    recordSequence = None,
    recordAction = Some(action),
    recordCode = None,
    requiredAction = Some("msg"),
    requiredCode = None
)

object MessageRecord {
  lazy val dateTimeReads: Reads[DateTime] = new Reads[DateTime] {
    override def reads(json: JsValue): JsResult[DateTime] = {
      json match {
        case JsString(text) => Try[DateTime] {
          DateTime.parse(text, ISODateTimeFormat.dateTime())
        } match {
          case Success(date) => JsSuccess(date)
          case Failure(error) => JsError()
        }
        case _ => JsError()
      }
    }
  }
  
  lazy implicit val eventRecordReads: Reads[MessageRecord] = (
    (JsPath \ "id").read[UUID]  and
    (JsPath \ "action").read[String]  and
    (JsPath \ "time").read[DateTime](dateTimeReads)  and
    (JsPath \ "chan").read[String] and
    (JsPath \ "msg").read[String]
  )(MessageRecord.apply _)
  
  def parse(json: JsValue): JsResult[MessageRecord] = {
    json.validate[MessageRecord]
  }
}