package com.hoangdoviet.finaldoan.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.hoangdoviet.finaldoan.databinding.ListUserLayoutBinding
import com.hoangdoviet.finaldoan.model.LoginUiState
import com.hoangdoviet.finaldoan.utils.showToast

class UserListAdapter(
    val users: MutableList<LoginUiState>,
    private val listener: OnUserDeleteListener
) : RecyclerView.Adapter<UserListAdapter.UserViewHolder>() {
    inner class UserViewHolder(private val binding: ListUserLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(user: LoginUiState) {
            binding.studentNameListItem.text = user.username
            binding.studentCodeAndEmailListItem.text = user.email
            binding.buttonDeleteStudent.setOnClickListener {
                listener.onUserDelete(user.email, adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ListUserLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding)
    }

    override fun getItemCount() = users.size

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(users[position])
    }
    fun removeUserAt(position: Int) {
        users.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, users.size)
    }

}

interface OnUserDeleteListener {
    fun onUserDelete(email: String, position: Int)
}
