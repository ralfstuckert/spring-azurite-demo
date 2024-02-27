package com.github.ralfstuckert.springazuritedemo

import com.azure.spring.integration.storage.queue.inbound.StorageQueueMessageSource
import com.azure.spring.messaging.AzureHeaders
import com.azure.spring.messaging.checkpoint.Checkpointer
import com.azure.spring.messaging.storage.queue.core.StorageQueueTemplate
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.integration.annotation.InboundChannelAdapter
import org.springframework.integration.annotation.Poller
import org.springframework.integration.annotation.ServiceActivator
import org.springframework.integration.config.EnableIntegration
import org.springframework.messaging.Message
import org.springframework.messaging.MessageHandler
import org.springframework.stereotype.Component


const val DEMO_QUEUE = "testqueue"


@Component
class DemoMessageHandler() : MessageHandler {

    private val logger = LoggerFactory.getLogger(DemoMessageHandler::class.java)

    override fun handleMessage(message: Message<*>) {
        logger.info("New message received: '${message.text()}'")

        message.checkpoint()
    }


    fun Message<*>.checkpoint() {
        val checkpointer = checkNotNull(this.headers.get(AzureHeaders.CHECKPOINTER, Checkpointer::class.java))
        checkpointer.success()
            .doOnError { t: Throwable ->
                logger.error("checkpoint failed", t)
            }
            .doOnSuccess { t: Void? ->
                logger.debug("Message '{}' successfully checkpointed", this)
            }
            .block()
    }

}

@Configuration
@EnableIntegration
class MessageReceiverConfiguration {

    @Bean
    @InboundChannelAdapter(channel = DEMO_QUEUE, poller = Poller(fixedDelay = "1000", maxMessagesPerPoll = "-1"))
    fun storageQueueMessageSource(
        storageQueueTemplate: StorageQueueTemplate,
        azureQueueStorage: AzureQueueStorage
    ): StorageQueueMessageSource {
        azureQueueStorage.ensureQueueExists()
        return StorageQueueMessageSource(azureQueueStorage.queueName, storageQueueTemplate)
    }

    @Bean
    @ServiceActivator(inputChannel = DEMO_QUEUE)
    // allow to inject handler for testing
    fun messageReceiver(handler: DemoMessageHandler): MessageHandler =
        handler


}

fun Message<*>.text() = when (val payload = this.payload) {
    is ByteArray -> String(payload)
    else -> payload.toString()
}


