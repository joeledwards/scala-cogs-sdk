package io.cogswell.util

import java.util.concurrent.ScheduledThreadPoolExecutor
import scala.concurrent.duration.Duration

object Scheduler {
  lazy private val scheduler = new ScheduledThreadPoolExecutor(4)
  
  def execute(action: => Unit): Unit = {
    scheduler.execute(new Runnable {
      def run(): Unit = action
    })
  }
  
  def schedule(delay: Duration)(action: => Unit): Unit = {
    scheduler.schedule(new Runnable {
      def run(): Unit = action
    }, delay.length, delay.unit)
  }
}