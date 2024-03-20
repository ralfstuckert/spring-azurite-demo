package com.github.ralfstuckert.springazuritedemo

import com.azure.data.tables.TableServiceClientBuilder
import com.azure.data.tables.models.TableEntity
import com.azure.data.tables.models.TableServiceException
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class AzureTableStorageTest : AzuriteTestcontainer() {

    val tableName = "test"
    val tableClient = with (TableServiceClientBuilder().connectionString(tableConnectionString).buildClient()) {
        createTableIfNotExists(tableName) ?: getTableClient(tableName)
    }
    val tableStorage = AzureTableStorage(tableClient)
    val testPartition = "testPartition"


    @BeforeEach
    fun setup() {
        tableClient.listEntities().forEach { entity ->
            tableClient.deleteEntity(entity)
        }
    }


    @Test
    fun getEntity() {
        tableStorage.getEntity(testPartition, "hihi") shouldBe null

        tableClient.upsertEntity(TableEntity(testPartition, "hihi").apply {
            addProperty("content", "1234")
        })

        val entity = tableStorage.getEntity(testPartition, "hihi")
        checkNotNull(entity)
        entity.properties["content"] shouldBe "1234"
    }

    @Test
    fun `insert entity`() {
        assertThrows<TableServiceException> {
            tableClient.getEntity(testPartition, "hihi")
        }.response.statusCode shouldBe 404

        tableStorage.upsertEntity(testPartition, "hihi", mapOf("content" to "1234"))

        val entity = tableClient.getEntity(testPartition, "hihi")
        checkNotNull(entity)
        entity.properties["content"] shouldBe "1234"
    }

    @Test
    fun `update entity`() {
        tableClient.upsertEntity(TableEntity(testPartition, "hihi").setProperties(mapOf("content" to "1234")))

        val entity = tableStorage.getEntity(testPartition, "hihi")
        checkNotNull(entity)
        entity.properties["content"] shouldBe "1234"


        tableStorage.upsertEntity(testPartition, "hihi", mapOf("content" to "5555"))

        val updated = tableClient.getEntity(testPartition, "hihi")
        checkNotNull(updated)
        updated.properties["content"] shouldBe "5555"
    }

    @Test
    fun listEntities() {
        tableStorage.listEntities().toList() shouldBe emptyList()

        tableStorage.upsertEntity(testPartition, "a1", mapOf("content" to "a1"))
        tableStorage.upsertEntity(testPartition, "a2", mapOf("content" to "a2"))
        tableStorage.upsertEntity("partion1", "b1", mapOf("content" to "b1"))
        tableStorage.upsertEntity("partion1", "b2", mapOf("content" to "b2"))

        tableStorage.listEntities().map { it.rowKey }.shouldContainAll("a1","a2", "b1", "b2")

        tableStorage.listEntities(testPartition).map { it.rowKey } shouldBe listOf("a1","a2")

        tableStorage.listEntities("partion1").map { it.rowKey } shouldBe listOf("b1", "b2")
    }


}