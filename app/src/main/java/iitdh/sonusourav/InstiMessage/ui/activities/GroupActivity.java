package iitdh.sonusourav.InstiMessage.ui.activities;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseListAdapter;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import iitdh.sonusourav.InstiMessage.R;
import iitdh.sonusourav.InstiMessage.models.ChatMessage;

public class GroupActivity extends AppCompatActivity {

    public static final int SIGN_IN_REQUEST_CODE = 1;
    private FloatingActionButton fab;
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    private ActionBar actionBar;
    private FirebaseListAdapter<ChatMessage> adapter;
    private ImageView imageView;
    Boolean notif = false;
    public static final int NOTIFICATION_ID = 1;
    String messgaeLastText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);
        bindView();

        actionBar=getSupportActionBar();
        actionBar.setTitle(Html.fromHtml("<font color=\"#33cccc\">" + "Invisibility zone" + "</font>"));
        isInternetOn();
        displayChatMessages();
        lastMessage();

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText input = (EditText) findViewById(R.id.input);

                // Read the input field and push a new instance
                // of ChatMessage to the Firebase database
                if (input.getText().toString().length() > 0) {
                    FirebaseDatabase.getInstance().getReference()
                            .child("Chats")
                            .push()
                            .setValue(new ChatMessage(input.getText().toString(),
                                    pref.getString("email", null)
)
                            );
                    // Clear the input
                    input.setText("");
                } else {
                    input.setError("You can't post an empty Message. !!");
                }
            }
        });
//checking for first time launch
        if (pref.getBoolean("NotificationSend", notif) == false) {
            sendNotification();
        } else {

        }
    }

    //gets value of last message
    private void lastMessage() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        // change this to your databae ref
        final DatabaseReference messages = database
                .getReference()
                .child("Chats"); // change this to your databae ref

        messages.limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {
                    // access last message
                    DataSnapshot messageSnapShot = dataSnapshot.getChildren().iterator().next();
                    messgaeLastText = (String) messageSnapShot.child("messageText").getValue();
                    Log.v("Value", messgaeLastText);

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void bindView() {
        pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        editor = pref.edit();
        imageView = (ImageView) findViewById(R.id.imageView);

    }



    private void displayChatMessages() {
        ListView listOfMessages = (ListView) findViewById(R.id.list_of_messages);
        adapter = new FirebaseListAdapter<ChatMessage>(this, ChatMessage.class,
                R.layout.message, FirebaseDatabase.getInstance().getReference().child("Chats")) {
            @Override
            protected void populateView(View v, ChatMessage model, int position) {
                // Get references to the views of message.xml
                TextView messageText = (TextView) v.findViewById(R.id.message_text);
                TextView messageUser = (TextView) v.findViewById(R.id.message_user);
                TextView messageTime = (TextView) v.findViewById(R.id.message_time);

                // Set their text
                messageText.setText(model.getMessageText());
                messageUser.setText(model.getMessageUser());

                // Format the date before showing it
                messageTime.setText(DateFormat.format("dd-MM-yyyy (HH:mm:ss)",
                        model.getMessageTime()));
            }
        };

        listOfMessages.setAdapter(adapter);


    }


    //check intenet connection
    public final boolean isInternetOn() {

        // get Connectivity Manager object to check connection
        ConnectivityManager connec =
                (ConnectivityManager) getSystemService(getBaseContext().CONNECTIVITY_SERVICE);

        // Check for network connections
        if (connec.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.CONNECTED ||
                connec.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.CONNECTING ||
                connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.CONNECTING ||
                connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.CONNECTED) {

            // if connected with internet

//            Toast.makeText(this, " Connected ", Toast.LENGTH_LONG).show();
            return true;

        } else if (
                connec.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.DISCONNECTED ||
                        connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.DISCONNECTED) {
//            startActivity(new Intent(GroupActivity.this, NoInternet.class));
              Toast.makeText(this, " Internet unavailable ", Toast.LENGTH_LONG).show();

            return false;
        }
        return false;
    }

    public void sendNotification() {
        // Use NotificationCompat.Builder to set up our notification.
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        //icon appears in device notification bar and right hand corner of notification
        builder.setSmallIcon(R.drawable.send);
        // This intent is fired when notification is clicked
        Intent intent = new Intent(this, GroupActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        // Set the intent that will fire when the user taps the notification.
        builder.setContentIntent(pendingIntent);
        // Large icon appears on the left of the notification
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.send));
        // Content title, which appears in large type at the top of the notification
        builder.setContentTitle("InstiMessage");
        // Content text, which appears in smaller text below the title
        builder.setContentText("Welcome to IIT Dh Messenger");
        // The subtext, which appears under the text on newer devices.
        // This will show-up in the devices with Android 4.2 and above only
        builder.setSubText("Tap to view documentation about notifications.");
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // Will display the notification in the notification bar
        notificationManager.notify(NOTIFICATION_ID, builder.build());

        notif = true;
        editor.putBoolean("NotificationSend", notif);
        editor.commit();

    }

    @Override
    protected void onResume() {
        isInternetOn();
        super.onResume();
    }
}
