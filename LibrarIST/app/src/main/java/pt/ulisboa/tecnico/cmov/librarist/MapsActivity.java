package pt.ulisboa.tecnico.cmov.librarist;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.media.audiofx.Virtualizer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.appengine.repackaged.com.google.common.collect.Maps;
import com.google.maps.GaeRequestHandler;
import com.google.maps.GeoApiContext;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import pt.ulisboa.tecnico.cmov.librarist.databinding.ActivityMapsBinding;

import android.Manifest;
import android.widget.SearchView;
import android.widget.Toast;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    private static final int RESULT_CANCEL = 1;
    private static final int RESULT_ADD_LIBRARY = 2;
    private static final int RESULT_ADD_BOOK = 3;
    private static final int RESULT_UPDATE_BOOK = 4;

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private ArrayList<Library> markerList = new ArrayList<Library>();
    private ArrayList<Book> bookList = new ArrayList<Book>();
    private boolean isPermissionGranted;
    Realm realm;
    RealmResults<Library> favoriteLibraries;



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Realm.init(getApplicationContext());
        realm = Realm.getDefaultInstance();

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
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);
        try {
            getMarkers();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }


        Button centerMapBtn = findViewById(R.id.centermapbtn);
        centerMapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                float Latitude = 38.7370f;
                float Longitude = -9.3027f;
                LatLng coordinate = new LatLng(Latitude, Longitude);
                CameraUpdate yourLocation = CameraUpdateFactory.newLatLngZoom(coordinate, 19.0f);
                mMap.animateCamera(yourLocation);
            }
        });


        Button favoritesBtn = findViewById(R.id.favoritesbtn);
        favoritesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MapsActivity.this, ViewFavoritesActivity.class);
                ArrayList<Library> favoritesList = new ArrayList<Library>();
                for (int i = 0; i < markerList.size(); i++) {
                    if(markerList.get(i).isFavorite()){
                        favoritesList.add(markerList.get(i));
                    }
                }
                intent.putExtra("LIBRARYLIST", favoritesList);
                startActivity(intent);
            }
        });


        Button allBooksBtn = findViewById(R.id.allbooksbtn);
        allBooksBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MapsActivity.this, ViewDonatedBooksActivity.class);
                i.putExtra("BOOKLIST", bookList);
                startActivity(i);
            }
        });


        Button addLibraryBtn = findViewById(R.id.addlibrarybtn);
        addLibraryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MapsActivity.this, AddLibraryActivity.class);
                i.putExtra("FILES_TO_SEND", markerList);
                startActivityForResult(i, 2);
            }
        });

        SearchView searchPlace = findViewById(R.id.searchplace);
        searchPlace.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                LatLng coordinate = getLocationFromAddress(MapsActivity.this, query);
                if(coordinate != null){
                    CameraUpdate yourLocation = CameraUpdateFactory.newLatLngZoom(coordinate, 19.0f);
                    mMap.animateCamera(yourLocation);
                    Toast.makeText(MapsActivity.this, "Now displaying: " + query, Toast.LENGTH_SHORT).show();
                    return true;
                }
                Toast.makeText(MapsActivity.this, "Location not found!", Toast.LENGTH_SHORT).show();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
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
                    Toast.makeText(MapsActivity.this, "User Canceled Dialogue", Toast.LENGTH_SHORT).show();
                }
            });
            assert dialog != null;
            dialog.show();
        }
        return false;
    }

    // Call Back method to get the Message form other Activity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // check if the request code is same as what is passed  here it is 2
        if(resultCode == RESULT_CANCEL){
            return;
        }
        if (resultCode == RESULT_ADD_LIBRARY) {
            super.onActivityResult(requestCode, resultCode, data);
            Library l = (Library) data.getSerializableExtra("LIBRARY");
            markerList.add(l);

            LatLng lLocation = new LatLng(l.getLat(), l.getLng());
            if(l.isFavorite()){
                mMap.addMarker(new MarkerOptions().position(lLocation).title(l.getName()).icon(BitmapDescriptorFactory.fromResource(R.drawable.favoritemarker)));
            }
            else{
                mMap.addMarker(new MarkerOptions().position(lLocation).title(l.getName()));
            }

            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lLocation, 15f));
        }
        if (resultCode == RESULT_ADD_BOOK) {
            super.onActivityResult(requestCode, resultCode, data);
            Book b = (Book) data.getSerializableExtra("BOOK");
            Library l = (Library) data.getSerializableExtra("LIBRARY");
            for (int i = 0; i < markerList.size(); i++) {
                if (Objects.equals(markerList.get(i).getName(), l.getName())){
                    markerList.get(i).addLibraryBook(b);
                    bookList.add(b);

                    Toast.makeText(MapsActivity.this, "Book added to library", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            Toast.makeText(MapsActivity.this, "Error :(", Toast.LENGTH_SHORT).show();
        }
        if (resultCode == RESULT_UPDATE_BOOK){
            super.onActivityResult(requestCode, resultCode, data);
            Book b = (Book) data.getSerializableExtra("BOOK");
            Library l = (Library) data.getSerializableExtra("LIBRARY");
            for (int i = 0; i < bookList.size(); i++) {
                if(Objects.equals(bookList.get(i).barcode, b.barcode)){
                    bookList.set(i, b);
                    break;
                }
            }
            for (int i = 0; i < l.getLibraryBooks().size(); i++) {
                if(Objects.equals(l.getLibraryBooks().get(i).getBarcode(), b.barcode)){
                    l.getLibraryBooks().set(i, b);
                }
            }
            for (int i = 0; i < markerList.size(); i++) {
                if(markerList.get(i).getName().equals(l.getName())){
                    markerList.set(i, l);
                }
            }
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(false);
        mMap.getUiSettings().setCompassEnabled(true);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);

        // Add a marker in Sydney and move the camera
        // LatLng sydney = new LatLng(-34, 151);
        // mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));

        LatLng currentLocation = new LatLng(38.7370, -9.3027);
        mMap.addMarker(new MarkerOptions().position(currentLocation).title("Your Location"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15f));

        for (int i = 0; i < markerList.size(); i++){
            Library l = markerList.get(i);
            LatLng newMarker = new LatLng(l.getLat(), l.getLng());
            if(l.isFavorite()){
                mMap.addMarker(new MarkerOptions().position(newMarker).title(l.getName()).icon(BitmapDescriptorFactory.fromResource(R.drawable.favoritemarker)));
            }
            else{
                mMap.addMarker(new MarkerOptions().position(newMarker).title(l.getName()));
            }
        }
        mMap.setOnMarkerClickListener(this);
    }

    public void getMarkers() throws IOException, InterruptedException {
        RequestHandler requestHandlerLib = new RequestHandler();

        requestHandlerLib.sendRequest("GET", "/library/getAll", null, null);

        String response = requestHandlerLib.getResponseData();
        while(response == null){
            response = requestHandlerLib.getResponseData();
        }

        Map<String, Object> map = null;
        if (!Objects.equals(response, "")) {
            ObjectMapper mapper = new ObjectMapper();
            map = mapper.readValue(response, Map.class);
            System.out.println("LIBRARIES MAP: " + map);
        }

        if(map != null) {
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                Map<String, Object> LibraryDict = (Map<String, Object>) entry.getValue();
                Library L = new Library(entry.getKey(), Double.parseDouble((String) Objects.requireNonNull(LibraryDict.get("latitude"))), Double.parseDouble((String) Objects.requireNonNull(LibraryDict.get("longitude"))), (String) LibraryDict.get("photoLocation"), Boolean.parseBoolean((String) LibraryDict.get("isFavorite")));
                markerList.add(L);
            }
        }

        RequestHandler requestHandlerBook = new RequestHandler();
        requestHandlerBook.sendRequest("GET", "/book/getAll", null, null);

        response = null;
        while(response == null){
            response = requestHandlerBook.getResponseData();
        }

        map = null;
        if (!Objects.equals(response, "")) {
            ObjectMapper mapper = new ObjectMapper();
            map = mapper.readValue(response, Map.class);
            System.out.println("BOOKS MAP: " + map);
        }

        if(map != null) {
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                for (int j = 0; j < ((ArrayList <Object>) entry.getValue()).size(); j++) {
                    Map<String, Object> BookDict = (Map<String, Object>) ((ArrayList<?>) entry.getValue()).get(j);
                    Book B = new Book(entry.getKey(), (String) BookDict.get("title"), (String) BookDict.get("cover"), Boolean.parseBoolean((String) BookDict.get("notifications")));
                    B.setQuantity(Integer.parseInt((String) Objects.requireNonNull(BookDict.get("quantity"))));
                    for (int i = 0; i < markerList.size(); i++) {
                        if(markerList.get(i).getName().equals(BookDict.get("library"))){
                            B.addToAvailableLibraries(markerList.get(i));
                            markerList.get(i).addLibraryBook(B);
                        }
                    }
                    bookList.add(B);
                }
            }
        }
    }

    public ArrayList<Library> getLibraries(){
        return markerList;
    }

    private void checkPermission(){
        Dexter.withContext(this).withPermission(Manifest.permission.ACCESS_FINE_LOCATION).withListener(new PermissionListener() {
            @Override
            public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                isPermissionGranted = true;
                Toast.makeText(MapsActivity.this, "Permission Granted", Toast.LENGTH_SHORT).show();
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

    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        boolean isLibrary = false;
        int i;
        for (i = 0; i < markerList.size(); i++) {
            if (Objects.equals(marker.getTitle(), markerList.get(i).getName())) {
                isLibrary = true;
                break;
            }
        }
        if (!isLibrary) {
            return false;
        }

        Intent intent = new Intent(MapsActivity.this, ViewLibraryActivity.class);
        intent.putExtra("LIBRARY", markerList.get(i));
        assert markerList.get(i) != null;
        startActivityForResult(intent, 3);
        return true;
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

    public static LatLng getCurrentPos(){
        return new LatLng(38.7370f, -9.3027f);
    }
}
