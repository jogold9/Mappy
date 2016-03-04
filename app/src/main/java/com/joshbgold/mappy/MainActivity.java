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
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;

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

                //1. Find out where user wants to go
                getDestination(addressFieldSpinner);

                //2. Check for custom address
                if (address.equals("Custom")) {
                    getCustomAddress();
                }

                else {
                    //3. Find out what transportation method user wants
                    GetTransportType(googTransitButton, googBikeButton, googDrivingButton, googWalkingButton);

                    //4. Get directions from Google Maps
                    getGoogleDirections(mode);
                }
            }
        });

    }

    private void getDestination(Spinner addressFieldSpinner) {
        friendlyLocation = addressFieldSpinner.getSelectedItem().toString();

        if (friendlyLocation.equals("Home")) {
            address = getString(R.string.homeAddress);
        } else if (friendlyLocation.equals("Sarahs house")) {
            address = getString(R.string.SarahsAddress);
        } else if (friendlyLocation.equals("Work")) {
            address = getString(R.string.workAddress);
        } else if (friendlyLocation.equals("Custom")) {
            address = "Custom";
        } else {
            //do nothing
        }
    }

    private String GetTransportType(RadioButton googTransitButton, RadioButton googBikeButton, RadioButton googDrivingButton, RadioButton
            googWalkingButton) {

        if (googBikeButton.isChecked()) {
            mode = "&mode=b";  //user chose biking
        } else if (googDrivingButton.isChecked()) {
            mode = "&mode=d"; //user choose driving
        } else if (googTransitButton.isChecked()) {
            mode = "&mode=transit";  //user chose public transit
        } else if (googWalkingButton.isChecked()) {
            mode = "&mode=w"; //user chose walking
        }
        return mode;
    }

    private void getGoogleDirections(String mode) {
        boolean connected = isConnected();

        if (!connected) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Mappy is sorry...")
                    .setIcon(R.mipmap.ic_launcher)
                    .setMessage("You do not have a mobile or wifi connection with internet service at this time.")
                    .setCancelable(false)
                    .setNegativeButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        } else if (address == null || address.equals("")) {
            Toast.makeText(MainActivity.this, "It looks like you select custom address, but did not enter an address.", Toast.LENGTH_LONG).show();
        } else {
            try {
                address = address.replace(' ', '+');
                Uri mapsIntentUri = Uri.parse("google.navigation:q=" + address + mode);
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, mapsIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);
            } catch (Exception exception) {
                Toast.makeText(MainActivity.this, "Oh noes!  We are unable to navigate to destination for some reason.  Please try formatting " +
                                "your address a little differently, or try again later.",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    public class SpinnerActivity extends Activity implements AdapterView.OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {

            friendlyLocation = parent.getItemAtPosition(pos).toString();
            
        }

        public void onNothingSelected(AdapterView<?> parent) {
            // Another interface callback

        }
    }

    private String getCustomAddress() {
        final EditText addressEditText = new EditText(this);
        final String[] customAddress = {""};

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Please enter destination address");
        alertDialog.setView(addressEditText);
        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                customAddress[0] = addressEditText.getText().toString();
                address = customAddress[0];
                doNext();
            }
        });
        alertDialog.setNegativeButton("CANCEL", null);
        alertDialog.create().show();

        return customAddress[0];
    }

    private void doNext(){
        final RadioButton googTransitButton = (RadioButton) findViewById(R.id.googTransitButton);
        final RadioButton googBikeButton = (RadioButton) findViewById(R.id.googBicycleButton);
        final RadioButton googDrivingButton = (RadioButton) findViewById(R.id.googDrivingButton);
        final RadioButton googWalkingButton = (RadioButton) findViewById(R.id.googWalkButton);

        //3. Find out what transportation method user wants
        GetTransportType(googTransitButton, googBikeButton, googDrivingButton, googWalkingButton);

        //4. Get directions from Google Maps
        getGoogleDirections(mode);

    }

    //Checks for mobile or wifi connectivity, returns true for connected, false otherwise
    private boolean isConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnectedOrConnecting()) {
            return true;
        } else {
            return false;
        }
    }
}
