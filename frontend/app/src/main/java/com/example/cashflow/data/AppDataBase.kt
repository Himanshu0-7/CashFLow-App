// data/AppDatabase.kt
package com.example.cashflow.data

import android.content.Context
import androidx.room.*
import com.example.cashflow.data.dao.*
import com.example.cashflow.model.*

@Database(entities = [Company::class, Ledger::class, TallyEntry::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun companyDao(): CompanyDao
    abstract fun ledgerDao(): LedgerDao
    abstract fun tallyEntryDao(): TallyEntryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        fun get(context: Context) = INSTANCE ?: synchronized(this) {
            Room.databaseBuilder(context, AppDatabase::class.java, "cashflow.db")
                .build().also { INSTANCE = it }
        }
    }
}