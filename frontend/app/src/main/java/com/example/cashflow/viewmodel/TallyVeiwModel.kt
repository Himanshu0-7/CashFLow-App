// viewmodel/TallyViewModel.kt
package com.example.cashflow.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.example.cashflow.data.AppDatabase
import com.example.cashflow.data.TallySyncService
import com.example.cashflow.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class TallyViewModel(app: Application) : AndroidViewModel(app) {

    private val db = AppDatabase.get(app)
    private val companyDao = db.companyDao()
    private val ledgerDao = db.ledgerDao()
    private val entryDao = db.tallyEntryDao()

    val companies: StateFlow<List<Company>> =
        companyDao.getAll().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _activeCompany = MutableStateFlow<Company?>(null)
    val activeCompany: StateFlow<Company?> = _activeCompany

    val ledgers: StateFlow<List<Ledger>> = _activeCompany
        .flatMapLatest { c -> c?.let { ledgerDao.getByCompany(it.id) } ?: flowOf(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val entries: StateFlow<List<TallyEntry>> = _activeCompany
        .flatMapLatest { c -> c?.let { entryDao.getByCompany(it.id) } ?: flowOf(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {
        viewModelScope.launch { _activeCompany.value = companyDao.getActive() }
    }

    fun switchCompany(company: Company) {
        viewModelScope.launch {
            // deactivate all, activate selected
            companies.value.forEach { companyDao.update(it.copy(isActive = false)) }
            companyDao.update(company.copy(isActive = true))
            _activeCompany.value = company.copy(isActive = true)
        }
    }

    fun addCompany(name: String, tallyIp: String) {
        viewModelScope.launch {
            val c = Company(UUID.randomUUID().toString(), name, tallyIp)
            companyDao.insert(c)
            if (_activeCompany.value == null) switchCompany(c)
        }
    }

    fun addLedger(name: String, group: String) {
        val cid = _activeCompany.value?.id ?: return
        viewModelScope.launch {
            ledgerDao.insert(Ledger(UUID.randomUUID().toString(), cid, name, group))
        }
    }

    // Auto-detect voucher type based on groups
    fun detectVoucherType(debitLedger: Ledger, creditLedger: Ledger): String {
        val cashBankGroups = setOf("Cash", "Bank Accounts")
        return when {
            debitLedger.group in cashBankGroups && creditLedger.group in cashBankGroups -> "Contra"
            debitLedger.group in cashBankGroups -> "Payment"
            creditLedger.group in cashBankGroups -> "Receipt"
            else -> "Journal"
        }
    }

    fun addEntry(
        debitLedger: Ledger, creditLedger: Ledger,
        amount: Double, narration: String, date: Long
    ) {
        val company = _activeCompany.value ?: return
        val voucherType = detectVoucherType(debitLedger, creditLedger)
        viewModelScope.launch {
            val entry = TallyEntry(
                id = UUID.randomUUID().toString(),
                companyId = company.id,
                voucherType = voucherType,
                debitLedgerId = debitLedger.id,
                creditLedgerId = creditLedger.id,
                amount = amount,
                narration = narration,
                date = date,
                synced = false
            )
            entryDao.insert(entry)
            syncIfOnline(entry, debitLedger, creditLedger, company)
        }
    }

    private suspend fun syncIfOnline(
        entry: TallyEntry, debitLedger: Ledger,
        creditLedger: Ledger, company: Company
    ) {
        val reachable = TallySyncService.isReachable(company.tallyIp, company.tallyPort)
        if (reachable) {
            val ok = TallySyncService.pushEntry(entry, debitLedger, creditLedger, company)
            if (ok) entryDao.markSynced(entry.id)
        }
    }

    // Call on app resume to push unsynced entries
    fun syncPending() {
        val company = _activeCompany.value ?: return
        viewModelScope.launch {
            val unsynced = entryDao.getUnsynced()
            val allLedgers = ledgers.value
            unsynced.forEach { entry ->
                val dr = allLedgers.find { it.id == entry.debitLedgerId } ?: return@forEach
                val cr = allLedgers.find { it.id == entry.creditLedgerId } ?: return@forEach
                syncIfOnline(entry, dr, cr, company)
            }
        }
    }
}