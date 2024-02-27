package com.github.ralfstuckert.springazuritedemo

import com.azure.storage.queue.QueueClientBuilder
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class AzureQueueStorageTest : AzuriteTestcontainer() {

    val queueClient =
        QueueClientBuilder()
            .connectionString(queueConnectionString)
            .queueName(DEMO_QUEUE)
            .buildClient()

    val queueStorage = AzureQueueStorage(queueClient)



    @Test
    fun sendMessage() {
        queueStorage.sendMessage("hihi")

        queueClient.receiveMessage().body.toBytes().toString(Charsets.UTF_8) shouldBe "hihi"
    }


    @Test
    fun receiveMessage() {
        queueStorage.receiveMessage() shouldBe null

        queueClient.sendMessage("hihi")
        queueStorage.receiveMessage()?.body?.toBytes()?.toString(Charsets.UTF_8) shouldBe "hihi"
    }


}