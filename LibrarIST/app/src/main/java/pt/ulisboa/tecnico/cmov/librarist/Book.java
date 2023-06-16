package pt.ulisboa.tecnico.cmov.librarist;

import java.io.Serializable;
import java.util.ArrayList;

import io.realm.RealmList;
import io.realm.RealmObject;

public class Book implements Serializable {
    String barcode;
    String title;
    String cover;
    String description;
    int quantity;
    boolean notifications;
    ArrayList<Library> availableLibraries;


    public Book(String barcode, String title, String cover, String description, boolean notifications) {
        this.barcode = barcode;
        this.title = title;
        this.cover = cover;
        this.description = description;
        this.notifications = notifications;
        availableLibraries = new ArrayList<>();
    }

    public Book(String barcode, String title, String cover, boolean notifications) {
        this.barcode = barcode;
        this.title = title;
        this.cover = cover;
        this.notifications = notifications;
        availableLibraries = new ArrayList<>();
        this.quantity = 1;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
    public void addQuantity(){quantity += 1;}
    public void addQuantity(int quantity){this.quantity += quantity;}
    public boolean removeQuantity(){
        if(quantity > 0){
            quantity -= 1;
            return true;
        }
        return false;
    }
    public boolean removeQuantity(int quantity){
        if(this.quantity > quantity){
            this.quantity -= quantity;
            return true;
        }
        return false;
    }

    public boolean isNotifications() {
        return notifications;
    }

    public void setNotifications(boolean notifications) {
        this.notifications = notifications;
    }

    public ArrayList<Library> getAvailableLibraries() {
        return availableLibraries;
    }

    public void setAvailableLibraries(ArrayList<Library> availableLibraries) {
        this.availableLibraries = availableLibraries;
    }

    public void addToAvailableLibraries(Library l){
        this.availableLibraries.add(l);
    }
}
