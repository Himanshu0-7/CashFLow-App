package com.example.cashflow.model

import androidx.compose.ui.graphics.vector.ImageVector

data class Book(
    val id: String,
    val name: String,
    val type: String,
    val balance: Double
)

data class Category(
    val name: String,
    val icon: ImageVector
)

data class Transaction(
    val id: String,
    val title: String,
    val amount: Double,
    val category: String,
    val location: String,
    val date: Long,
    val type: String,
    val bookId: String
)