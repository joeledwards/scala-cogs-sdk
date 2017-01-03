package io.cogswell.util

import org.scalatest._

class CryptoTest extends FlatSpec with Matchers {
  private val hmacKey = Transcoder.string("key").ascii.bytes
  private val msg = "The quick brown fox jumps over the lazy dog"
  private val message = Transcoder.string(msg).ascii.bytes
  
  private def rawDigest(hexDigest: String): Array[Byte] = {
    Transcoder.string(hexDigest).hex.bytes
  }
  
  private def hex(bytes: Array[Byte]): String = {
    Transcoder.bytes(bytes).hex.string
  }
  
  "crypto: hash" should "generate a correct MD5 digest" in {
    val hexDigest = "9e107d9d372bb6826bd81d3542a419d6"
    val digest = rawDigest(hexDigest)
    hex(Crypto.md5(message)) should be (hexDigest)
    Crypto.md5(message) should be (digest)
  }
  
  it should "generate a correct SHA-1 digest" in {
    val hexDigest = "2fd4e1c67a2d28fced849ee1bb76e7391b93eb12"
    val digest = rawDigest(hexDigest)
    hex(Crypto.sha1(message)) should be (hexDigest)
    Crypto.sha1(message) should be (digest)
  }
  
  it should "generate a correct SHA-256 digest" in {
    val hexDigest = "d7a8fbb307d7809469ca9abcb0082e4f8d5651e46d3cdb762d02d0bf37c9e592"
    val digest = rawDigest(hexDigest)
    hex(Crypto.sha256(message)) should be (hexDigest)
    Crypto.sha256(message) should be (digest)
  }
  
  it should "generate a correct SHA-512 digest" in {
    val hexDigest = "07e547d9586f6a73f73fbac0435ed76951218fb7d0c8d788a309d785436bbb642e93a252a954f23912547d1e8a3b5ed6e1bfd7097821233fa0538f3db854fee6"
    val digest = rawDigest(hexDigest)
    hex(Crypto.sha512(message)) should be (hexDigest)
    Crypto.sha512(message) should be (digest)
  }
  
  "crypto: hmac" should "generate a correct MD5 HMAC" in {
    val hexDigest = "80070713463e7749b90c2dc24911e275"
    val digest = rawDigest(hexDigest)
    hex(Crypto.hmacMd5(hmacKey, message)) should be (hexDigest)
    Crypto.hmacMd5(hmacKey, message) should be (digest)
  }
  
  it should "generate a correct SHA-1 HMAC" in {
    val hexDigest = "de7c9b85b8b78aa6bc8a7a36f70a90701c9db4d9"
    val digest = rawDigest(hexDigest)
    hex(Crypto.hmacSha1(hmacKey, message)) should be (hexDigest)
    Crypto.hmacSha1(hmacKey, message) should be (digest)
  }
  
  it should "generate a correct SHA-256 HMAC" in {
    val hexDigest = "f7bc83f430538424b13298e6aa6fb143ef4d59a14946175997479dbc2d1a3cd8"
    val digest = rawDigest(hexDigest)
    hex(Crypto.hmacSha256(hmacKey, message)) should be (hexDigest)
    Crypto.hmacSha256(hmacKey, message) should be (digest)
  }
}