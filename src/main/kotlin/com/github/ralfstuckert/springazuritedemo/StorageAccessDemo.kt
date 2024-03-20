package com.github.ralfstuckert.springazuritedemo

import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component

@Component
class StorageAccessDemo(
    val blobStorage: AzureBlobStorage,
    val tableStorage: AzureTableStorage
) : ApplicationRunner {

    private val logger = LoggerFactory.getLogger(StorageAccessDemo::class.java)


    override fun run(args: ApplicationArguments?) {
        blobStorage()
    }

    fun blobStorage() {
        val blobName = "testblob"
        logger.info("creating blob '$blobName'")
        blobStorage.updateBlob(blobName, "Here we go".toByteArray(Charsets.UTF_8))

        logger.info("reading blob '$blobName'")
        val content = blobStorage.downloadBlob(blobName).toString(Charsets.UTF_8)
        logger.info("content is: $content")
    }
}