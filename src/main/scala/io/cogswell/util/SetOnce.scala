package io.cogswell.util

class SetOnce[T] {
  private var v: Option[T] = None
  
  /**
   * Sets the value of it has not been set.
   * 
   * @param value the value to which the caller is attempting to set this instance
   * 
   * @return true if successfully set; false if already set
   */
  def set(value: T): Boolean = { this.synchronized {
    v match {
      case None => {
        v = Some(value)
        true
      }
      case Some(_) => false
    }
  } /* synchronized */ }
  
  def isSet: Boolean = v.isDefined
  def value: Option[T] = v
}