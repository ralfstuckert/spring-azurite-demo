package com.github.ralfstuckert.springazuritedemo

import com.azure.storage.blob.BlobServiceClientBuilder
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class AzureBlobStorageTest : AzuriteTestcontainer() {

    val container = "test"
    val client = BlobServiceClientBuilder()
        .connectionString(blobConnectionString)
        .buildClient()
    val blobContainerClient = client.getBlobContainerClient(container)
    val storageService = AzureBlobStorage(client, container)

    @BeforeEach
    fun setup() {
        if (blobContainerClient.exists()) {
            blobContainerClient.listBlobs().map { it.name }.forEach {
                blobContainerClient.getBlobClient(it).delete()
            }
        }
    }

    @Test
    fun listBlobNames() {
        storageService.listBlobNames() shouldBe emptyList()

        blobContainerClient.getBlobClient("a").upload("aaa".byteInputStream())
        blobContainerClient.getBlobClient("b").upload("bbb".byteInputStream())
        blobContainerClient.getBlobClient("c").upload("ccc".byteInputStream())

        storageService.listBlobNames() shouldBe listOf("a", "b", "c")
    }

    @Test
    fun updateBlob() {
        val contentA = "aaa".toByteArray()

        storageService.updateBlob("a", contentA)

        blobContainerClient.getBlobClient("a").downloadContent().toBytes() shouldBe contentA
    }


    @Test
    fun downloadBlob() {
        val contentA = "aaa"
        val contentB = "bbb"
        blobContainerClient.getBlobClient("a").upload(contentA.byteInputStream())
        blobContainerClient.getBlobClient("b").upload(contentB.byteInputStream())

        storageService.downloadBlob("a") shouldBe contentA.toByteArray()
        storageService.downloadBlob("b") shouldBe contentB.toByteArray()
    }

}