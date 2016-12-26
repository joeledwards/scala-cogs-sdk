package io.cogswell.util

import java.util.concurrent.atomic.AtomicBoolean

class MarkOnce {
  private var marked: AtomicBoolean = new AtomicBoolean(false)
  
  /**
   * Marks the value only if it has not yet been marked.
   * 
   * @return true if the this operation marked the value, otherwise false
   */
  def mark: Boolean = marked.compareAndSet(false, true)
  
  /**
   * Indicates whether this instance has been marked.
   * 
   * @return true if marked, otherwise false
   */
  def isMarked: Boolean = marked.get
}