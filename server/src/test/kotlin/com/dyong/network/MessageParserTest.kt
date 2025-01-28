package com.dyong.network

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import org.junit.platform.suite.api.Suite




@Suite
class MessageParserTest: StringSpec({
 "parse network message header+body" {
  @Serializable
  data class JsonObject1(val name: String, val age: Int)

  @Serializable
  data class JsonObject2(val product: String, val price: Double)

  val input = "25{\"name\":\"John\", \"age\":30}{\"product\":\"Laptop\", \"price\":999.99}"

  // Parse the input string and provide the expected types
  val result = MessageParser().parse(input, JsonObject1::class, JsonObject2::class)
  result.first.name shouldBe "John"
  result.second.product shouldBe "Laptop"
  result.second.price shouldBe 999.99
 }
})