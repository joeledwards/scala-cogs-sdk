package io.cogswell.pubsub.responses

/**
 * Any response which contains a sequence.
 */
trait SequencedResponse {
  def sequence: Long
}