package com.example.notesapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.example.notesapp.R;
import com.example.notesapp.model.NotesModel;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;

public class RecyclerViewAdapter  extends  RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>{
    private Context context;
    private ArrayList<NotesModel> mItems;
    private ArrayList<NotesModel.NotesDetails> mDetailItems;
    private NotesModel.NotesDetails model;

    public RecyclerViewAdapter(Context context, ArrayList<NotesModel.NotesDetails> items) {
        this.context = context;
        this.mDetailItems = items;

    }

    @Override
    public ViewHolder onCreateViewHolder( ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notes, parent, false);
        ViewHolder viewHolder = new ViewHolder(v);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(mDetailItems.get(position));

    }

    @Override
    public int getItemCount() {
        return mDetailItems.size();
    }

    public void setFilter(ArrayList<NotesModel.NotesDetails> filteredModelList) {
        mDetailItems = new ArrayList<>();
        mDetailItems.addAll(filteredModelList);
        notifyDataSetChanged();
    }

    public class ViewHolder  extends RecyclerView.ViewHolder{
        private MaterialTextView mTags,mContent,mAuthor;
        private MaterialCardView mCardView;

        private ViewHolder(View itemView) {
            super(itemView);

            mTags=itemView.findViewById(R.id.tags);
            mContent=itemView.findViewById(R.id.content);
            mAuthor=itemView.findViewById(R.id.author);
            mCardView=itemView.findViewById(R.id.card_view);



          /* mCardView.setOnLongClickListener(new View.OnLongClickListener() {
               @Override
               public boolean onLongClick(View view) {
                   mCardView.setChecked(!mCardView.isChecked());
                   return true;
               }
           });*/


        }

        private void bind(NotesModel.NotesDetails data){
            String text="";
           ArrayList<String> arrayList= data.getTags();

               for (int i = 0; i < arrayList.size(); i++) {
                   if(arrayList.size()>1) {
                       text += arrayList.get(i) + ",";
                   }else{
                       text=arrayList.get(i);
                   }
               }

            mTags.setText(text);
            mContent.setText(data.getContent());
            mAuthor.setText("- "+data.getAuthor());


            mCardView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {

                mCardView.setChecked(!mCardView.isChecked());

                    return true;
                }
            });

        }
    }
}
