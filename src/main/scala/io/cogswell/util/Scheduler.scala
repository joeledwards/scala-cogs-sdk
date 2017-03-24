package io.cogswell.util

import java.util.concurrent.ScheduledThreadPoolExecutor
import scala.concurrent.duration.Duration

object Scheduler {
  lazy private val scheduler = new ScheduledThreadPoolExecutor(4)
  
  /**
   * Execute an operation in a new thread.
   * 
   * @param actionthe action to execute
   */
  def execute(action: => Unit): Unit = {
    scheduler.execute(new Runnable {
      def run(): Unit = action
    })
  }
  
  /**
   * Schedule a function to be performed after some delay.
   * 
   * @param delay the delay before performing the action
   * @param action the action to schedule
   */
  def schedule(delay: Duration)(action: => Unit): Unit = {
    scheduler.schedule(new Runnable {
      def run(): Unit = action
    }, delay.length, delay.unit)
  }
  
  /**
   * Schedule an action to be performed at a regular interval.
   * 
   * @param interval the frequency at which to repeat the action
   * @param initialDelay an optional delay before the first invocation of the action
   * @param action the action to repeat
   */
  def repeat(
      interval: Duration, initialDelay: Duration = Duration.Zero
  )(action: => Unit): Unit = {
    def doAgain(): Unit = {
      action
      schedule(interval)(doAgain)
    }
    
    schedule(initialDelay)(doAgain)
  }
}