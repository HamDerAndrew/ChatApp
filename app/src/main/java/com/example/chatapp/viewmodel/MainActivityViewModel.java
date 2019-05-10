package com.example.chatapp.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import com.example.chatapp.R;
import com.example.chatapp.models.ChatMessage;
import com.firebase.ui.database.FirebaseListOptions;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.List;

public class MainActivityViewModel extends ViewModel {
    private MutableLiveData<List<ChatMessage>> chatMessages;

    public LiveData<List<ChatMessage>> getChatMessages() {
        if(chatMessages == null) {
            chatMessages = new MutableLiveData<List<ChatMessage>>();

            //Open a query instance to Firebase database
            Query query = FirebaseDatabase.getInstance().getReference();

            //create FirebaseListOptions
            FirebaseListOptions<ChatMessage> options = new FirebaseListOptions.Builder<ChatMessage>()
                    .setQuery(query, ChatMessage.class)
                    .setLayout(R.layout.message)
                    .build();
        }
        return chatMessages;
    }

    private void loadChatMessages() {
    }
}
