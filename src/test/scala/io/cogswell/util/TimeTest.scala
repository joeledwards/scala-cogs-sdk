package io.cogswell.util

import org.scalatest._
import org.joda.time.DateTime

class TimeTest extends FlatSpec with Matchers {
  "Time" should "format a DateTime as an ISO-8601 timestamp" in {
    Time.toIso(new DateTime(0L)) should be ("1970-01-01T00:00:00.000Z")
  }
  
  it should "parse an ISO-8601 timestamp into a DateTime" in {
    Time.fromIso("1970-01-01T00:00:00.000Z") should be (new DateTime(0L))
  }
}