package pt.ulisboa.tecnico.cmov.librarist;

import static com.google.android.gms.common.GooglePlayServicesUtilLight.isGooglePlayServicesAvailable;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import pt.ulisboa.tecnico.cmov.librarist.databinding.ActivityAddLibraryBinding;
import pt.ulisboa.tecnico.cmov.librarist.databinding.ActivityMainBinding;

public class AddLibraryActivity extends FragmentActivity implements OnMapReadyCallback {
    ActivityResultLauncher<String> libraryPhoto;
    ActivityAddLibraryBinding binding;
    boolean isFavorite;
    GoogleMap mMap;
    EditText libraryLocation;
    LatLng latlng = null;

    RequestHandler requestHandler = new RequestHandler();


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        ArrayList<Library> librariesList = (ArrayList<Library>) getIntent().getSerializableExtra("FILES_TO_SEND");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_library);
        binding = ActivityAddLibraryBinding.inflate(getLayoutInflater());

        isFavorite = false;
        EditText libraryName = findViewById(R.id.libraryname);
        libraryLocation = findViewById(R.id.librarylocation);

        libraryLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        ImageView libraryImage = findViewById(R.id.libraryimage);
        libraryPhoto = registerForActivityResult(
                new ActivityResultContracts.GetContent(), new ActivityResultCallback<Uri>() {
                    @Override
                    public void onActivityResult(Uri result) {
                        libraryImage.setImageURI(result);
                    }
                }
        );

        Button addLibraryImageBtn = findViewById(R.id.addlibraryimagebtn);
        addLibraryImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                libraryPhoto.launch("image/*");
            }
        });

        Button addLibraryBtn = findViewById(R.id.addlibrarybutton);
        addLibraryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: Add new library
                String lName = libraryName.getText().toString();
                String lLocation = libraryLocation.getText().toString();
                LatLng p;

                if(latlng == null){
                    p = getLocationFromAddress(AddLibraryActivity.this, lLocation);
                }
                else {
                    p = latlng;
                }


                Bitmap bitmap = drawableToBitmap(libraryImage.getDrawable());

                try {
                    writeBitmapToFile(lName.replaceAll("\\s", "") + ".bmp", bitmap);
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }
                String file = lName;
                String filename = getApplicationContext().getFilesDir().toString() + "/" + file.replaceAll("\\s", "") + ".bmp";

                Library l = new Library(lName, p.latitude, p.longitude, filename, isFavorite);

                ArrayList<String> paramNames = new ArrayList<>();
                ArrayList<String> params = new ArrayList<>();

                paramNames.add("name");
                params.add(lName);
                paramNames.add("latitude");
                params.add(String.valueOf(p.latitude));
                paramNames.add("longitude");
                params.add(String.valueOf(p.longitude));
                paramNames.add("isFavorite");
                params.add(String.valueOf(isFavorite));
                paramNames.add("photoLocation");
                params.add(filename);

                requestHandler.sendRequest("POST", "/library/create", paramNames, params);
                Intent intent = new Intent();
                intent.putExtra("LIBRARY", l);
                setResult(2, intent);
                Toast.makeText(getApplicationContext(), "Adding Library...", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        Button cancelBtn = findViewById(R.id.cancelbutton);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                setResult(1, intent);
                finish();
            }
        });

        Button favoritesBtn = findViewById(R.id.favoritesbtn);
        favoritesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isFavorite = !isFavorite;
            }
        });

        ImageButton picklocationbtn = findViewById(R.id.picklocation);
        picklocationbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(AddLibraryActivity.this, PickLocationOnMap.class), 2);
            }
        });
    }

    private void writeBitmapToFile(String fileName, Bitmap content) throws FileNotFoundException {
        File path = getApplicationContext().getFilesDir();
        try{
            FileOutputStream writer = new FileOutputStream(new File(path, fileName));
            content.compress(Bitmap.CompressFormat.PNG,100,writer);
            Toast.makeText(this, "Wrote to file: " + fileName, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public LatLng getLocationFromAddress(Context context, String strAddress) {

        Geocoder coder = new Geocoder(context, Locale.getDefault());
        List<Address> address;
        LatLng p1 = null;
        try {
            // May throw an IOException
            address = coder.getFromLocationName(strAddress, 5);
            if (address == null || address.size() == 0) {
                return null;
            }
            Address location = address.get(0);
            p1 = new LatLng(location.getLatitude(), location.getLongitude());

        } catch (IOException ex) {

            ex.printStackTrace();
        }

        return p1;
    }
    private String getAddressFromLocation(double lat, double lng) throws IOException {
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());

        addresses = geocoder.getFromLocation(lat, lng, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5

        if(addresses != null){
            return addresses.get(0).getAddressLine(0);
        }
        return null;
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
    }
    // Call Back method to get the Message form other Activity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // check if the request code is same as what is passed  here it is 2
        if (requestCode == 2) {
            double lat = (double) data.getSerializableExtra("LAT");
            double lng = (double) data.getSerializableExtra("LNG");
            latlng = new LatLng(lat, lng);
            try {
                libraryLocation.setText(getAddressFromLocation(lat, lng));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static Bitmap drawableToBitmap (Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if(bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if(drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }
}
