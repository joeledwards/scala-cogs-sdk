package io.cogswell.util

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.concurrent.Promise
import scala.util.Try
import scala.util.Failure
import scala.util.Success

/**
 * Utilities for working with Scala Futures.
 */
object Futures {
  /**
   * This one major piece missing from Future.
   * 
   * @param future the Future to translate
   * @param translator the function which will perform the translation
   * 
   * @return a Future which will either be the Future resulting from the translation,
   * or a failed Future if there was an error invoking the translator function.
   */
  def translate[T, U](future: Future[T])(
    translator: Try[T] => Future[U]
  )(implicit ec: ExecutionContext): Future[U] = {
    val promise = Promise[U]
    
    future.onComplete { result =>
      Try {
        translator(result)
      } match {
        case Success(f) => promise.completeWith(f)
        case Failure(e) => promise.failure(e)
      }
    }
    
    promise.future
  }
}