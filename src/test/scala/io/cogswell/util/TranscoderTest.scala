package io.cogswell.util

import org.scalatest._

class TranscoderTest extends FlatSpec with Matchers {
  "transcoder" should "assemble a Str which contains the string" in {
    Transcoder.string("test").string should be ("test")
  }
  
  it should "assemble a Bytes which contains the byte array" in {
    Transcoder.bytes(Array(1,2,3)).bytes should be (Array(1,2,3))
  }
  
  it should "transcode string to/from ascii" in {
    Transcoder.string("text").ascii.bytes should be (Array(116,101,120,116))
    Transcoder.bytes(Array(116,101,120,116)).ascii.string should be ("text")
    Transcoder.string("text").ascii.ascii.string should be ("text")
  }
  
  it should "transcode string to/from utf-8" in {
    Transcoder.string("λ").utf8.bytes should be (Array(-50, -69))
    Transcoder.bytes(Array(-50, -69)).utf8.string should be ("λ")
    Transcoder.string("λ").utf8.utf8.string should be ("λ")
  }
  
  it should "transcode bytes to/from hex" in {
    Transcoder.bytes(Array(1,2,3)).hex.string should be ("010203")
    Transcoder.string("010203").hex.bytes should be (Array(1,2,3))
    Transcoder.bytes(Array(1,2,3)).hex.hex.bytes should be (Array(1,2,3))
  }
  
  it should "transcode bytes to/from base-64" in {
    Transcoder.bytes(Array(1,2,3)).base64.string should be ("AQID")
    Transcoder.string("AQID").base64.bytes should be (Array(1,2,3))
    Transcoder.bytes(Array(1,2,3)).base64.base64.bytes should be (Array(1,2,3))
  }
  
  it should "fail to decode invalid hex text" in {
    try {
      Transcoder.string("not hex").hex.bytes
      fail("Should have failed to decode invalid text as hex.")
    } catch {
      case cause: IllegalArgumentException =>
      case _: Throwable => fail("Failed, but with unexpected exception.")
    }
  }
}