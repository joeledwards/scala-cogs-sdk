package io.cogswell.util

import org.scalatest._

class SetOnceTest extends FlatSpec with Matchers {
  "SetOnce" should "report unset after instantiation" in {
    (new SetOnce[Long]).isSet should be (false)
  }
  
  "SetOnce" should "not contain a value after instantiation" in {
    (new SetOnce[Long]).value should be (None)
  }
  
  "SetOnce" should "be set only once" in {
    val set = new SetOnce[Long]
    set.set(1L) should be (true)
    set.set(2L) should be (false)
  }
  
  "SetOnce" should "report being set after set is called" in {
    val set = new SetOnce[Long]
    set.set(1L)
    set.isSet should be (true)
  }
  
  "SetOnce" should "contain a value after set is called" in {
    val set = new SetOnce[Long]
    set.set(1L)
    set.value should be (Some(1L))
  }
  
  "SetOnce" should "contain only the first value set" in {
    val set = new SetOnce[Long]
    set.set(3L)
    set.set(1L)
    set.set(2L)
    
    set.value should be (Some(3L))
  }
}