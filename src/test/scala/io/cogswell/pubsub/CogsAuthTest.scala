package io.cogswell.pubsub

import org.scalatest._

class CogsAuthTest extends FlatSpec with Matchers {
  "CogsAuth.splitKey()" should "split a key with the correct number of sections" in {
    CogsAuth.splitKey("A-dead-beef") should be (Some(CogsKey("A", "dead", "beef")))
  }
  
  it should "fail to split a key with too few sections" in {
    CogsAuth.splitKey("A-beef") should be (None)
  }
  
  it should "fail to split a key with too many sections" in {
    CogsAuth.splitKey("A-dead-dead-beef") should be (None)
  }
  
  "CogsAuth.authContent()" should "generate auth content for a valid key" in {
    CogsAuth.authContent(Seq("A-dead-beef")) match {
      case None => fail("Should have generated value content")
      case Some(auth) => {
        auth.hmac.length should be (64)
        auth.hmac.isEmpty should be (false)
      }
    }
  }
  
  it should "fail to generate auth content for invalid keys" in {
    CogsAuth.authContent(Seq()) should be (None)
    CogsAuth.authContent(Seq("")) should be (None)
    CogsAuth.authContent(Seq("A-beef")) should be (None)
    CogsAuth.authContent(Seq("A-dead-dead-beef")) should be (None)
  }
}