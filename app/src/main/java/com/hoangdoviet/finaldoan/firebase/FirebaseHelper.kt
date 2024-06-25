package com.hoangdoviet.finaldoan.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.hoangdoviet.finaldoan.model.Message

class FirebaseHelper {
    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference
    private val currentUser = FirebaseAuth.getInstance().currentUser

    fun insertMessage(message: Message) {
        currentUser?.let { user ->
            val userMessagesRef = database.child("messages").child(user.uid)
            val key = userMessagesRef.push().key
            if (key != null) {
                userMessagesRef.child(key).setValue(message)
            }
        }
    }

    fun getAllMessages(callback: (List<Message>) -> Unit) {
        currentUser?.let { user ->
            val userMessagesRef = database.child("messages").child(user.uid)
            val messages = mutableListOf<Message>()
            userMessagesRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (dataSnapshot in snapshot.children) {
                        val message = dataSnapshot.getValue(Message::class.java)
                        if (message != null) {
                            messages.add(message)
                        }
                    }
                    callback(messages)
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle possible errors.
                }
            })
        }
    }
}