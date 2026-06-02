// model/TallyModels.kt
package com.example.cashflow.model

import androidx.room.*

@Entity(tableName = "companies")
data class Company(
    @PrimaryKey val id: String,
    val name: String,
    val tallyIp: String = "localhost",   // Tally Gateway IP
    val tallyPort: Int = 9000,
    val isActive: Boolean = false
)

@Entity(
    tableName = "ledgers",
    foreignKeys = [ForeignKey(
        entity = Company::class,
        parentColumns = ["id"],
        childColumns = ["companyId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("companyId")]
)
data class Ledger(
    @PrimaryKey val id: String,
    val companyId: String,
    val name: String,
    val group: String       // "Cash", "Bank Accounts", "Sundry Debtors", etc.
)

@Entity(
    tableName = "tally_entries",
    foreignKeys = [ForeignKey(
        entity = Company::class,
        parentColumns = ["id"],
        childColumns = ["companyId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("companyId")]
)
data class TallyEntry(
    @PrimaryKey val id: String,
    val companyId: String,
    val voucherType: String,   // "Payment", "Receipt", "Contra", "Journal"
    val debitLedgerId: String,
    val creditLedgerId: String,
    val amount: Double,
    val narration: String,
    val date: Long,
    val synced: Boolean = false
)