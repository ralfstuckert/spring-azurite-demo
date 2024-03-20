package com.github.ralfstuckert.springazuritedemo

import com.azure.spring.messaging.storage.queue.core.StorageQueueTemplate
import io.kotest.matchers.shouldBe
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.integration.support.MessageBuilder
import org.springframework.messaging.Message

class MessageSenderTest {

    val storageQueueTemplateMock = mockk<StorageQueueTemplate>(relaxed = true)
    val sender = MessageSender(storageQueueTemplateMock)
    val messageSlot = slot< Message<*>>()


    @BeforeEach
    fun setup() {
        clearAllMocks()
    }


    @Test
    fun run() {
        every { storageQueueTemplateMock.send(any(), capture(messageSlot)) } just Runs

        sender.run(null)

        val captured = messageSlot.captured
        captured.payload shouldBe "Hello"

        verify {  storageQueueTemplateMock.send(DEMO_QUEUE, captured) }
    }
}