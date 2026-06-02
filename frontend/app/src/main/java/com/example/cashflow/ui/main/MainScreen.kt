package com.example.cashflow.ui.main


import androidx.activity.ComponentActivity
import androidx.compose.animation.core.Animatable
import com.example.cashflow.R
import androidx.compose.runtime.Composable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import androidx.compose.material3.Button
import androidx.compose.material3.RadioButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.cashflow.ui.booksEntry.BooksEntry
import com.example.cashflow.ui.category.CategoryScreen
import com.example.cashflow.ui.home.HomeScreen
import com.example.cashflow.viewmodel.BookViewModel


@Composable
fun MainScreen(
    navController: NavController, viewModel: BookViewModel = viewModel(
        viewModelStoreOwner = LocalContext.current as ComponentActivity
    )
) {
    Box(
        modifier = Modifier
            .background(Color.Black)
            .fillMaxSize()
    ) {

        val pageState = rememberPagerState(pageCount = { 2 })

        val config = LocalConfiguration.current
        val density = LocalDensity.current

        val scope = rememberCoroutineScope()
        val screenHeightPx = with(density) {
            config.screenHeightDp.dp.toPx()
        }
        val sheetHeightPx = with(density) {
            600.dp.toPx()
        }
        val closePos = screenHeightPx
        val openPos = screenHeightPx - sheetHeightPx

        var animoffset = remember { Animatable(closePos) }
        var balanceInput by remember { mutableStateOf("") }
        val state = viewModel.uiState.value
        HorizontalPager(
            state = pageState,
            pageSpacing = 16.dp,
            userScrollEnabled = animoffset.value > (screenHeightPx - 100f),
            modifier = Modifier
                .fillMaxWidth()
                .nestedScroll(rememberNestedScrollInteropConnection()),
        ) { page ->
            when (page) {
                0 -> HomeScreen()
                1 -> CategoryScreen(navController)

            }
        }


        val dragModifier = Modifier.pointerInput(Unit) {
            detectDragGestures(
                onDrag = { change, dragAmount ->
                    change.consume()
                    val newValue = animoffset.value + dragAmount.y

                    scope.launch {
                        animoffset.snapTo(
                            newValue.coerceIn(openPos, closePos)
                        )
                    }
                },
                onDragEnd = {
                    scope.launch {
                        val progress =
                            (closePos - animoffset.value) / sheetHeightPx

                        if (progress >= 0.4f) {
                            animoffset.animateTo(openPos)
                        } else {
                            animoffset.animateTo(closePos)
                        }
                    }
                }

            )
        }



        Box(
            modifier = Modifier
                .offset { IntOffset(0, animoffset.value.toInt()) }
                .fillMaxWidth()
                .then(dragModifier)
                .height(600.dp)
                .background(
                    Color(0xFF0f1230),
                    RoundedCornerShape(
                        topEnd = 24.dp,
                        topStart = 24.dp
                    )
                ),
            contentAlignment = Alignment.TopCenter

        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Drag handle
                Box(
                    modifier = Modifier
                        .padding(top = 8.dp, bottom = 12.dp)
                        .width(40.dp)
                        .height(4.dp)
                        .background(Color.Gray, RoundedCornerShape(50))
                )
                Spacer(Modifier.height(12.dp))
                Text("CREATE BOOK", color = Color.White, fontSize = 24.sp)
                Spacer(Modifier.height(24.dp))

                Text(
                    "Book Name",
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Start),
                )
                Spacer(Modifier.height(6.dp))

                TextField(
                    value = state.bookName,
                    onValueChange = { viewModel.updateBookName(it) },
                    placeholder = { Text("Enter Book Name") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF2B3C6A),
                        unfocusedContainerColor = Color(0xFF2B3C6A),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )

                Spacer(Modifier.height(20.dp))

                Text("Type :", color = Color.White, modifier = Modifier.align(Alignment.Start))
                Spacer(Modifier.height(10.dp))
                BookOption(
                    "Cash Book",
                    selected = state.selectedOption == "Cash",
                    onSelect = { viewModel.selectOption("Cash") })
                Spacer(Modifier.height(8.dp))
                BookOption(
                    "Travel Book",
                    selected = state.selectedOption == "Travel",
                    onSelect = { viewModel.selectOption("Travel") }
                )
                Text(
                    text = if (state.selectedOption == "Travel") "Budget" else "Opening Balance",
                    color = Color.Gray
                )

                TextField(
                    value = balanceInput,
                    onValueChange = { balanceInput = it },
                    placeholder = {
                        Text(
                            if (state.selectedOption == "Travel")
                                "Enter Budget"
                            else
                                "Enter Opening Balance"
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
//                Spacer(Modifier.weight(1f))
                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = {
                        val balance = balanceInput.toDoubleOrNull() ?: 0.0

                        viewModel.saveBook(balance)

                        scope.launch {
                            animoffset.animateTo(closePos)
                            pageState.animateScrollToPage(1)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Save")
                }
                Spacer(Modifier.height(16.dp))
            }

        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Color.Transparent
                )
                .then(dragModifier)
                .align(Alignment.BottomCenter),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = {
                    scope.launch {
                        pageState.animateScrollToPage(0)
                    }
                },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    painter = painterResource(
                        id =
                            if (pageState.currentPage == 0) {
                                R.drawable.active_home
                            } else {
                                R.drawable.homeicon
                            }
                    ),
                    contentDescription = "Home",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            IconButton(
                onClick = {
                    scope.launch {
                        animoffset.animateTo(openPos)
                    }
                },
                modifier = Modifier
                    .size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Cash",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            IconButton(
                onClick = {
                    scope.launch {
                        pageState.animateScrollToPage(1)
                    }
                },
                modifier = Modifier
                    .size(48.dp)
            ) {
                Icon(
                    painter = painterResource(
                        if (pageState.currentPage == 1) {
                            R.drawable.active_collection
                        } else {
                            R.drawable.collection
                        }
                    ),
                    contentDescription = "collect",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }


        }

    }
}

@Composable
fun BookOption(text: String, selected: Boolean, onSelect: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(44.dp)
            .background(
                if (selected) Color(0xFF2B3C6A) else Color.Transparent,
                RoundedCornerShape(10.dp)
            )
            .clickable { onSelect() }
            .border(1.dp, Color.Gray, RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = null
        )
        Spacer(Modifier.width(8.dp))

        Text(text, color = Color.White)
    }
}



