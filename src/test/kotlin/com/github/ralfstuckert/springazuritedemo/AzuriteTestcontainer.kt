package com.github.ralfstuckert.springazuritedemo

import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.GenericContainer


class AzuriteContainer : GenericContainer<AzuriteContainer>("mcr.microsoft.com/azure-storage/azurite:3.29.0")

abstract class AzuriteTestcontainer()  {
    companion object {

        val container = AzuriteContainer().withExposedPorts(10000, 10001, 10002)

        // this is a public documented key for testing purposes with azurite, see https://learn.microsoft.com/de-de/azure/storage/common/storage-use-azurite?tabs=visual-studio%2Cblob-storage#connect-to-azurite-with-sdks-and-tools
        val accountKey = "Eby8vdM02xNOcqFlqUwJPLlmEtlCDXJ1OUzFT50uSRZ6IFsuFq2UVErCz4I6tq/K1SZFPTOtr/KBHBeksoGMGw=="
        val accountName = "devstoreaccount1"

        val blobUrl by lazy {
            "http://${container.host}:${container.getMappedPort(10000)}"
        }
        val queueUrl by lazy {
            "http://${container.host}:${container.getMappedPort(10001)}"
        }
        val tableUrl by lazy {
            "http://${container.host}:${container.getMappedPort(10002)}"
        }
        val blobConnectionString by lazy { "DefaultEndpointsProtocol=http;AccountName=${accountName};AccountKey=${accountKey};BlobEndpoint=${blobUrl}/devstoreaccount1;" }
        val queueConnectionString by lazy { "DefaultEndpointsProtocol=http;AccountName=${accountName};AccountKey=${accountKey};QueueEndpoint=${queueUrl}/devstoreaccount1;" }
        val tableConnectionString by lazy { "DefaultEndpointsProtocol=http;AccountName=${accountName};AccountKey=${accountKey};TableEndpoint=${tableUrl}/devstoreaccount1;" }


        init {
            container.start()
        }

        @JvmStatic
        @DynamicPropertySource
        fun registerAzuriteProperties(registry: DynamicPropertyRegistry) {
            registry.add("demo.storage.table.endpoint") { "${tableUrl}/devstoreaccount1" }

            // overwrite endpoints using the testcontainers ports
            registry.add("spring.cloud.azure.storage.blob.endpoint") { "${blobUrl}/devstoreaccount1" }
            registry.add("spring.cloud.azure.storage.queue.endpoint") { "${queueUrl}/devstoreaccount1" }
        }
    }

}
