package com.psb.geminiai.ui.chat

import android.app.Activity
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.ai.sample.feature.chat.ChatMessage
import com.google.ai.sample.feature.chat.Participant
import com.psb.geminiai.R
import com.psb.geminiai.databinding.ItemIncomingMessageBinding
import com.psb.geminiai.databinding.ItemOutgoingMessageBinding
import com.psb.geminiai.utils.setVisible
import com.psb.geminiai.utils.timeAgo
import com.yydcdut.markdown.MarkdownProcessor
import java.util.Date

class ChatAdapter(val activity: Activity, val list: ArrayList<ChatMessage>, val markdownProcessor: MarkdownProcessor) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val INCOMING = 0
    private val OUTGOING = 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == OUTGOING) OutGoingViewHolder(LayoutInflater.from(activity).inflate(R.layout.item_outgoing_message, parent, false))
        else IncomingViewHolder(LayoutInflater.from(activity).inflate(R.layout.item_incoming_message, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]
        if (holder is IncomingViewHolder) {
            holder.binding.textMessageBody.text = markdownProcessor.parse(model.text)
            holder.binding.textMessageTime.text = model.time.timeAgo()
        } else if (holder is OutGoingViewHolder) {
            holder.binding.textMessageBody.text = markdownProcessor.parse(model.text)
            holder.binding.textMessageTime.text = model.time.timeAgo()
            model.img?.let {
                Glide.with(holder.binding.imgUserSent).asBitmap().load(it).into(holder.binding.imgUserSent)
                holder.binding.imgUserSent.setVisible()
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (list[position].participant == Participant.USER) OUTGOING else INCOMING
    }

    inner class IncomingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = ItemIncomingMessageBinding.bind(itemView)
    }

    inner class OutGoingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = ItemOutgoingMessageBinding.bind(itemView)
    }

}
