package pt.ulisboa.tecnico.cmov.librarist;

import android.graphics.Bitmap;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

import io.realm.RealmList;
import io.realm.RealmObject;

public class Library implements Serializable{
    private String name;
    private double lat;
    private double lng;
    private boolean isFavorite;
    private String photoURL;
    private ArrayList<Book> libraryBooks;

    public Library(){

    }

    public Library(String name, double lat, double lng, String photoURL){
        this.name = name;
        this.lat = lat;
        this.lng = lng;
        isFavorite = false;
        this.photoURL = photoURL;
        libraryBooks = new ArrayList<>();
    }

    public Library(String name, double lat, double lng, String photoURL, boolean isFavorite){
        this.name = name;
        this.lat = lat;
        this.lng = lng;
        this.isFavorite = isFavorite;
        this.photoURL = photoURL;
        libraryBooks = new ArrayList<>();
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    public String getPhotoURL() {
        return photoURL;
    }

    public void setPhotoURL(String photoURL) {
        this.photoURL = photoURL;
    }

    public ArrayList<Book> getLibraryBooks() {
        return libraryBooks;
    }

    public void setLibraryBooks(ArrayList<Book> libraryBooks) {
        this.libraryBooks = libraryBooks;
    }

    public void addLibraryBook(Book book){
        libraryBooks.add(book);
    }


}
