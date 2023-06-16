package pt.ulisboa.tecnico.cmov.librarist;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class LibraryAdapter extends RecyclerView.Adapter<LibraryAdapter.ViewHolder> {
    private ArrayList<Library> mDataset;
    private final RecyclerViewInterface recyclerViewInterface;
    private Context context;
    private LatLng currentpos;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView libraryTitle;
        public TextView libraryLocation;
        public TextView footer;

        public ViewHolder(ViewGroup viewGroup, RecyclerViewInterface recyclerViewInterface) {
            super(viewGroup);
            libraryTitle = viewGroup.findViewById(R.id.librarytitle);
            libraryLocation = viewGroup.findViewById(R.id.librarylocation);
            footer = viewGroup.findViewById(R.id.footer);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(recyclerViewInterface != null){
                        int pos = getAdapterPosition();

                        if(pos != RecyclerView.NO_POSITION){
                            recyclerViewInterface.onItemClick(pos);
                        }
                    }
                }
            });
        }
    }
    public LibraryAdapter(ArrayList<Library> myDataset, Context context, RecyclerViewInterface recyclerViewInterface, LatLng currentpos) {
        mDataset = myDataset;
        this.context = context;
        this.recyclerViewInterface = recyclerViewInterface;
        this.currentpos = currentpos;

        Collections.sort(mDataset, new Comparator<Library>() {

            public int compare(Library l1, Library l2) {
                // compare two instance of `Score` and return `int` as result.
                return (int) (distance(l1.getLat(), currentpos.latitude, l1.getLng(), currentpos.longitude)-(distance(l2.getLat(), currentpos.latitude, l2.getLng(), currentpos.longitude)));
            }
        });
    }

    @NonNull
    @Override
    public LibraryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // create a new view
        ViewGroup v = (ViewGroup) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.library_item_view, parent, false);
        // set the view's size, margins, paddings and layout parameters

        return new ViewHolder(v, recyclerViewInterface);
    }

    @Override
    public void onBindViewHolder(@NonNull LibraryAdapter.ViewHolder holder, int position) {
        Library l = mDataset.get(position);
        holder.libraryTitle.setText(mDataset.get(position).getName());
        String location;
        try {
            location = getAddressFromLocation(mDataset.get(position).getLat(),mDataset.get(position).getLng());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if(location != null){
            holder.libraryLocation.setText(location);
            DecimalFormat df = new DecimalFormat("#.#");
            df.setRoundingMode(RoundingMode.CEILING);
            String lDistance = df.format(distance(mDataset.get(position).getLat(), currentpos.latitude, mDataset.get(position).getLng(), currentpos.longitude)) + " meters";
            holder.footer.setText(lDistance);
            return;
        }
        holder.libraryLocation.setText(R.string.location_not_found);
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }


    private String getAddressFromLocation(double lat, double lng) throws IOException {
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(context, Locale.getDefault());

        addresses = geocoder.getFromLocation(lat, lng, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5

        if(addresses != null){
            return addresses.get(0).getAddressLine(0);
        }
        return null;
    }

    public static double distance(double lat1, double lat2, double lon1,
                                  double lon2) {

        final int R = 6371; // Radius of the earth

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters


        distance = Math.pow(distance, 2);

        return Math.sqrt(distance);
    }

}
