package io.cogswell.util

import org.scalatest._

class MarkOnceTest extends FlatSpec with Matchers {
  "MarkOnce" should "report unmarked after instantiation" in {
    (new MarkOnce).isMarked should be (false)
  }
  
  "MarkOnce" should "be marked only once" in {
    val mark = new MarkOnce
    mark.mark should be (true)
    mark.mark should be (false)
    mark.mark should be (false)
  }
  
  "MarkOnce" should "report marked after mark is called" in {
    val mark = new MarkOnce
    mark.mark
    mark.isMarked should be (true)
  }
}