package pt.ulisboa.tecnico.cmov.librarist;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.Sort;

public class ViewFavoritesActivity extends AppCompatActivity implements RecyclerViewInterface{
    private ArrayList<Library> favoritesList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_favorites);

        favoritesList = (ArrayList<Library>) getIntent().getSerializableExtra("LIBRARYLIST");

        RecyclerView libraryRecycler = (RecyclerView) findViewById(R.id.libraryrecycler);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        libraryRecycler.setHasFixedSize(true);

        // use a linear layout manager
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        libraryRecycler.setLayoutManager(mLayoutManager);

        LatLng currentpos = MapsActivity.getCurrentPos();

        LibraryAdapter mAdapter = new LibraryAdapter(favoritesList, ViewFavoritesActivity.this, this, currentpos);
        libraryRecycler.setAdapter(mAdapter);
    }

    @Override
    public void onItemClick(int position) {
        Intent intent = new Intent (ViewFavoritesActivity.this, ViewLibraryActivity.class);
        Library l = favoritesList.get(position);
        intent.putExtra("LIBRARY", l);
        ViewFavoritesActivity.this.startActivity(intent);
    }
}