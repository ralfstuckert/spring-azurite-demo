package com.github.ralfstuckert.springazuritedemo

import com.azure.spring.messaging.storage.queue.core.StorageQueueTemplate
import com.ninjasquad.springmockk.MockkBean
import io.kotest.matchers.shouldBe
import io.mockk.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.integration.support.MessageBuilder
import org.springframework.messaging.Message
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds


@ActiveProfiles("azurite")
@SpringBootTest(properties = ["demo.storage.queue.sender.enabled=false"])
class MessageReceiverTest:AzuriteTestcontainer() {

    @MockkBean()
    lateinit var messageHandlerMock: DemoMessageHandler

    @Autowired
    lateinit var storageQueueTemplate: StorageQueueTemplate

    val messageSlot = slot< Message<*>>()

    @BeforeEach
    fun setup() {
        clearAllMocks()
    }

    @Test
    fun `receive message`() {
        every { messageHandlerMock.handleMessage(capture(messageSlot)) } just Runs

        storageQueueTemplate.send(DEMO_QUEUE, MessageBuilder.withPayload("Yeehaw").build())

        val message = messageSlot.awaitMessage()
        message.text() shouldBe "Yeehaw"
    }

    fun CapturingSlot< Message<*>>.awaitMessage(timeout:Duration = 3.seconds): Message<*> = runBlocking {
        withTimeout(timeout) {
            while (!this@awaitMessage.isCaptured ) {
                delay(100)
            }
            this@awaitMessage.captured
        }
    }


}