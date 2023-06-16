package pt.ulisboa.tecnico.cmov.librarist;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class ViewDonatedBooksActivity extends AppCompatActivity implements RecyclerViewInterface{
    private ArrayList<Book> booksList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_donated_books);

        booksList = (ArrayList<Book>) getIntent().getSerializableExtra("BOOKLIST");

        RecyclerView bookRecycler = (RecyclerView) findViewById(R.id.donatedbooksrecycler);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        bookRecycler.setHasFixedSize(true);

        // use a linear layout manager
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        bookRecycler.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)
        BookAdapter mAdapter = new BookAdapter(booksList, ViewDonatedBooksActivity.this, this);
        bookRecycler.setAdapter(mAdapter);

        SearchView searchBook = findViewById(R.id.searchbook);
        searchBook.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                ArrayList<Book> filteredBookList = new ArrayList<>();
                for (int i = 0; i < booksList.size(); i++) {
                    if(booksList.get(i).getTitle().toLowerCase().contains(query.toLowerCase()) || booksList.get(i).getBarcode().contains(query)){
                        filteredBookList.add(booksList.get(i));
                    }
                }
                BookAdapter mAdapter = new BookAdapter(filteredBookList, ViewDonatedBooksActivity.this, ViewDonatedBooksActivity.this);
                bookRecycler.setAdapter(mAdapter);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                ArrayList<Book> filteredBookList = new ArrayList<>();
                for (int i = 0; i < booksList.size(); i++) {
                    if(booksList.get(i).getTitle().toLowerCase().contains(newText.toLowerCase()) || booksList.get(i).getBarcode().contains(newText)){
                        filteredBookList.add(booksList.get(i));
                    }
                }
                BookAdapter mAdapter = new BookAdapter(filteredBookList, ViewDonatedBooksActivity.this, ViewDonatedBooksActivity.this);
                bookRecycler.setAdapter(mAdapter);
                return false;
            }
        });
    }

    @Override
    public void onItemClick(int position) {
        Intent intent = new Intent (ViewDonatedBooksActivity.this, ViewBookActivity.class);
        Book b = booksList.get(position);
        intent.putExtra("BOOK", b);
        ViewDonatedBooksActivity.this.startActivity(intent);
    }
}