package com.example.chatapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseListOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.text.DateFormat;

public class MainActivity extends AppCompatActivity {
    public static int SIGN_IN_REQUEST_CODE = 5;
    private FirebaseListAdapter<ChatMessage> adapter;

    //FirebaseListAdapter needs this to listen to changes in the database (like when we write messages)
    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    //FirebaseListAdapter needs to stop listening for changes in the database when the activity ends
    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*----------SETUP FOR THE CHAT MESSAGE VIEWS--------------*/
        /*----------This should probably be in a void method, but I tried that with no success------*/
        //declare the chat messages window ListView
        ListView listOfMessages = (ListView)findViewById(R.id.message_window);

        //Open a query instance to Firebase database
        Query query = FirebaseDatabase.getInstance().getReference();

        //create FirebaseListOptions
        FirebaseListOptions<ChatMessage> options = new FirebaseListOptions.Builder<ChatMessage>()
                .setQuery(query, ChatMessage.class)
                .setLayout(R.layout.message)
                .build();

        adapter = new FirebaseListAdapter<ChatMessage>(options) {
            @Override
            protected void populateView(View v, ChatMessage model, int position) {
                //Here we get the reference to the views of message.xml file
                TextView messageText = (TextView)v.findViewById(R.id.message_content);
                TextView messageUser = (TextView)v.findViewById(R.id.message_author);
                TextView messageTime = (TextView)v.findViewById(R.id.message_sent);

                //Set their text/user input
                messageText.setText(model.getMessageText());
                messageUser.setText(model.getMessageUser());

                //Format the date display before actually showing it
                //Having problems with the "format" method, therefor it's commented out
                // messageTime.setText(DateFormat.format("dd-MM-yyyy (HH:mm:ss)", model.getMessageTime()));
            }
        };
        //Put the chat messages in the window listview
        listOfMessages.setAdapter(adapter);
        /*----------END OF SETUP FOR THE CHAT MESSAGE VIEWS--------------*/



        /*----------SIGN IN / SIGN UP FUNCTIONALITY--------------*/
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            // Start sign in/sign up activity
            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .build(),
                    SIGN_IN_REQUEST_CODE
            );
        } else {
            // User is already signed in, then show welcome
            Toast.makeText(this,
                    "Welcome " + FirebaseAuth.getInstance()
                            .getCurrentUser()
                            .getDisplayName(),
                    Toast.LENGTH_LONG)
                    .show();
        }
        /*----------END OF SIGN IN / SIGN UP FUNCTIONALITY--------------*/



        FloatingActionButton sendMessage = (FloatingActionButton)findViewById(R.id.sendMessage);

        sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText message_input = (EditText)findViewById(R.id.message_input);

                //Read what is in the input field and make a new instance of ChatMessage to the Firebase Database
                FirebaseDatabase.getInstance()
                        .getReference()
                        .push()
                        .setValue(new ChatMessage(message_input.getText().toString(),
                                FirebaseAuth.getInstance().getCurrentUser().getDisplayName())
                        );
                //Empty the input for text
                message_input.setText("");
            }
        });
    }

    //Override to handle clickevents on the menu item
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SIGN_IN_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this,
                        "Success! Welcome to the ChatApp!",
                        Toast.LENGTH_LONG)
                        .show();
            } else {
                Toast.makeText(this,
                        "Unable to sign in. Try again later.",
                        Toast.LENGTH_LONG)
                        .show();
                // Close the app
                finish();
            }
        }
    }

    //Instantiate the menu here in MainActivity
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    //Handle clickevents on the log-out menu item.
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.sign_out) {
            AuthUI.getInstance().signOut(this)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(MainActivity.this,
                                    "You are now logged out.",
                                    Toast.LENGTH_LONG)
                                    .show();

                            // Close activity
                            finish();
                        }
                    });
        }
        return true;
    }
}
