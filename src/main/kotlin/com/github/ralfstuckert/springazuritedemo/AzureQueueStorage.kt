package com.github.ralfstuckert.springazuritedemo

import com.azure.storage.queue.QueueClient
import com.azure.storage.queue.models.QueueMessageItem
import org.springframework.stereotype.Service
import java.util.concurrent.atomic.AtomicBoolean

@Service
class AzureQueueStorage(val queueClient: QueueClient) {

    private val queueCreated = AtomicBoolean()

    val queueName
        get() = queueClient.queueName

    fun ensureQueueExists() {
        if (queueCreated.getAndSet(true))
            return
        else
            queueClient.createIfNotExists()
    }

    // TODO String vs binary
    fun sendMessage(message: String) {
        ensureQueueExists()
        queueClient.sendMessage(message);
    }

    fun receiveMessage(): QueueMessageItem? {
        ensureQueueExists()
        return queueClient.receiveMessage()
    }

}


