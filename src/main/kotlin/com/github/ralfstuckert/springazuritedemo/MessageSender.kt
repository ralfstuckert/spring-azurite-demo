package com.github.ralfstuckert.springazuritedemo

import com.azure.spring.messaging.storage.queue.core.StorageQueueTemplate
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.integration.support.MessageBuilder
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(name = ["demo.storage.queue.sender.enabled"], matchIfMissing = true)
class MessageSender(val storageQueueTemplate: StorageQueueTemplate):ApplicationRunner {

    override fun run(args: ApplicationArguments?) {
        storageQueueTemplate.send(DEMO_QUEUE, MessageBuilder.withPayload("Hello").build())
    }
}