package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
  @Query("SELECT * FROM transactions ORDER BY dateEpochDay DESC, createdAtMillis DESC")
  fun getAllTransactions(): Flow<List<TransactionEntity>>

  @Query("SELECT * FROM transactions WHERE dateEpochDay BETWEEN :startDay AND :endDay ORDER BY dateEpochDay DESC, createdAtMillis DESC")
  fun getTransactionsBetween(startDay: Long, endDay: Long): Flow<List<TransactionEntity>>

  @Query("SELECT COUNT(*) FROM transactions")
  suspend fun getCount(): Int

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(transaction: TransactionEntity): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAll(transactions: List<TransactionEntity>)

  @Update
  suspend fun update(transaction: TransactionEntity)

  @Delete
  suspend fun delete(transaction: TransactionEntity)

  @Query("DELETE FROM transactions WHERE id = :id")
  suspend fun deleteById(id: Long)
}
