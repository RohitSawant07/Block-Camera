package rohit.com.blockcamera;

import android.Manifest;
import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.math.RoundingMode;
import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity {
    public static MainActivity staticMainActivity;
    private ComponentName admin;
    private Switch blockCamera;
    private DevicePolicyManager mDPM;
    private final int DeviceAdminRequestCode = 0;

    private Button scanButton;
    private Button btnGetLocation;
    private Button btnUnlock;
    private TextView txtLocation;
    private ProgressBar progressBar;
    private LocationManager locationManager;
    private LocationListener listener;

    private EditText officeLat;
    private EditText officeLong;

    double longitude = 0;
    double latitude = 0;

    double officeLongitude = 0;
    double officeLatitude = 0;

    boolean isMock = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeView();
        staticMainActivity = this;
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        mDPM = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        admin = new ComponentName(getApplicationContext(), DevAdminReceiver.class);
        final DevicePolicyManager mDPM =
                (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);

        if (mDPM.getCameraDisabled(admin)) {
            blockCamera.setChecked(true);
        }

        //-------------------------------------------------------------------------------//
        final Activity activity = this;
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                IntentIntegrator intentIntegrator = new IntentIntegrator(activity);
                intentIntegrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
                intentIntegrator.setPrompt("FNFIS");
                intentIntegrator.setCameraId(0);
                intentIntegrator.setBeepEnabled(true);
                intentIntegrator.setBarcodeImageEnabled(false);
                intentIntegrator.setOrientationLocked(false);
                intentIntegrator.initiateScan();
            }
        });
        //-------------------------------------------------------------------------------//
        blockCamera.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    blockCamera();
                } else {
                    unblockCamera();
                }
            }
        });
        //------------------------------------------------------------------------------//
        listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if (location.isFromMockProvider()) {
                    Toast.makeText(getApplicationContext(), "It's Mock GPS", Toast.LENGTH_SHORT).show();
                    isMock = true;
                } else {
                    isMock = false;
                }
                progressBar.setVisibility(View.GONE);
                txtLocation.setText(String.valueOf(location.getLatitude()) + " " + String.valueOf(location.getLongitude()));
                latitude = location.getLatitude();
                longitude = location.getLongitude();
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {
                Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(i);
            }
        };

        btnUnlock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mDPM.getCameraDisabled(admin)) {
                    Toast.makeText(getApplicationContext(), "Camera Already Open", Toast.LENGTH_SHORT).show();
                } else {
//                    officeLatitude = Double.valueOf(String.valueOf(officeLat.getText()));
//                    officeLongitude = Double.valueOf(String.valueOf(officeLong.getText()));

                    officeLatitude = 18.564081;
                    officeLongitude = 73.807071;

                    if (latitude == 0 || longitude == 0) {
                        Toast.makeText(getApplicationContext(), "Waiting For Location", Toast.LENGTH_SHORT).show();
                    }
                    if (mDPM.getCameraDisabled(admin)) {
                        if (isMock) {
                            Toast.makeText(getApplicationContext(), "Mock GPS Enabled", Toast.LENGTH_LONG).show();
                        } else {
                            geoUnblock();
                        }
                    }
                }
            }
        });

        btnGetLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDPM.getCameraDisabled(admin)) {
                    txtLocation.setText("");
                    progressBar.setVisibility(View.VISIBLE);
                    if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        progressBar.setVisibility(View.GONE);
                        return;
                    }
                    locationManager.requestSingleUpdate("gps", listener, null);
                }
            }
        });
    }

    private void initializeView() {
        blockCamera = findViewById(R.id.BlockCamera);
        scanButton = findViewById(R.id.btnScan);

        txtLocation = findViewById(R.id.txtLocation);
        btnUnlock = findViewById(R.id.btnLocation);

        officeLat = findViewById(R.id.etOfficeLat);
        officeLong = findViewById(R.id.etOfficeLong);

        btnGetLocation = findViewById(R.id.btnGetLocation);

        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
    }

    private void blockCamera() {
        if (mDPM.isAdminActive(admin)) {
            mDPM.setCameraDisabled(admin, true);
            blockCamera.setChecked(true);
        } else {
            blockCamera.setChecked(false);
            activateAdmin();
        }
    }

    private void unblockCamera() {
        if(mDPM.isAdminActive(admin)){
            mDPM.setCameraDisabled(admin, false);
            blockCamera.setChecked(false);
        } else {
            blockCamera.setChecked(true);
            activateAdmin();
        }
    }

    private void activateAdmin() {
        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, admin);
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Make sure to accept in order to provide support for lock and wipe");
        startActivityForResult(intent, DeviceAdminRequestCode);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case DeviceAdminRequestCode:
                if (mDPM.isAdminActive(admin)) {
                    deviceAdminOn();
                }
            break;
        }
        if (requestCode != 0) {
            IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            if (result != null) {
                if (result.getContents() == null) {
                    Toast.makeText(this, "You Cancelled the Scanning", Toast.LENGTH_SHORT).show();
                } else {
                    blockCamera(result.getContents());
                }
            }
            else {
                super.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    private void blockCamera(String content) {
        if (content.trim().equals("FIS1234")) {
            blockCamera();
        }
    }

    private void deviceAdminOn() {
        mDPM.setCameraDisabled(admin, true);
        blockCamera.setChecked(true);
    }

    @Override
    public void onBackPressed() {
        Toast.makeText(getApplicationContext(), "Press the home or the recent apps button to exit.", Toast.LENGTH_SHORT).show();
    }

    private void geoUnblock() {
        if (latitude == 0 || longitude == 0 || officeLatitude == 0 || officeLongitude == 0 ) {
            //do Nothing
        } else {
            Location officeLocation = new Location("Office Location");
            officeLocation.setLatitude(officeLatitude);
            officeLocation.setLongitude(officeLongitude);

            Location currentLocation = new Location("Current Location");
            currentLocation.setLatitude(latitude);
            currentLocation.setLongitude(longitude);

            float distance = officeLocation.distanceTo(currentLocation);

            DecimalFormat df = new DecimalFormat("#.##");
            df.setRoundingMode(RoundingMode.CEILING);
            if (distance > 1000) {
                unblockCamera();
                Toast.makeText(getApplicationContext(), "Camera Unblocked, Distance is: " + String.valueOf(df.format(distance * 0.001)) + " " + "Km, Which is greater than 1km", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getApplicationContext(), "Still in Office", Toast.LENGTH_LONG).show();
            }
        }
    }
}
