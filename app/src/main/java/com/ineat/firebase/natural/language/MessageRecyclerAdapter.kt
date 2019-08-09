package com.ineat.firebase.natural.language

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_message.view.*

class MessageRecyclerAdapter(private val messages: List<Message>): RecyclerView.Adapter<MessageRecyclerAdapter.Holder>()  {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_message, parent, false)
        return Holder(view)
    }

    override fun getItemCount(): Int = messages.size

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bindMessage(messages[position])
    }

    class Holder(v: View) : RecyclerView.ViewHolder(v) {

        fun bindMessage(message: Message) {
            itemView.messageLayout.gravity = if (message.isMe) Gravity.END else Gravity.START
            itemView.messageTextView.setBackgroundResource(if (message.isMe) R.drawable.bg_item_end else R.drawable.bg_item_start)
            itemView.messageTextView.text = message.text
        }


    }

}
