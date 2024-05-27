package com.hoangdoviet.finaldoan.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hoangdoviet.finaldoan.R
import com.hoangdoviet.finaldoan.model.Message
import com.hoangdoviet.finaldoan.utils.Constants.RECEIVE_ID
import com.hoangdoviet.finaldoan.utils.Constants.SEND_ID

class MessageAdapter : RecyclerView.Adapter<MessageAdapter.MyViewHolder>() {
    var messageList   = mutableListOf<Message>()
    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tv_message: TextView = itemView.findViewById(R.id.tv_message)
        val tv_bot_message: TextView = itemView.findViewById(R.id.tv_bot_message)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.message_item, parent, false)
        )
    }

    override fun getItemCount(): Int = messageList.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentMessage = messageList[position]
        when(currentMessage.id){
            SEND_ID -> {
                holder.tv_message.apply {
                    text = currentMessage.message
                    visibility = View.VISIBLE
                }
                holder.tv_bot_message.visibility = View.GONE
            }
            RECEIVE_ID -> {
                holder.tv_bot_message.apply {
                    text = currentMessage.message
                    visibility = View.VISIBLE
                }
                holder.tv_message.visibility = View.GONE
            }
        }
    }
    fun insertMessage(message: Message) {
        messageList.add(message)
        notifyItemInserted(messageList.size)
    }
}