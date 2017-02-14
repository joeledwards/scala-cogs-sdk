package io.cogswell.pubsub.responses

import scala.util.Failure
import scala.util.Success
import scala.util.Try

import io.cogswell.pubsub.records.ServerRecord
import io.cogswell.exceptions.CogsParseException

/**
 * This class represents any type of response received from the server.
 * This does not include messages which are the result of a publish, or
 * other notifications which the server may send.
 */
abstract class ServerResponse[Self <: ServerResponse[Self]] extends ServerRecord[Self] {
  val requiredCode: Option[Int]
  val code: Int
  
  /**
   * First perform the validation of the super class (ServerRecord).
   * Then validate that the code matches if it is required.
   */
  override def validate: Try[Self] = {
    val w: Try[Self] = super.validate flatMap { _ =>
      requiredCode match {
        case Some(reqCode) => reqCode match {
          case `code` => Success(self)
          case _ => Failure(CogsParseException(
              s"Invalid code '$code' for type ${this.getClass.getName}", None
          ))
        }
        case None => Success(self)
      }
    }
    
    w
  }
}