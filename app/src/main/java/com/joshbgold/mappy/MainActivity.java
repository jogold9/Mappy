package com.joshbgold.mappy;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Spinner;

public class MainActivity extends Activity {
    private String friendlyLocation = "";
    private String address = "";
    private String mode = "@mode=d";  //mode b is bicycling, mode d is for driving, mode t is for transit, mode w is for walking

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Spinner addressFieldSpinner = (Spinner) findViewById(R.id.dropdownEditText);
        final Button launchMapButton = (Button) findViewById(R.id.launchMapButton);
        final RadioButton googTransitButton = (RadioButton) findViewById(R.id.googTransitButton);
        final RadioButton googBikeButton = (RadioButton) findViewById(R.id.googBicycleButton);
        final RadioButton googDrivingButton = (RadioButton) findViewById(R.id.googDrivingButton);
        final RadioButton googWalkingButton = (RadioButton) findViewById(R.id.googWalkButton);
        final Button exitButton = (Button)findViewById(R.id.exit_button);


        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.locations_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        addressFieldSpinner.setAdapter(adapter);

        launchMapButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {

                getDestination(addressFieldSpinner);  //gets the destination user has entered

                LaunchNavChoice(googTransitButton, googBikeButton, googDrivingButton, googWalkingButton);
            }
        });

        View.OnClickListener exit = new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                finish();
            }
        };

        exitButton.setOnClickListener(exit);
    }

    private void LaunchNavChoice(RadioButton googTransitButton, RadioButton googBikeButton, RadioButton googDrivingButton, RadioButton
            googWalkingButton) {

        if (googBikeButton.isChecked()){
            mode = "&mode=b";
            getGoogleDirections(mode);
        }
        else if (googDrivingButton.isChecked()){
            mode = "&mode=d";
            getGoogleDirections(mode);
        }
        else if (googTransitButton.isChecked()) {
            mode = "&mode=transit";
            getGoogleDirections(mode);
        }
        else if (googWalkingButton.isChecked()){
            mode = "&mode=w";
            getGoogleDirections(mode);
        }
    }

    private void getDestination(Spinner addressFieldSpinner) {
        friendlyLocation = addressFieldSpinner.getSelectedItem().toString();

        if (friendlyLocation.equals("Home")) {
            address = "2617 SE 35th Place, 97202";
        } else if (friendlyLocation.equals("Sarahs house")) {
            address = "3946 NE Failing, 97212";
        } else if (friendlyLocation.equals("Work")) {
            address = "619 SW 11th Avenue, 97205";
        } else if (friendlyLocation.equals("Custom")) {
            //have an AlertDialog that pops up with an EditText for entering the item then you just add that to your Array
            //http://stackoverflow.com/questions/15672767/allow-users-to-enter-new-value-in-spinner
            address = friendlyLocation;
        } else {
        }
    }

    private void getGoogleDirections(String mode) {
        boolean connected = isConnected();

        if (!connected){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Mappy is sorry...")
                    .setIcon(R.mipmap.ic_launcher)
                    .setMessage("You do not have a mobile or wifi connection with internet service at this time.")
                    .setCancelable(false)
                    .setNegativeButton("OK",new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        }
        else {
            try {
                address = address.replace(' ', '+');
                Uri mapsIntentUri = Uri.parse("google.navigation:q=" + address + mode);
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, mapsIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);
            }
            catch (Exception exception) {
            }
        }
    }

 /*   private void getWazeCarDirections() {
        try {
            address = address.replace(' ', '+');
            Uri mapsIntentUri = Uri.parse("waze.navigation:q=" + address);
            Intent mapIntent = new Intent( Intent.ACTION_VIEW, mapsIntentUri);
            startActivity(mapIntent);

            //if Waze is not installed, take user to page where they can download and install Waze
        } catch (ActivityNotFoundException exception) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.waze"));
        }
    }*/

    public class SpinnerActivity extends Activity implements AdapterView.OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> parent, View view,int pos, long id) {
            // An item was selected. You can retrieve the selected item using
            // parent.getItemAtPosition(pos)
            friendlyLocation = parent.getItemAtPosition(pos).toString();

            if (friendlyLocation.equals("Home")) {
                address = "2617 SE 35th Place";
            } else if (friendlyLocation.equals("Sarahs house")) {
                address = "3946 NE Failing, Portland, OR 97212";
            } else if (friendlyLocation.equals("Work")) {
                address = "619 SW 11th Avenue, Portland, OR 97205";
            } else if (friendlyLocation.equals("Custom")) {
                address = friendlyLocation;
            } else {
            }
        }

        public void onNothingSelected(AdapterView<?> parent) {
            // Another interface callback

        }
    }

    //Checks for mobile or wifi connectivity, returns true for connected, false otherwise
    private boolean isConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED;
    }

}
