package pt.ulisboa.tecnico.cmov.librarist;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.ArrayList;

import pt.ulisboa.tecnico.cmov.librarist.databinding.ActivityPickLocationOnMapBinding;

public class PickLocationOnMap extends FragmentActivity implements OnMapReadyCallback {

    GoogleMap mMap;
    boolean isPermissionGranted;
    private ActivityPickLocationOnMapBinding binding;

    ArrayList<Marker> mMarkerList = new ArrayList<Marker>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityPickLocationOnMapBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        checkPermission();
        if (isPermissionGranted) {
            if (checkGooglePlayServices()) {
                Toast.makeText(this, "Google Play Services Available", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Google Play Services Unavailable", Toast.LENGTH_SHORT).show();
            }
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragmentmap);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(false);
        mMap.getUiSettings().setCompassEnabled(true);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);
        LatLng currentLocation = new LatLng(38.7370, -9.3027);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15f));

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener()
        {
            @Override
            public void onMapClick(@NonNull LatLng arg0)
            {
                googleMap.clear();
                mMarkerList.clear();

                android.util.Log.i("onMapClick", "Horray!");

                Marker mMarker = mMap.addMarker(new MarkerOptions().position(arg0).title("New Library"));
                mMarkerList.add(mMarker);
                CameraUpdate yourLocation = CameraUpdateFactory.newLatLngZoom(arg0, 19.0f);
                mMap.animateCamera(yourLocation);

            }
        });

        Button cancelBtn = findViewById(R.id.cancelbutton);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                googleMap.clear();
                finish();
            }
        });

        Button confirmBtn = findViewById(R.id.confirmbutton);
        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("LAT", mMarkerList.get(0).getPosition().latitude);
                intent.putExtra("LNG", mMarkerList.get(0).getPosition().longitude);
                setResult(2, intent);
                Toast.makeText(getApplicationContext(), "Adding Library...", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private boolean checkGooglePlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int result = apiAvailability.isGooglePlayServicesAvailable(this);
        if (result == ConnectionResult.SUCCESS) {
            return true;
        } else if (apiAvailability.isUserResolvableError(result)) {
            Dialog dialog = apiAvailability.getErrorDialog(this, result, 201, new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    Toast.makeText(PickLocationOnMap.this, "User Canceled Dialogue", Toast.LENGTH_SHORT).show();
                }
            });
            assert dialog != null;
            dialog.show();
        }
        return false;
    }

    private void checkPermission(){
        Dexter.withContext(this).withPermission(Manifest.permission.ACCESS_FINE_LOCATION).withListener(new PermissionListener() {
            @Override
            public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                isPermissionGranted = true;
                Toast.makeText(PickLocationOnMap.this, "Permission Granted", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri= Uri.fromParts("package", getPackageName(), "");
                intent.setData(uri);
                startActivity(intent);
            }

            @Override
            public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                permissionToken.continuePermissionRequest();
            }
        }).check();
    }
}