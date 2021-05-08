package com.todoapp.todoapp.utils

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import androidx.core.app.ActivityCompat.startActivityForResult
import com.todoapp.todoapp.activities.accountHandler.MyProfileActivity

object Constants {
    const val USERS:String="users"
    const val BOARDS: String = "boards"
    const val NAME: String = "name"
    const val EMAIL: String = "email"
    const val PASSWORD: String = "password"
    const val ASSIGNED_TO:String="assignedTo"
    const val DOCUMENT_ID:String="documentId"

    const val TASK_LIST:String="taskList"
    const val TASK_LIST_ITEM_POSITION:String = "task_list_item_position"
    const val CARD_LIST_ITEM_POSITION:String = "card_list_item_position"

    const val BOARD_DETAIL: String = "board_detail"
    const val TITLE: String = "title"

    const val IMAGE:String="image"

    const val READ_STORAGE_PERMISSION_CODE=1
    const val PICK_IMAGE_REQUEST_CODE = 2

    fun showImageChooser(activity: Activity)
    {
        val galleryIntent= Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        activity.startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST_CODE)
    }

    fun getFileExtension(activity: Activity,uri: Uri?):String?
    {
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(activity.contentResolver.getType(uri!!))
    }

}