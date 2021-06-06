package com.sivert.schat2.ui.screens.single_chat

import android.net.Uri
import android.view.View
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.sivert.schat2.R
import com.sivert.schat2.database.USER
import com.sivert.schat2.database.getMessageKey
import com.sivert.schat2.database.uploadFileToStorage
import com.sivert.schat2.models.CommonModel
import com.sivert.schat2.ui.message_recycler_view.view_holders.AppHolderFactory
import com.sivert.schat2.ui.message_recycler_view.view_holders.MessageHolder
import com.sivert.schat2.ui.message_recycler_view.views.MessageView
import com.sivert.schat2.utilits.*

class SingleChatAdapter(private val contact: CommonModel) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var mListMessagesCache = mutableListOf<MessageView>()
    private var mListHolders = mutableListOf<MessageHolder>()

    private lateinit var mAppVoiceRecorder: AppVoiceRecorder



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return AppHolderFactory.getHolder(parent, viewType)
    }

    override fun getItemViewType(position: Int): Int {
        return mListMessagesCache[position].getTypeView()
    }

    override fun getItemCount(): Int = mListMessagesCache.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as MessageHolder).drawMessage(mListMessagesCache[position])
        var message = mListMessagesCache[position].text
        var fromId = mListMessagesCache[position].from
        val messageKey = getMessageKey(contact.id)
        if (message == "play") {
            if (fromId == USER.id) {
                showToast("Записуємо у нього голосове")
            }
            else {
                showToast("Записуємо у вас голосове")
            }
        }

        holder.itemView.setOnClickListener {
            showPopupMenu(
                holder.itemView,
                mListMessagesCache[position],
                contact
            ) {
                showToast("Test")
            }
        }

        holder.itemView.setOnLongClickListener(OnLongClickListener { v ->
            showToast("Test")
            true
        })
    }

    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
        (holder as MessageHolder).onAttach(mListMessagesCache[holder.adapterPosition])
        mListHolders.add((holder as MessageHolder))
        super.onViewAttachedToWindow(holder)
    }

    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
        (holder as MessageHolder).onDetach()
        mListHolders.remove((holder as MessageHolder))
        super.onViewDetachedFromWindow(holder)
    }

    fun addItemToBottom(
        item: MessageView,
        onSuccess: () -> Unit
    ) {
        if (!mListMessagesCache.contains(item)) {
            mListMessagesCache.add(item)
            notifyItemInserted(mListMessagesCache.size)
        }
        onSuccess()
    }

    fun addItemToTop(
        item: MessageView,
        onSuccess: () -> Unit
    ) {
        if (!mListMessagesCache.contains(item)) {
            mListMessagesCache.add(item)
            mListMessagesCache.sortBy { it.timeStamp.toString() }
            notifyItemInserted(0)
        }
        onSuccess()
    }


    fun onDestroy() {
        mListHolders.forEach {
            it.onDetach()
        }
    }

}