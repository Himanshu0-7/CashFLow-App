package com.example.cashflow.ui.books

import androidx.activity.ComponentActivity
import androidx.collection.intSetOf
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.cashflow.model.Book
import com.example.cashflow.ui.category.CategoryScreen
import com.example.cashflow.viewmodel.BookViewModel
import kotlinx.coroutines.launch

@Composable
fun BooksScreen(type: String, navController: NavController) {
    val viewModel: BookViewModel = viewModel(
        viewModelStoreOwner = LocalContext.current as ComponentActivity
    )
    val state = viewModel.uiState.value
    val books = state.books
    val filteredBooks = books.filter { it.type == type }
    var dragDistance = 0f
    val scope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) }
    Box(Modifier.fillMaxSize()) {

        // Previous screen
        CategoryScreen(navController)
        val hasEntered = remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            hasEntered.value = true
        }
        // Current screen
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset { IntOffset(offsetX.value.toInt(), 0) }
                .background(Color(0xFF080A1B))
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { _, dragAmount ->
                            val newOffset = (offsetX.value + dragAmount)
                                .coerceAtLeast(0f)

                            scope.launch {
                                offsetX.snapTo(newOffset)
                            }
                        },
                        onDragEnd = {
                            scope.launch {
                                if (offsetX.value > 300f) {
                                    // Slide fully off screen first
                                    offsetX.animateTo(
                                        targetValue = size.width.toFloat(),
                                        animationSpec = tween(200)
                                    )
                                    // Small buffer so the frame is rendered before Navigation acts
                                    kotlinx.coroutines.delay(16)
                                    navController.popBackStack()
                                } else {
                                    offsetX.animateTo(0f, tween(250))
                                }
                            }
                        }
                    )
                }
        ) {

            BooksScreenContent(navController, filteredBooks, type, viewModel)

        }

    }
}

@Composable
fun BooksScreenContent(
    navController: NavController,
    filteredBooks: List<Book>,
    type: String,
    viewModel: BookViewModel
) {
    if (filteredBooks.isEmpty()) {

        Text(
            text = "No books created",
            color = Color.Gray,
//            modifier = Modifier.align(Alignment.Center)
        )

    } else {

        Column(
            modifier = Modifier.fillMaxSize()
        ) {

            Text(
                text = "$type Books",
                color = Color.White,
                fontSize = 20.sp,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(16.dp)
            )

            LazyColumn {
                items(filteredBooks) { book ->
                    BookCard(navController, book, viewModel)
                }
            }
        }
    }
}

@Composable
fun BookCard(navController: NavController, book: Book, viewModel: BookViewModel) {
    val (totalExpense, second, third) = viewModel.getSummary(book.id)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                navController.navigate("bookEntry/${book.id}")
            }
            .padding(12.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF0f1230)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = book.name,
                color = Color.White,
                fontSize = 20.sp
            )
            Text(
                text = "₹ $totalExpense",
                color = Color.White
            )
        }

    }
}
