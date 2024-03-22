package com.github.ralfstuckert.springazuritedemo

import com.azure.core.util.BinaryData
import com.azure.storage.blob.BlobClient
import com.azure.storage.blob.BlobContainerClient
import com.azure.storage.blob.BlobServiceClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream


@Service
class AzureBlobStorage(val blobServiceClient: BlobServiceClient,
    @Value("\${demo.storage.blob.container}") val containerName: String
) {

    private val blobContainerClient: BlobContainerClient by lazy {
        blobServiceClient.createBlobContainerIfNotExists(containerName) ?: blobServiceClient.getBlobContainerClient(
            containerName
        )
    }


    private fun getBlobClient(blobName: String): BlobClient {
        return blobContainerClient.getBlobClient(blobName);
    }


    fun listBlobNames():List<String> =
        blobContainerClient.listBlobs().map { it.name }

    fun downloadBlob(blobName: String): ByteArray =
            getBlobClient(blobName).downloadContent().toBytes()

    fun updateBlob(blobName: String, byteArray: ByteArray) =
        getBlobClient(blobName).upload(BinaryData.fromBytes(byteArray), true)

    fun deleteBlob(blobName: String) =
        getBlobClient(blobName).delete()
}


