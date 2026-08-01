package com.michaldrabik.data_local.sources

import com.michaldrabik.data_local.database.model.FloppySyncQueue

interface FloppySyncQueueLocalDataSource {

  suspend fun insert(items: List<FloppySyncQueue>): List<Long>

  suspend fun getAll(): List<FloppySyncQueue>

  suspend fun delete(items: List<FloppySyncQueue>)

  suspend fun deleteAll()
}
