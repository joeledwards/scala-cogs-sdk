package io.cogswell.util

import java.util.concurrent.TimeUnit

import scala.concurrent.Await
import scala.concurrent.Promise
import scala.concurrent.duration.Duration

import org.scalatest._

class SchedulerTest extends FlatSpec with Matchers {
  private def threadId: Long = Thread.currentThread.getId
  
  "Scheduler" should "schedule a function to run immediately" in {
    val promise = Promise[Long]
    
    Scheduler.execute {
      promise.success(threadId)
    }
    
    val bgId = Await.result(promise.future, Duration(250L, TimeUnit.MILLISECONDS))
    bgId should not be (threadId)
  }
  
  "Scheduler" should "schedule a function to run in 1 ms" in {
    val promise = Promise[Long]
    
    Scheduler.schedule(Duration(1L, TimeUnit.MILLISECONDS)) {
      promise.success(threadId)
    }
    
    val bgId = Await.result(promise.future, Duration(250L, TimeUnit.MILLISECONDS))
    bgId should not be (threadId)
  }
}