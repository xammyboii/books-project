@file:OptIn(ExperimentalGlideComposeApi::class)

package com.example.books

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.example.books.database.BookDatabase
import com.example.books.database.models.Books


private lateinit var db: BookDatabase
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistDetailsScreen(
    navController: NavController,
    innerPadding: PaddingValues,
    param: Int
){
    //Database variables
    val context = LocalContext.current
    db = BookDatabase.getDatabase(context)
    val booksDao = db.BooksDao()

    Scaffold(modifier = Modifier.padding(innerPadding),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "${booksDao.getDetailsOfPlaylist(param).playlistName}",
                        maxLines = 1
                    )
                },
                actions = {
                    IconButton(onClick = { navController.navigate("0") }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Localized description"
                        )
                    }
                }
            )
        }
    )
    {innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center

        ){
            Text(
                text = "Testing ${param}",
                color = Color.Black,
                fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                fontWeight = FontWeight.Bold
            )

            Column(modifier = Modifier
                .fillMaxSize()

            ) {
                //Retrieve list of books
                val listOfBooks = booksDao.getAllBooksInPlaylist(id = param)
                LazyColumn(modifier = Modifier
                ) {

                    items(listOfBooks) {item ->

                        booksCards(item)
                        Divider(modifier = Modifier)
                    }

                }
            }

        }
    }

}

//Composable that will display the each book
@Composable
fun booksCards(
    book: Books
) {
    //Keeps track of dialog
    val isDialogOpen = remember { mutableStateOf(false) }

    //Determines state of the menu
    var expanded by remember {
        mutableStateOf(false)
    }
    Box(modifier = Modifier, contentAlignment = Alignment.Center)
    {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .padding(15.dp),
            shape = RoundedCornerShape(15.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary)
        ) {
            Row {
                //Contains image
                GlideImage(
                    modifier = Modifier
                        .width(150.dp)
                        .height(225.dp),
                    model = "https://covers.openlibrary.org/b/ISBN/${book.isbn_10}-M.jpg",
                    contentDescription = "Book Cover",
                    contentScale = ContentScale.FillBounds
                )

                Column(
                    modifier = Modifier
                        .padding(start = 5.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(text = book.title, modifier = Modifier
                            .fillMaxWidth(0.80F))

                        //Icon Button
                        IconButton( { expanded = true }, modifier = Modifier
                                .padding(0.dp),) {
                            Icon(
                                imageVector = Icons.Filled.MoreVert,
                                contentDescription = "Localized description"
                            )
                            //List of options
                            val listItems = arrayOf("Add to Playlist", "Delete")
                            val contextForToast = LocalContext.current.applicationContext
                            //Sets menu into column
                            Column(modifier = Modifier) {
                                DropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false }) {
                                    // adding items
                                    listItems.forEachIndexed { itemIndex, itemValue ->
                                        DropdownMenuItem(
                                            onClick = {
                                                //Identifies by index of item which to run
                                                if(itemIndex == 0 )
                                                {
                                                    //Opens Dialog box
                                                    isDialogOpen.value = true;
                                                }
                                                else
                                                {
                                                    Toast.makeText(contextForToast, "Delete Book", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                        ) {
                                            Text(text = itemValue)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Text(text = "Published: ${book.publish_date}")
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Text(text = "ISBN 10: ${book.isbn_10}\nISBN 13: ${book.isbn_13}")
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(), verticalAlignment = Alignment.Bottom
                    ) {
                        val string = book.subjects
                        val list =
                            string.split("[\\[\\],\"]+".toRegex()).filter { it.isNotBlank() }

                        var tags = "Tags:\n"
                        list.forEach { item ->
                            tags += "${item} "
                        }
                        Text(fontSize = 11.sp, text = tags)

                    }


                }
            }
        }
    }

    //Dialog box
    if(isDialogOpen.value)
    {
        //Dialog box variables
        var newPlaylistName by rememberSaveable { mutableStateOf("") }

        //Needs text validation

        fun newPlaylistCreated(name: String)
        {
        }

        Dialog(onDismissRequest = { isDialogOpen }) {
            Card(
                modifier = Modifier
                    .wrapContentWidth(),
                shape = RoundedCornerShape(15.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary)
            ){
                Box(modifier = Modifier
                    .height(150.dp)
                    .width(200.dp)

                ) {
                    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        Row {
                            Text(
                                text = "Create New Playlist",
                                modifier = Modifier
                                    .padding(top = 5.dp),
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        Row{

                            OutlinedTextField(
                                modifier = Modifier
                                    .width(125.dp)
                                    .padding(top = 10.dp),
                                value = newPlaylistName,
                                onValueChange = { newPlaylistName = it },
                                singleLine = true,
                            )
                        }
                        Row{
                            TextButton(onClick = { isDialogOpen.value = false }) {
                                Text(text = "Cancel")
                            }
                            TextButton(onClick = { newPlaylistCreated(newPlaylistName) }) {
                                Text(text = "Create")
                            }
                        }
                    }
                }
            }
        }
    }
}