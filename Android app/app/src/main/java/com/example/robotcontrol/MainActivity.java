package com.example.robotcontrol;

import android.graphics.BitmapFactory;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.graphics.Bitmap;

import org.json.JSONObject;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class MainActivity extends AppCompatActivity {

    private Socket socket; //Create the socket instance
    private BufferedInputStream in; //input stream instance
    private PrintWriter out; //output stream instance
    private InetAddress address; //Put address of the raspberry pi here
    private int port; //Put the port number of the raspberry pi here
    private ImageView robotCamera; //create the imageView instance
    private Switch modeToggle;
    private TextView modeStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Get the toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //Instantiate the port
        port = 5000;

        try {
            address = InetAddress.getByName("137.82.226.227");
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        //Instantiate the mode toggle
        modeToggle = (Switch) findViewById(R.id.modeToggle);
        //Instantiate the mode status
        modeStatus = (TextView) findViewById(R.id.ModeStatus);
        //Get the forwardButton
        final Button forwardButton = (Button) findViewById(R.id.forward);
        //Get the leftButton
        final Button leftButton = (Button) findViewById(R.id.leftwards);
        //Get the rightbutton
        final Button rightButton = (Button) findViewById(R.id.rightwards);
        //Instantiate the imageView
        robotCamera = findViewById(R.id.imageView);
        //Set a default image
        robotCamera.setImageResource(R.drawable.ic_launcher_background);

        //Add an event listener for the forwardsButton to call moveForward onclick
        forwardButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                moveForward();
            }
        });

        //Add an event listener for the leftButton to call turnLeft onclick
        leftButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                turnLeft();
            }
        });

        //Add an event listener for the rightButton to call turnRight onclick
        rightButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                turnRight();
            }
        });

        //Add an event listener for the toggle
        modeToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //If the switch is on
                if(isChecked) {
                    modeStatus.setText("Mode:Remote Control");
                }
                else {
                    modeStatus.setText("Mode:Autonomous");
                }
                ChangeMode(isChecked);
            }
        });

        //Try to serve
//        while(true) {
            try {
                serve();
            } catch (Exception e) {
                //If exception, print stack trace
                e.printStackTrace();
            }
            final TextView connectedStatus = (TextView) findViewById(R.id.textView);
            connectedStatus.setText("Not Connected");
//        }
    }

    //Start the sever
    public void serve() throws IOException{
        //Set the onscreen message to say that there is a good connection
        final TextView connectedStatus = (TextView) findViewById(R.id.textView);

        //While loop for handling until we close sockets
        while (true) {
            try {
                //Connect it to the specified port and ip address
                socket = new Socket(address, port);
                //Setup the input and output streams
                in = new BufferedInputStream(socket.getInputStream());
//                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));

                //If no exception, display a message so we know that it is connected
                connectedStatus.setText("Connected");

                //Start the mainFunction
                mainFunctionality();
            } catch (Exception e) {
                //If execption, print stack trace
                e.printStackTrace();
            //Once the try catch finishes
            } finally {
                //Close the input stream
                in.close();
                //Close the output stream
                out.close();
                //Close the socket
                socket.close();
            }
        }
    }

    //Start to listen and serve requests
    public void mainFunctionality() {
        //create a bitmap factory instance
        BitmapFactory converter = new BitmapFactory();

        //Continually update the photo
        while (true) {
            //create a new bitmap
            Bitmap image = converter.decodeStream(in);
            //set the image as the newly converted bitmap
            robotCamera.setImageBitmap(image);
        }
    }

    //Send a request
    public void sendRequest(String movement, String mode) {
        //Create a new JSONObject
        JSONObject request = new JSONObject();

        //Try adding fields to the JSON
        try {
            request.put("Type", movement);
            request.put(s", mode);
                    
            //Send the request out over the socket
            out.print(request);
            //Flush the socket
            out.flush();
        }
        catch (Exception e) {
            //If exception, print stack trace
            e.printStackTrace();
        }
    }

    //Tell the pi to change modes
    public void ChangeMode(boolean mode) {
        if(mode) {
            sendRequest("NULL", "Remote");
        }
        else {
            sendRequest("NULL", "Autonomous");
        }
    }

    //send a request to turn left
    public void turnLeft()  {
        if(modeStatus.getText() == "Mode:Remote Control") {
            sendRequest("Left", "Remote");
        }
    }

    //send a request to turn right
    public void turnRight() {
        if(modeStatus.getText() == "Mode:Remote Control") {
            sendRequest("Right", "Remote");
        }
    }

    //send a request to move forwards
    public void moveForward() {
        if(modeStatus.getText() == "Mode:Remote Control") {
            sendRequest("Forward", "Remote");
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
