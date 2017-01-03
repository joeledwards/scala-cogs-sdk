package io.cogswell.util

import javax.xml.bind.DatatypeConverter

object Transcoder {
  case class Bytes(bytes: Array[Byte]) { 
    /**
     * Hex encode the bytes.
     */
    def hex: Str = Str(DatatypeConverter.printHexBinary(bytes).toLowerCase)
    
    /**
     * Base-64 encode the bytes.
     */
    def base64: Str = Str(DatatypeConverter.printBase64Binary(bytes))
    
    /**
     * UTF-8 decode the bytes.
     */
    def utf8: Str = Str(new String(bytes, "UTF-8"))
    
    /**
     * ASCII decode the bytes.
     */
    def ascii: Str = Str(new String(bytes, "US-ASCII"))
  }
  
  case class Str(string: String) {
    /**
     * Hex decode the string.
     */
    def hex: Bytes = Bytes(DatatypeConverter.parseHexBinary(string))
    
    /**
     * Base-64 decode the string.
     */
    def base64: Bytes = Bytes(DatatypeConverter.parseBase64Binary(string))
    
    /**
     * UTF-8 encode the string.
     */
    def utf8: Bytes = Bytes(string.getBytes("utf-8"))
    
    /**
     * ASCII encode the string.
     */
    def ascii: Bytes = Bytes(string.getBytes("ascii"))
  }
  
  /**
   * Creates a new Bytes, wrapping the supplied byte array.
   */
  def bytes(bytes: Array[Byte]): Bytes = Bytes(bytes)
  
  /**
   * Creates a new Str, wrapping the supplied string.
   */
  def string(string: String): Str = Str(string)
}