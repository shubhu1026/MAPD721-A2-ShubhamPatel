package com.shubhu1026.mapd721_a2_shubhampatel

import android.annotation.SuppressLint
import android.content.ContentProviderOperation
import android.os.Bundle
import android.provider.ContactsContract
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.shubhu1026.mapd721_a2_shubhampatel.ui.theme.MAPD721A2ShubhamPatelTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MAPD721A2ShubhamPatelTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ContactManager(context = this)
                }
            }
        }
    }
}

data class Contact(val displayName: String, val phoneNumber: String)

@SuppressLint("UnrememberedMutableState")
@Composable
fun ContactManager(context: ComponentActivity) {
    var contactName by remember { mutableStateOf("") }
    var contactNumber by remember { mutableStateOf("") }
    var contacts by remember { mutableStateOf(emptyList<Contact>()) }

    val shouldLoadContacts = remember { mutableStateOf(false) }
    val shouldSaveContact = remember { mutableStateOf(false) }

    if (shouldLoadContacts.value) {
        contacts = loadContacts(context = context)
        shouldLoadContacts.value = false
    }

    if (shouldSaveContact.value) {
        addContact(context = context, name = contactName, number = contactNumber)
        shouldSaveContact.value = false
        contacts = loadContacts(context = context)
    }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize()
    ) {
        // Input fields
        OutlinedTextField(
            value = contactName,
            onValueChange = { contactName = it },
            label = { Text("Contact Name") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )

        OutlinedTextField(
            value = contactNumber,
            onValueChange = { contactNumber = it },
            label = { Text("Contact Number") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
        )

        // Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = {
                    shouldLoadContacts.value = true
                },
            ) {
                Text("Load Contacts")
            }

            Spacer(modifier = Modifier.width(16.dp))

            Button(
                onClick = { shouldSaveContact.value = true},
            ) {
                Text("Add Contact")
            }
        }

        // Contacts List
        LazyColumn {
            items(contacts) { contact ->
                Column(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth()
                ) {
                    Text(contact.displayName, modifier = Modifier.padding(bottom = 4.dp))
                    Text(contact.phoneNumber, modifier = Modifier.padding(top = 4.dp), color = Color.Gray)
                }
            }
        }
    }
}

@SuppressLint("Range")
@Composable
fun loadContacts(context: ComponentActivity): List<Contact> {
    val contacts = mutableListOf<Contact>()

    context.contentResolver.query(
        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
        arrayOf(
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
        ),
        null,
        null,
        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
    )?.use { cursor ->
        if (cursor.moveToFirst()) {
            do {
                val displayName =
                    cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                val phoneNumber =
                    cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                contacts.add(Contact(displayName, phoneNumber))
            } while (cursor.moveToNext())
        }
    }

    return contacts
}

@Composable
fun addContact(context: ComponentActivity, name: String, number: String){
    val ops = ArrayList<ContentProviderOperation>()

    // Create a new raw contact
    ops.add(
        ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
            .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
            .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
            .build()
    )

    // Add contact's name
    ops.add(
        ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
            .withValue(
                ContactsContract.Data.MIMETYPE,
                ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE
            )
            .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name)
            .build()
    )

    // Add contact's phone number
    ops.add(
        ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
            .withValue(
                ContactsContract.Data.MIMETYPE,
                ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE
            )
            .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, number)
            .withValue(
                ContactsContract.CommonDataKinds.Phone.TYPE,
                ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE
            )
            .build()
    )

    // Execute the operations
    try {
        context.contentResolver.applyBatch(ContactsContract.AUTHORITY, ops)
        Toast.makeText(context, "Successful", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        // Handle the exception, e.g., show an error message
        Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show()
        e.printStackTrace()
    }

}

@Preview(showBackground = true)
@Composable
fun ContactManagerPreview() {
    val context = LocalContext.current as ComponentActivity
    MAPD721A2ShubhamPatelTheme {
        ContactManager(context = context)
    }
}