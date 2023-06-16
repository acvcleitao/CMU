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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.ViewHolder> {
    private ArrayList<Book> mDataset;
    private final RecyclerViewInterface recyclerViewInterface;
    private Context context;
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
    public BookAdapter(ArrayList<Book> myDataset, Context context, RecyclerViewInterface recyclerViewInterface) {
        mDataset = myDataset;
        this.context = context;
        this.recyclerViewInterface = recyclerViewInterface;

    }

    @NonNull
    @Override
    public BookAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // create a new view
        ViewGroup v = (ViewGroup) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.library_item_view, parent, false);
        // set the view's size, margins, paddings and layout parameters

        return new ViewHolder(v, recyclerViewInterface);
    }

    @Override
    public void onBindViewHolder(@NonNull BookAdapter.ViewHolder holder, int position) {
        Book b = mDataset.get(position);
        holder.libraryTitle.setText(b.getTitle());
        String barcodeString = "barcode: " + b.getBarcode();
        holder.libraryLocation.setText(barcodeString);
        String quantity = String.valueOf(b.getQuantity());
        holder.footer.setText(quantity);
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}
