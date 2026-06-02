package com.example.cashflow.viewmodel

import com.example.cashflow.model.Book

data class BookUiState(
    val bookName: String = "",
    val selectedOption: String = "Travel",
    val books: List<Book> = emptyList()
)