package com.example.cashflow.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.cashflow.data.AppDatabase
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
        companyDao.getAll()
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _activeCompany = MutableStateFlow<Company?>(null)
    val activeCompany: StateFlow<Company?> = _activeCompany

    val ledgers: StateFlow<List<Ledger>> =
        _activeCompany
            .flatMapLatest { company ->
                company?.let { ledgerDao.getByCompany(it.id) }
                    ?: flowOf(emptyList())
            }
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val entries: StateFlow<List<TallyEntry>> =
        _activeCompany
            .flatMapLatest { company ->
                company?.let { entryDao.getByCompany(it.id) }
                    ?: flowOf(emptyList())
            }
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {
        viewModelScope.launch {
            _activeCompany.value = companyDao.getActive()
        }
    }

    fun switchCompany(company: Company) {
        viewModelScope.launch {
            companies.value.forEach {
                companyDao.update(it.copy(isActive = false))
            }

            val active = company.copy(isActive = true)
            companyDao.update(active)
            _activeCompany.value = active
        }
    }

    fun addCompany(name: String, tallyIp: String) {
        viewModelScope.launch {
            val company = Company(
                id = UUID.randomUUID().toString(),
                name = name,
                tallyIp = tallyIp
            )

            companyDao.insert(company)

            if (_activeCompany.value == null) {
                switchCompany(company)
            }
        }
    }

    fun addLedger(name: String, group: String) {
        val companyId = _activeCompany.value?.id ?: return

        viewModelScope.launch {
            ledgerDao.insert(
                Ledger(
                    id = UUID.randomUUID().toString(),
                    companyId = companyId,
                    name = name,
                    group = group
                )
            )
        }
    }

    fun detectVoucherType(
        debitLedger: Ledger,
        creditLedger: Ledger
    ): String {

        val cashBankGroups = setOf(
            "Cash",
            "Bank Accounts"
        )

        return when {
            debitLedger.group in cashBankGroups &&
                    creditLedger.group in cashBankGroups -> "Contra"

            debitLedger.group in cashBankGroups -> "Payment"

            creditLedger.group in cashBankGroups -> "Receipt"

            else -> "Journal"
        }
    }

    fun addEntry(
        debitLedger: Ledger,
        creditLedger: Ledger,
        amount: Double,
        narration: String,
        date: Long
    ) {

        val company = _activeCompany.value ?: return

        viewModelScope.launch {

            val entry = TallyEntry(
                id = UUID.randomUUID().toString(),
                companyId = company.id,
                voucherType = detectVoucherType(
                    debitLedger,
                    creditLedger
                ),
                debitLedgerId = debitLedger.id,
                creditLedgerId = creditLedger.id,
                amount = amount,
                narration = narration,
                date = date,
                synced = false
            )

            entryDao.insert(entry)
        }
    }
}