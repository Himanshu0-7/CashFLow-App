// data/dao/TallyDao.kt
package com.example.cashflow.data.dao

import androidx.room.*
import com.example.cashflow.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CompanyDao {
    @Query("SELECT * FROM companies")
    fun getAll(): Flow<List<Company>>
    @Query("SELECT * FROM companies WHERE isActive = 1 LIMIT 1")
    suspend fun getActive(): Company?
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(c: Company)
    @Update
    suspend fun update(c: Company)
    @Delete
    suspend fun delete(c: Company)
}

@Dao
interface LedgerDao {
    @Query("SELECT * FROM ledgers WHERE companyId = :cid")
    fun getByCompany(cid: String): Flow<List<Ledger>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(l: Ledger)
    @Delete
    suspend fun delete(l: Ledger)
}

@Dao
interface TallyEntryDao {
    @Query("SELECT * FROM tally_entries WHERE companyId = :cid ORDER BY date DESC")
    fun getByCompany(cid: String): Flow<List<TallyEntry>>

    @Query("SELECT * FROM tally_entries WHERE synced = 0")
    suspend fun getUnsynced(): List<TallyEntry>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(e: TallyEntry)
    @Query("UPDATE tally_entries SET synced = 1 WHERE id = :id")
    suspend fun markSynced(id: String)
}