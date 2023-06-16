package pt.ulisboa.tecnico.cmov.librarist;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class ViewBookActivity extends AppCompatActivity implements RecyclerViewInterface{
    private ArrayList<Library> libraryList;
    Book myBook;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_book);
        myBook = (Book) getIntent().getSerializableExtra("BOOK");
        libraryList = myBook.getAvailableLibraries();
        TextView bookTitle = findViewById(R.id.title);
        TextView bookbarcode = findViewById(R.id.barcode);
        ImageView bookCover = findViewById(R.id.bookcover);

        bookTitle.setText(myBook.getTitle());
        bookbarcode.setText(myBook.getBarcode());

        // get Cover image from local storage
        String filename = myBook.getCover();
        filename = filename.replaceAll("\\s", "");

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bmp = BitmapFactory.decodeFile(filename, options);

        bookCover.setImageBitmap(bmp);

        RecyclerView libraryRecycler = (RecyclerView) findViewById(R.id.libraryrecycler);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        libraryRecycler.setHasFixedSize(true);

        // use a linear layout manager
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        libraryRecycler.setLayoutManager(mLayoutManager);

        LatLng currentpos = MapsActivity.getCurrentPos();

        LibraryAdapter mAdapter = new LibraryAdapter(libraryList, ViewBookActivity.this, this, currentpos);
        libraryRecycler.setAdapter(mAdapter);
    }

    @Override
    public void onItemClick(int position) {
        Intent intent = new Intent (ViewBookActivity.this, ViewLibraryActivity.class);
        Library l = libraryList.get(position);
        intent.putExtra("LIBRARY", l);
        ViewBookActivity.this.startActivity(intent);
    }
}