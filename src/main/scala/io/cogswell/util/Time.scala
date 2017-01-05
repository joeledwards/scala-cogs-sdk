package io.cogswell.util

import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.ISODateTimeFormat

object Time {
  DateTimeZone.setDefault(DateTimeZone.UTC)
  
  private lazy val formatter = ISODateTimeFormat.dateTime()
  private lazy val parser = ISODateTimeFormat.dateTimeParser()
  
  def utc(time: DateTime) = time.withZone(DateTimeZone.UTC)
  
  def now(): DateTime = DateTime.now()
  def nowUtc(): DateTime = utc(now())
  
  def fromIso(timestamp: String): DateTime = parser.parseDateTime(timestamp)
  def toIso(time: DateTime): String = formatter.print(time.getMillis)
  def nowIso(): String = toIso(now())
}