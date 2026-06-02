package com.example.cashflow.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.cashflow.model.Book
import com.example.cashflow.model.Transaction
import com.example.cashflow.viewmodel.BookUiState
import org.json.JSONObject
import java.net.URL

class BookViewModel : ViewModel() {

    var uiState = mutableStateOf(BookUiState())
        private set

    fun updateBookName(name: String) {
        uiState.value = uiState.value.copy(bookName = name)
    }

    fun selectOption(option: String) {
        uiState.value = uiState.value.copy(selectedOption = option)
    }

    fun saveBook(balance: Double) {
        val currentState = uiState.value
        if (currentState.bookName.isBlank()) return

        val newBook = Book(
            id = System.currentTimeMillis().toString(),
            name = currentState.bookName,
            type = currentState.selectedOption,
            balance = balance
        )

        uiState.value = currentState.copy(
            books = currentState.books + newBook,
            bookName = "",
            selectedOption = "Travel"
        )
    }

    private val _transactions = mutableStateListOf<Transaction>()
    val transactions: List<Transaction> = _transactions

    fun addTransaction(transaction: Transaction) {
        _transactions.add(transaction)
    }

    fun getTransactionsByBook(bookId: String): List<Transaction> {
        return _transactions.filter { it.bookId == bookId }
    }

    fun getSummary(bookId: String): Triple<Double, Double, Double> {

        val book = getBookById(bookId) ?: return Triple(0.0, 0.0, 0.0)
        val list = _transactions.filter { it.bookId == bookId }

        val totalExpense = list
            .filter { it.type == "expense" }
            .sumOf { it.amount }

        val totalIncome = list
            .filter { it.type == "income" }
            .sumOf { it.amount }

        return if (book.type == "Travel") {

            // 🔹 Travel Book
            val budget = book.balance
            val remaining = budget - totalExpense

            Triple(totalExpense, budget, remaining)

        } else {

            // 🔹 Cash Book
            val openingBalance = book.balance
            val currentBalance = openingBalance + totalIncome - totalExpense

            Triple(totalExpense, openingBalance, currentBalance)
        }
    }

    fun getBookById(bookId: String): Book? {
        return uiState.value.books.firstOrNull { it.id == bookId }
    }

    suspend fun getLocationSuggestions(query: String): List<String> {
        return try {
            val url =
                "https://nominatim.openstreetmap.org/search?q=$query&format=json&addressdetails=1"

            val response = URL(url).readText()

            val jsonArray = org.json.JSONArray(response)

            val result = mutableListOf<String>()

            for (i in 0 until jsonArray.length()) {
                val item = jsonArray.getJSONObject(i)
                result.add(item.getString("display_name"))
            }

            result
        } catch (e: Exception) {
            emptyList()
        }
    }
}
