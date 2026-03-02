package com.example.cashflow.ui

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.cashflow.model.Book

class BookViewModel : ViewModel() {

    var bookName = mutableStateOf("")
        private set
    var books = mutableStateOf(listOf<Book>())
        private set

    var selectedOption = mutableStateOf("Travel")
        private set

    fun updateBookName(name: String) {
        bookName.value = name
    }

    fun selectOption(option: String) {
        selectedOption.value = option
    }


    fun saveBook() {
        if (bookName.value.isBlank()) return

        val newBook = Book(
            id = System.currentTimeMillis().toString(),
            name = bookName.value,
            type = selectedOption.value
        )

        books.value = books.value + newBook

        // reset form
        bookName.value = ""
        selectedOption.value = "Travel"
    }
}