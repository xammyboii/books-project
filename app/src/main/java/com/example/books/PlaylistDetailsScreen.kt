@file:OptIn(ExperimentalGlideComposeApi::class)

package com.example.books

import android.annotation.SuppressLint
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.example.books.database.BookDatabase
import com.example.books.database.models.Books
import com.example.books.database.models.Playlists_Books
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.journeyapps.barcodescanner.BarcodeEncoder


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
                            contentDescription = "Back to Playlists"
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
            Column(modifier = Modifier
                .fillMaxSize()

            ) {
                //Retrieve list of books
                val listOfBooks = booksDao.getAllBooksInPlaylist(id = param)
                Log.d("playlist_id","Value: ${listOfBooks}")
                LazyColumn(modifier = Modifier
                ) {

                    items(listOfBooks) {item ->

                        booksCards(item, param, navController)
                        Divider(modifier = Modifier)
                    }

                }
            }

        }
    }

}

//Composable that will display the each book
@OptIn(ExperimentalMaterial3Api::class, ExperimentalGlideComposeApi::class)
@Composable
fun booksCards(
    book: Books,
    playList_id: Int,
    navController: NavController
) {
    val context = LocalContext.current
    db = BookDatabase.getDatabase(context)
    val booksDao = db.BooksDao()
    //Keeps track of dialog
    val isDialogOpen = remember { mutableStateOf(false) }
    val isDialogQROpen = remember { mutableStateOf(false) }

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
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary),
            onClick = ({navController.navigate("4/${playList_id}-${book.uid}")})
        ) {
            Row(modifier = Modifier
                .padding(0.dp)) {
                //Determines which isbn to use for image
                var thumbnailISBN = ""
                if(book.isbn_10.length == 0)
                {
                    thumbnailISBN = book.isbn_13;
                }
                else
                {
                    thumbnailISBN = book.isbn_10;
                }

                //Contains image
                GlideImage(
                    modifier = Modifier
                        .width(150.dp)
                        .height(225.dp),
                    model = "https://covers.openlibrary.org/b/ISBN/${thumbnailISBN}-M.jpg",
                    contentDescription = "${book.title} Cover",
                    contentScale = ContentScale.FillBounds
                )

                Column(
                    modifier = Modifier
                        .padding(start = 5.dp, top = 0.dp, bottom = 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(0.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = book.title, modifier = Modifier
                            .fillMaxWidth(0.80F))

                        //Icon Button
                        IconButton( { expanded = true }, modifier = Modifier
                                .padding(0.dp),) {
                            Icon(
                                imageVector = Icons.Filled.MoreVert,
                                contentDescription = "${book.title} menu"
                            )
                            //List of options
                            val listItems = arrayOf("Add to Playlist", "Delete", "Share")
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
                                                    expanded = false;
                                                }
                                                else if(itemIndex == 2)
                                                {
                                                    //Opens Dialog box
                                                    isDialogQROpen.value = true;
                                                    expanded = false;
                                                }
                                                else
                                                {
                                                    if(playList_id == booksDao.getSavedPlaylist())
                                                    {
                                                        Toast.makeText(context, "Deleted books", Toast.LENGTH_SHORT).show()
                                                        booksDao.deleteBooksFromSavedPlaylist(book.uid)
                                                        booksDao.deleteBookDetails(book.uid)
                                                    }
                                                    else
                                                    {
                                                        booksDao.deleteBookInPlaylist(playlist_id = playList_id, book_id = book.uid )
                                                    }

                                                    navController.navigate("3/${playList_id}")
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

    //Dialog box for reassign
    if(isDialogOpen.value)
    {
        //Dialog box variables
        var newPlaylistName by rememberSaveable { mutableStateOf("") }


        Dialog(onDismissRequest = { isDialogOpen.value = false }, DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)) {
            Card(
                modifier = Modifier
                    .wrapContentWidth()
                    .height(300.dp),
                shape = RoundedCornerShape(15.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary)
            ){
                Box(modifier = Modifier
                    .fillMaxHeight()
                    .width(200.dp)


                ) {
                    Column(modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(), horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Add to playlist",
                                modifier = Modifier
                                    .fillMaxWidth(),
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }
                        Column {
                            Row(modifier = Modifier
                                .fillMaxWidth(0.75F)
                                .fillMaxHeight(),){
                                //Excludes the default playlist
                                val listItems = booksDao.getAllPlaylists()

                                val contextForToast = LocalContext.current.applicationContext
                                LazyColumn(modifier = Modifier
                                ) {

                                    items(listItems) {item ->
                                        val checkedState = remember { mutableStateOf(false) }
                                        //Checks if the book is already assigned to the playlist
                                        if(booksDao.checksIfBookIsAssigned(item.uid, book.uid))
                                        {
                                            checkedState.value = true;
                                        }

                                        Row(modifier = Modifier
                                            .clickable { checkedState.value = !checkedState.value; }
                                            .fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically){


                                            Text(text = item.playlistName)
                                            Checkbox(checked = checkedState.value, onCheckedChange = {checkedState.value = !checkedState.value
                                                //Does the Add / Remove on playlist assignment
                                                if(checkedState.value)
                                                {
                                                    //Assigns
                                                    booksDao.assignBookToPlaylist(Playlists_Books(uid = 0, playlist_id = item.uid, book_id = book.uid))
                                                }
                                                //Verifies that the playlist select is not the default playlist
                                                else if (item.playlistName != "Saved")
                                                {
                                                    //Deletes from playlist
                                                    booksDao.deleteBookInPlaylist(playlist_id = item.uid, book_id = book.uid )

                                                }

                                            })
                                        }
                                        Divider(modifier = Modifier)
                                    }
                                }

                            }
                        }


                    }
                }
            }
        }
    }
    //Dialog box for QR code
    if(isDialogQROpen.value)
    {
        Dialog(onDismissRequest = { isDialogQROpen.value = false }, DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)) {
            Card(
                modifier = Modifier
,
                shape = RoundedCornerShape(15.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary)
            ){
                Box(modifier = Modifier

                ) {
                    var bookISBN = ""
                    if(book.isbn_10.length == 0)
                    {
                        bookISBN = book.isbn_13;
                    }
                    else
                    {
                        bookISBN = book.isbn_10;
                    }
                    val isbn = bookISBN// Explicitly convert to String if needed
                    val qrCodeSize = 500 // Set your desired QR code size

                    val mWriter = MultiFormatWriter()
                    val mMatrix = mWriter.encode(isbn, BarcodeFormat.QR_CODE, qrCodeSize, qrCodeSize)
                    //Creates bitmap
                    val mEncoder = BarcodeEncoder()
                    val mBitmap = mEncoder.createBitmap(mMatrix)
                    // Check if QR code was generated successfully
                    if (mBitmap != null) {

                        GlideImage(
                            modifier = Modifier,
                            model = mBitmap,
                            contentDescription = "${book.title} QR code",
                            contentScale = ContentScale.FillBounds
                        )
                    } else {
                        // Handle the case where QR code generation failed
                        Toast.makeText(context, "Failed to generate QR code", Toast.LENGTH_SHORT).show()
                    }

                }
            }
        }
    }
}