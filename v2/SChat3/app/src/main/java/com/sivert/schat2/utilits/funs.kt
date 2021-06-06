package com.sivert.schat2.utilits

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import android.provider.OpenableColumns
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.ServerValue
import com.sivert.schat2.MainActivity
import com.sivert.schat2.R
import com.sivert.schat2.database.*
import com.sivert.schat2.models.CommonModel
import com.sivert.schat2.ui.message_recycler_view.views.MessageView
import com.sivert.schat2.ui.screens.main_list.MainListFragment
import com.sivert.schat2.ui.screens.single_chat.SingleChatFragment
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.*


fun showToast(message: String) {
    Toast.makeText(APP_ACTIVITY, message, Toast.LENGTH_SHORT).show()
}

fun restartActivity() {
    val intent = Intent(APP_ACTIVITY, MainActivity::class.java)
    APP_ACTIVITY.startActivity(intent)
    APP_ACTIVITY.finish()
}

fun replaceFragment(fragment: Fragment, addStack: Boolean = true) {
    if (addStack) {
        APP_ACTIVITY.supportFragmentManager.beginTransaction()
            .addToBackStack(null)
            .replace(
                R.id.data_container,
                fragment
            ).commit()
    } else {
        APP_ACTIVITY.supportFragmentManager.beginTransaction()
            .replace(
                R.id.data_container,
                fragment
            ).commit()
    }
}

fun hideKeyboard() {
    val imm: InputMethodManager =
        APP_ACTIVITY.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(APP_ACTIVITY.window.decorView.windowToken, 0)
}

fun ImageView.downloadAndSetImage(url: String) {
    Picasso.get()
        .load(url)
        .fit()
        .placeholder(R.drawable.default_photo)
        .into(this)
}

fun initContacts() {
    if (checkPermission(READ_CONTACTS)) {
        var arrayContacts = arrayListOf<CommonModel>()
        val cursor = APP_ACTIVITY.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            null,
            null,
            null
        )
        cursor?.let {
            while (it.moveToNext()) {
                val fullName =
                    it.getString(it.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                val phone =
                    it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                val iPhone = phone.replace(Regex("[\\s,-]"), "")
                val newModel = CommonModel()
                newModel.fullname = fullName
                newModel.phone = phone.replace(Regex("[\\s,-]"), "")
                if (iPhone != USER.phone) arrayContacts.add(newModel)
            }
        }
        cursor?.close()
        updatePhoneToDatabase(arrayContacts)

    }
}

fun String.asTime(): String {
    val time = Date(this.toLong())
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    return timeFormat.format(time)
}

fun getFilenameFromUri(uri: Uri): String {
    var result = ""
    val cursor = APP_ACTIVITY.contentResolver.query(uri, null, null, null, null)
    try {
        if (cursor != null && cursor.moveToFirst()) {
            result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
        }
    } catch (e: Exception) {
        showToast(e.message.toString())
    } finally {
        cursor?.close()
        return result
    }
}

fun getPlurals(count: Int) = APP_ACTIVITY.resources.getQuantityString(
    R.plurals.count_members, count, count
)

// my func
fun test() {
    showToast("TEST")

    fun getMessage(
        message: String,
        receivingUserID: String,
        typeText: String,
        function: () -> Unit
    ) {
        showToast(message)
    }

    fun sendsMessage(
        message: String,
        receivingUserID: String,
        typeText: String,
        function: () -> Unit
    ) {

        val refDialogUser = "$NODE_MESSAGES/$CURRENT_UID/$receivingUserID"
        val refDialogReceivingUser = "$NODE_MESSAGES/$receivingUserID/$CURRENT_UID"
        val messageKey = REF_DATABASE_ROOT.child(refDialogUser).push().key

        val mapMessage = hashMapOf<String, Any>()
        mapMessage[CHILD_FROM] =
            CURRENT_UID
        mapMessage[CHILD_TYPE] = typeText
        mapMessage[CHILD_TEXT] = message
        mapMessage[CHILD_ID] = messageKey.toString()
        mapMessage[CHILD_TIMESTAMP] =
            ServerValue.TIMESTAMP

        val mapDialog = hashMapOf<String, Any>()
        mapDialog["$refDialogUser/$messageKey"] = mapMessage
        mapDialog["$refDialogReceivingUser/$messageKey"] = mapMessage

        REF_DATABASE_ROOT
            .updateChildren(mapDialog)
            .addOnSuccessListener { function() }
            .addOnFailureListener { showToast(it.message.toString()) }
    }
}

fun createAlertDialog(
    contact: CommonModel,
    title: String,
    what: String,
    mRecyclerView: RecyclerView
) {

    val catNames = arrayOf("У меня", "У всех")
    var selectedItems = 0

    val builder = AlertDialog.Builder(APP_ACTIVITY)
    builder.setTitle(title)
        .setSingleChoiceItems(catNames, selectedItems) { dialog, item ->
            selectedItems = item
        }
        .setPositiveButton("OK") { dialog, id ->
            if (what == "clear") {
                when (selectedItems) {
                    0 -> clearChat(contact.id) {
                        showToast("Чат очищен")
//                        SingleChatFragment(contact).updateAdapter(mRecyclerView)
                        replaceFragment(SingleChatFragment(contact))
                    }
                    1 -> clearAllChat(contact.id) {
                        showToast("Чат очищен для всех")
                        replaceFragment(MainListFragment())
                    }
                }
            } else if (what == "delete") {
                when (selectedItems) {
                    0 -> deleteChat(contact.id) {
                        showToast("Чат удален")
                        replaceFragment(MainListFragment())
                    }
                    1 -> deleteAllChat(contact.id) {
                        showToast("Чат удален для всех")
                        replaceFragment(MainListFragment())
                    }
                }
            }

        }
        .setNegativeButton("Отмена") { dialog, id ->
        }
        .show()
    builder.create()
}

fun showPopupMenu(
    v: View,
    message: MessageView,
    contact: CommonModel,
    function: () -> Unit
) {
    val popupMenu = PopupMenu(APP_ACTIVITY, v)
    popupMenu.inflate(R.menu.popup_menu)
    popupMenu.setOnMenuItemClickListener { item ->
        when (item.itemId) {
            R.id.menu_reply_message -> {
                showToast("Ответить: " + contact.id)
                true
            }
            R.id.menu_copy_message -> {
                copy(message.text)
                true
            }
            R.id.menu_forward_message -> {
                showToast("Переслать: " + message.timeStamp.asTime())
                message.text
                true
            }
            R.id.menu_edit_message -> {
                showToast("Изменить")

                true
            }
            R.id.menu_delete_message -> {

                deleteMessage(message.id, contact) {
                    showToast("Удалить")
                    replaceFragment(SingleChatFragment(contact))
                    function()
                }
                true
            }
            else -> false
        }
    }
    popupMenu.setOnDismissListener { }
    popupMenu.show()
}


fun copy(text: CharSequence) {
    val clipboard = APP_ACTIVITY.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("", text)
    clipboard.setPrimaryClip(clip)
    showToast("Скопировано в буфер обмена")
}
