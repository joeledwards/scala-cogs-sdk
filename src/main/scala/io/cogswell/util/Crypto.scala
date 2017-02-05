package io.cogswell.util

import java.security.MessageDigest
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object Crypto {
  private val MD5 = "MD5"
  private val SHA1 = "SHA-1"
  private val SHA256 = "SHA-256"
  private val SHA512 = "SHA-512"
  
  private val HMAC_MD5 = "HmacMD5"
  private val HMAC_SHA1 = "HmacSHA1"
  private val HMAC_SHA256 = "HmacSHA256"
  
  private def digest(algorithm: String, message: Array[Byte]): Array[Byte] = {
    val digest = MessageDigest.getInstance(algorithm)
    digest.update(message)
    digest.digest
  }
  
  private def hmac(
      algorithm: String, key: Array[Byte], message: Array[Byte]
  ): Array[Byte] = {
    val hmac = Mac.getInstance(algorithm)
    val keySpec = new SecretKeySpec(key, algorithm)
    hmac.init(keySpec)
    hmac.update(message)
    hmac.doFinal()
  }
  
  def md5(message: Array[Byte]): Array[Byte] = digest(MD5, message)
  def sha1(message: Array[Byte]): Array[Byte] = digest(SHA1, message)
  def sha256(message: Array[Byte]): Array[Byte] = digest(SHA256, message)
  def sha512(message: Array[Byte]): Array[Byte] = digest(SHA512, message)
  
  def hmacMd5(key: Array[Byte], message: Array[Byte]): Array[Byte] = hmac(HMAC_MD5, key, message)
  def hmacSha1(key: Array[Byte], message: Array[Byte]): Array[Byte] = hmac(HMAC_SHA1, key, message)
  def hmacSha256(key: Array[Byte], message: Array[Byte]): Array[Byte] = hmac(HMAC_SHA256, key, message)
}