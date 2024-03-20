package com.github.ralfstuckert.springazuritedemo

import com.azure.core.http.rest.PagedIterable
import com.azure.data.tables.TableClient
import com.azure.data.tables.TableServiceClient
import com.azure.data.tables.TableServiceClientBuilder
import com.azure.data.tables.models.ListEntitiesOptions
import com.azure.data.tables.models.TableEntity
import com.azure.data.tables.models.TableServiceException
import com.azure.identity.DefaultAzureCredentialBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

typealias PartitionKey = String
typealias TableRow = Map<String, Any>

@Service
class AzureTableStorage(val tableClient: TableClient) {

    fun listEntities(partitionKey: PartitionKey? = null): PagedIterable<TableEntity> =
        if (partitionKey != null)
            ListEntitiesOptions().setFilter("PartitionKey eq '${partitionKey}'").let {
                tableClient.listEntities(it, null, null)
            }
        else
            tableClient.listEntities()

    fun getEntity(partitionKey: PartitionKey, rowKey: String): TableEntity? =
        try {
            tableClient.getEntity(partitionKey, rowKey)
        } catch (e: TableServiceException) {
            if (e.response.statusCode == 404)
                null
            else throw e
        }

    fun upsertEntity(partitionKey: PartitionKey, rowKey: String, content: TableRow) =
        tableClient.upsertEntity(TableEntity(partitionKey, rowKey).setProperties(content))
}

@Configuration
class AzureTableConfiguration {

    @Bean()
    @Profile("!azurite & !playtika")
    fun azureTableClient(
        @Value("\${demo.storage.table.endpoint}") tableEndpoint: String,
        @Value("\${demo.storage.table.tableName}") tableName: String
    ): TableClient {
        val defaultCredential = DefaultAzureCredentialBuilder().build()
        return TableServiceClientBuilder()
            .endpoint(tableEndpoint)
            .credential(defaultCredential)
            .buildClient()
            .createTableClient(tableName)
    }

    @Bean()
    @Profile("azurite | playtika")
    fun azuriteTableClient(
        @Value("\${demo.storage.table.endpoint}") tableEndpoint: String,
        @Value("\${demo.storage.table.tableName}") tableName: String
    ): TableClient {
        return TableServiceClientBuilder()
            // this is a public documented key for testing purposes with azurite, see https://learn.microsoft.com/de-de/azure/storage/common/storage-use-azurite?tabs=visual-studio%2Cblob-storage#connect-to-azurite-with-sdks-and-tools
            .connectionString("DefaultEndpointsProtocol=http;AccountName=devstoreaccount1;AccountKey=Eby8vdM02xNOcqFlqUwJPLlmEtlCDXJ1OUzFT50uSRZ6IFsuFq2UVErCz4I6tq/K1SZFPTOtr/KBHBeksoGMGw==;TableEndpoint=${tableEndpoint};")
            .buildClient()
            .createTableClient(tableName)
    }

    fun TableServiceClient.createTableClient(tableName: String): TableClient =
        // createTableIfNotExists() returns null if table already exists :-(
        createTableIfNotExists(tableName) ?: getTableClient(tableName)

}

