package com.example.notesapp.view;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.notesapp.ClickListener;
import com.example.notesapp.R;
import com.example.notesapp.RecyclerTouchListener;
import com.example.notesapp.model.NotesModel;
import com.google.android.material.card.MaterialCardView;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private RecyclerViewAdapter mAdapter;
    private GridLayoutManager mGridLayoutManager;
    private LinearLayoutManager mLinearLayoutMnager;
    private RecyclerView mRecyclerView;
    private SearchView mSearchView;
    private ProgressDialog progressDialog;
    private ImageView iv_download;
    private static final String BASE_URL = "https://api.quotable.io/quotes";
    private Gson gson;
    private ArrayList<NotesModel.NotesDetails> mListItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setAppToolbar();
        attachViews();
        GetData();
        setListeners();

        AppCompatDelegate
                .setDefaultNightMode(
                        AppCompatDelegate
                                .MODE_NIGHT_YES);
    }

    private void setAppToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        iv_download = toolbar.findViewById(R.id.iv_download);


    }

    private void setListeners() {

        iv_download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                GeneratePDF();

            }
        });

    }

    private void GeneratePDF() {


    }


    private void generateDataList(ArrayList<NotesModel.NotesDetails> mListItems) {
        mAdapter = new RecyclerViewAdapter(this, mListItems);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter.notifyDataSetChanged();


        mRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(this, mRecyclerView, new ClickListener() {
            @Override
            public void onClick(View view, final int position) {

            }

            @Override
            public void onLongClick(View view, int position) {

                MaterialCardView cardView=view.findViewById(R.id.card_view);

                cardView.setChecked(!cardView.isChecked());

            }
        }));
    }


    private void attachViews() {

        mRecyclerView=findViewById(R.id.recyclerView);
        mSearchView=findViewById(R.id.search_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setLayoutManager(mGridLayoutManager);

    }

    private void GetData() {

        mListItems=new ArrayList<>();

        if (mListItems.size() > 0) {
            mListItems.clear();
        }


        StringRequest stringRequest = new StringRequest(Request.Method.GET, BASE_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            gson = new Gson();

                            JSONObject obj = new JSONObject(response);
                            JSONArray array = obj.getJSONArray("results");
                            for (int i = 0; i < array.length(); i++) {
                                NotesModel.NotesDetails model=gson.fromJson(array.getString(i), NotesModel.NotesDetails.class);
                                mListItems.add(model);
                            }
                            generateDataList(mListItems);
                            }catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

   /* @Override
    public void onDestroy() {
        super.onDestroy();
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }*/
}
