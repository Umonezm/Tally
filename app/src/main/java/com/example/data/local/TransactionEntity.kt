package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.PaymentMethod
import com.example.data.model.TransactionCategory
import com.example.data.model.TransactionItem
import com.example.data.model.TransactionType

@Entity(tableName = "transactions")
data class TransactionEntity(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val amount: Double,
  val type: String, // EXPENSE or INCOME
  val category: String,
  val note: String,
  val dateEpochDay: Long,
  val paymentMethod: String,
  val isRecurring: Boolean = false,
  val createdAtMillis: Long = System.currentTimeMillis()
) {
  fun toDomain(): TransactionItem {
    return TransactionItem(
      id = id,
      amount = amount,
      type = if (type.equals("INCOME", ignoreCase = true)) TransactionType.INCOME else TransactionType.EXPENSE,
      category = TransactionCategory.fromName(category),
      note = note,
      dateEpochDay = dateEpochDay,
      paymentMethod = PaymentMethod.fromName(paymentMethod),
      isRecurring = isRecurring
    )
  }

  companion object {
    fun fromDomain(domain: TransactionItem): TransactionEntity {
      return TransactionEntity(
        id = domain.id,
        amount = domain.amount,
        type = domain.type.name,
        category = domain.category.name,
        note = domain.note,
        dateEpochDay = domain.dateEpochDay,
        paymentMethod = domain.paymentMethod.name,
        isRecurring = domain.isRecurring
      )
    }
  }
}
