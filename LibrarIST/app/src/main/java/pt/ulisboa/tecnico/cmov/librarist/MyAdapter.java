package pt.ulisboa.tecnico.cmov.librarist;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import io.realm.RealmResults;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
    Context context;
    RealmResults<Library> libraryList;

    public MyAdapter(Context context, RealmResults<Library> lList) {
        this.context = context;
        this.libraryList = lList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.library_item_view,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyAdapter.MyViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        TextView titleOutput;
        TextView descriptionOutput;
        TextView timeOutput;

        public MyViewHolder(@NonNull View libraryItemView) {
            super(libraryItemView);
            titleOutput = itemView.findViewById(R.id.librarytitle);
            descriptionOutput = itemView.findViewById(R.id.librarylocation);
            timeOutput = itemView.findViewById(R.id.footer);
        }
    }
}
