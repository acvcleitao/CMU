package pt.ulisboa.tecnico.cmov.librarist;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
public class ViewLibraryActivity extends AppCompatActivity implements OnMapReadyCallback, RecyclerViewInterface, GoogleMap.OnMarkerClickListener{

    Library l;
    private PopupWindow POPUP_WINDOW_SCORE = null;
    public static final String SERVER_ADDR = "http://192.92.147.100:5000";
    RequestHandler requestHandler = new RequestHandler();

    ArrayList<Book> booksList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_library);


        l = (Library) getIntent().getSerializableExtra("LIBRARY");
        booksList = l.getLibraryBooks();
        ImageView libraryImage = findViewById(R.id.libraryimage);

        String filename = l.getPhotoURL();
        filename = filename.replaceAll("\\s", "");
        Toast.makeText(ViewLibraryActivity.this, filename, Toast.LENGTH_LONG).show();
        System.out.println(filename);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bmp = BitmapFactory.decodeFile(filename, options);

        libraryImage.setImageBitmap(bmp);

        RecyclerView bookRecycler = (RecyclerView) findViewById(R.id.booklist);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        bookRecycler.setHasFixedSize(true);

        // use a linear layout manager
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        bookRecycler.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)
        BookAdapter mAdapter = new BookAdapter(booksList, ViewLibraryActivity.this, this);
        bookRecycler.setAdapter(mAdapter);

        CheckBox notifications = findViewById(R.id.favoritecheckbox);
        notifications.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                l.setFavorite(!l.isFavorite());
            }
        });

        ImageButton navigateBtn = findViewById(R.id.navigatebutton);
        navigateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        Button donateBookBtn = findViewById(R.id.donatedbookbtn);
        donateBookBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowDonateBookPopup();
            }
        });

        Button retrieveBookBtn = findViewById(R.id.retrievebookbtn);
        retrieveBookBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowRetrieveBookPopup();
            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapfragment);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

    }

    // Call Back method to get the Message form other Activity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == 3) {
            Book b = (Book) data.getSerializableExtra("BOOK");
            b.addToAvailableLibraries(l);
            l.addLibraryBook(b);
            ArrayList<String> paramNames = new ArrayList<>();
            ArrayList<String> params = new ArrayList<>();

            paramNames.add("title");
            params.add(b.getTitle());
            paramNames.add("barcode");
            params.add(b.getBarcode());
            paramNames.add("cover");
            params.add(b.getCover());
            paramNames.add("library");
            params.add(l.getName());
            paramNames.add("notifications");
            params.add(String.valueOf(b.isNotifications()));

            requestHandler.sendRequest("POST", "/book/create", paramNames, params);
            Intent intent = new Intent();
            intent.putExtra("BOOK", b);
            intent.putExtra("LIBRARY", l);
            setResult(3, intent);
            String bName = ((Book) data.getSerializableExtra("BOOK")).getBarcode();
            Toast.makeText(ViewLibraryActivity.this, "Added book " + bName + " to library " + l.getName(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        LatLng coordinate = new LatLng(l.getLat(), l.getLng());
        CameraUpdateFactory.newLatLngZoom(coordinate, 15f);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(coordinate, 15f));

        if(l.isFavorite()){
            googleMap.addMarker(new MarkerOptions().position(coordinate).title(l.getName()).icon(BitmapDescriptorFactory.fromResource(R.drawable.favoritemarker)));
        }
        else{
            googleMap.addMarker(new MarkerOptions().position(coordinate).title(l.getName()));
        }
        googleMap.getUiSettings().setScrollGesturesEnabled(false);
        googleMap.getUiSettings().setZoomGesturesEnabled(false);
        googleMap.getUiSettings().setScrollGesturesEnabledDuringRotateOrZoom(false);
    }


    private void ShowDonateBookPopup()
    {
        // Inflate the popup_layout.xml
        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = layoutInflater.inflate(R.layout.donate_book_popup, null);

        // Creating the PopupWindow
        POPUP_WINDOW_SCORE = new PopupWindow(this);
        POPUP_WINDOW_SCORE.setContentView(layout);
        POPUP_WINDOW_SCORE.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        POPUP_WINDOW_SCORE.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        POPUP_WINDOW_SCORE.setFocusable(true);

        // prevent clickable background
        POPUP_WINDOW_SCORE.setBackgroundDrawable(null);

        POPUP_WINDOW_SCORE.showAtLocation(layout, Gravity.CENTER, 1, 1);

        EditText bookBarcode = (TextInputEditText) layout.findViewById(R.id.bookbarcode);

        // Getting a reference to button one and do something
        Button donateBookBtn = (Button) layout.findViewById(R.id.donatedbookbtn);
        donateBookBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                for (int i = 0; i < l.getLibraryBooks().size(); i++) {
                    if(l.getLibraryBooks().get(i).getBarcode().equals(bookBarcode.getText().toString())){
                        l.getLibraryBooks().get(i).addQuantity();

                        ArrayList<String> paramNames = new ArrayList<>();
                        ArrayList<String> params = new ArrayList<>();

                        paramNames.add("library");
                        params.add(l.getName());
                        paramNames.add("barcode");
                        params.add(l.getLibraryBooks().get(i).getBarcode());

                        requestHandler.sendRequest("POST", "/book/donate", paramNames, params);

                        Intent intent = new Intent();
                        intent.putExtra("BOOK", l.getLibraryBooks().get(i));
                        intent.putExtra("LIBRARY", l);
                        setResult(4, intent);
                        POPUP_WINDOW_SCORE.dismiss();
                        finish();
                        return;
                    }
                }
                // Create new book if barcode is not found
                Intent intent = new Intent(ViewLibraryActivity.this, AddBookActivity.class);
                intent.putExtra("BARCODE", bookBarcode.getText().toString());
                startActivityForResult(intent, 3);
                POPUP_WINDOW_SCORE.dismiss();
            }
        });
    }

    private void ShowRetrieveBookPopup()
    {
        // Inflate the popup_layout.xml
        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = layoutInflater.inflate(R.layout.retrieve_book_popup, null);

        // Creating the PopupWindow
        POPUP_WINDOW_SCORE = new PopupWindow(this);
        POPUP_WINDOW_SCORE.setContentView(layout);
        POPUP_WINDOW_SCORE.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        POPUP_WINDOW_SCORE.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        POPUP_WINDOW_SCORE.setFocusable(true);

        // prevent clickable background
        POPUP_WINDOW_SCORE.setBackgroundDrawable(null);

        POPUP_WINDOW_SCORE.showAtLocation(layout, Gravity.CENTER, 1, 1);

        EditText bookBarcode = (TextInputEditText) layout.findViewById(R.id.bookbarcode);

        // Getting a reference to button one and do something
        Button retrieveBookBtn = (Button) layout.findViewById(R.id.retrievebookbtn);
        retrieveBookBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                int i;
                for (i = 0; i < l.getLibraryBooks().size(); i++){
                    if(l.getLibraryBooks().get(i).getBarcode().equals(bookBarcode.getText().toString())){

                        if(l.getLibraryBooks().get(i).getQuantity() > 0){
                            Toast.makeText(ViewLibraryActivity.this, "Book "+ l.getLibraryBooks().get(i).getTitle() + " retrieved!", Toast.LENGTH_LONG).show();
                            l.getLibraryBooks().get(i).removeQuantity();

                            ArrayList<String> paramNames = new ArrayList<>();
                            ArrayList<String> params = new ArrayList<>();

                            paramNames.add("library");
                            params.add(l.getName());
                            paramNames.add("barcode");
                            params.add(l.getLibraryBooks().get(i).getBarcode());

                            requestHandler.sendRequest("POST", "/book/retrieve", paramNames, params);
                            Intent intent = new Intent();
                            intent.putExtra("BOOK", l.getLibraryBooks().get(i));
                            intent.putExtra("LIBRARY", l);
                            setResult(4, intent);
                            POPUP_WINDOW_SCORE.dismiss();
                            finish();
                            return;
                        }
                        else{
                            Toast.makeText(ViewLibraryActivity.this, "Book " + l.getLibraryBooks().get(i).getTitle() + " has no copies left!", Toast.LENGTH_LONG).show();
                        }
                        POPUP_WINDOW_SCORE.dismiss();
                        return;
                    }
                }
                Toast.makeText(ViewLibraryActivity.this, "No book with barcode " + bookBarcode.getText().toString() + " available at this library :(", Toast.LENGTH_LONG).show();
                //Close Window
                POPUP_WINDOW_SCORE.dismiss();
            }
        });
    }

    @Override
    public void onItemClick(int position) {
        Intent intent = new Intent (ViewLibraryActivity.this, ViewBookActivity.class);
        Book b = booksList.get(position);
        intent.putExtra("BOOK", b);
        ViewLibraryActivity.this.startActivity(intent);
    }

    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        return true;
    }
}