package com.joshbgold.mappy;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
    private String homeAddress = "";
    private String workAddress = "";
    private String customAddress = "";
    private String mode = "@mode=d";  //mode b is bicycling, d is for driving, t is for transit, w is for walking
    protected String destinationType = "";

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

        homeAddress = loadPrefs("home", homeAddress);
        workAddress = loadPrefs("work", workAddress);

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

                getDestination(addressFieldSpinner);

            }
        });
    }

    private void getDestination(Spinner addressFieldSpinner) {
        friendlyLocation = addressFieldSpinner.getSelectedItem().toString();

        if (friendlyLocation.equals("Home")) {
            if (homeAddress.equals("")){
                address = getCustomAddress("Home");
            }
            else {
                address = homeAddress;
                doNext(destinationType);
            }
        }

        else if (friendlyLocation.equals("Work")) {
            if (workAddress.equals("")){
                address = getCustomAddress("Work");
            }
            else {
                address = workAddress;
                doNext(destinationType);
            }
        }

        else if (friendlyLocation.equals("Custom")) {
            if(customAddress.equals("")){
                address = getCustomAddress("Custom");
            }
            else {
                address = customAddress;
                doNext(destinationType);
            }
        }
    }

    private void doNext(String destinationType){
        final RadioButton googTransitButton = (RadioButton) findViewById(R.id.googTransitButton);
        final RadioButton googBikeButton = (RadioButton) findViewById(R.id.googBicycleButton);
        final RadioButton googDrivingButton = (RadioButton) findViewById(R.id.googDrivingButton);
        final RadioButton googWalkingButton = (RadioButton) findViewById(R.id.googWalkButton);

        //3. Find out what transportation method user wants
        GetTransportType(googTransitButton, googBikeButton, googDrivingButton, googWalkingButton);

        //4. Get directions from Google Maps
        getGoogleDirections(mode);

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

                Toast.makeText(getApplicationContext(), "Getting directions for address: " + address, Toast.LENGTH_LONG).show();
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

    private String getCustomAddress(final String destinationType) {
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

                //save address to shared preferences
                if (destinationType.equals("Home")){
                    savePrefs("home", address);
                }

                else if (destinationType.equals("Work")){
                    savePrefs("work", address);
                }

                doNext(destinationType);  //this line required to allow input of custom address BEFORE getting directions
            }
        });
        alertDialog.setNegativeButton("CANCEL", null);
        alertDialog.create().show();

        return address;
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

    //save prefs
    public void savePrefs(String key, String value){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    //get prefs
    public String loadPrefs(String key, String value){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        return sharedPreferences.getString(key, value);
    }
}
