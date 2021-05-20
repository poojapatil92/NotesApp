package com.example.notesapp.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.util.LruCache;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.notesapp.BuildConfig;
import com.example.notesapp.R;
import com.example.notesapp.adapter.RecyclerViewAdapter;
import com.example.notesapp.model.NotesModel;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfWriter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private RecyclerViewAdapter mAdapter;
    private GridLayoutManager mGridLayoutManager;
    private LinearLayoutManager mLinearLayoutMnager;
    private RecyclerView mRecyclerView;
    private SearchView mSearchView;
    private ConstraintLayout parentLayout;
    private ProgressDialog progressDialog;
    private ImageView iv_download;
    private String searchString;
    private static final String BASE_URL = "https://api.quotable.io/quotes";
    private Gson gson;
    private NotesModel.NotesDetails model;
    private ArrayList<NotesModel.NotesDetails> mListItems;
    private static final int PERMISSION_REQUEST_CODE = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setAppToolbar();
        requestPermission();
        attachViews();
        GetData();
        setListeners();


    }

    private void setAppToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        iv_download = toolbar.findViewById(R.id.iv_download);


    }
    private void requestPermission() {

        int result = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE);
        int result1 = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);


        if (result != PackageManager.PERMISSION_GRANTED &&
                result1 != PackageManager.PERMISSION_GRANTED
               ) {
            ActivityCompat.requestPermissions(this, new String[]{

                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    }, PERMISSION_REQUEST_CODE);
        } else {

        }
    }


    private void setListeners() {

        iv_download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(mListItems.size()>0) {

                    generatePDF(mRecyclerView);
                }
            }
        });

    }

    public void generatePDF(RecyclerView view) {

        RecyclerView.Adapter adapter = view.getAdapter();
        Bitmap bigBitmap = null;
        if (adapter != null) {
            int size = adapter.getItemCount();
            int height = 0;
            Paint paint = new Paint();
            int iHeight = 0;
            final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);


            final int cacheSize = maxMemory / 8;
            LruCache<String, Bitmap> bitmaCache = new LruCache<>(cacheSize);
            for (int i = 0; i < size; i++) {
                RecyclerView.ViewHolder holder = adapter.createViewHolder(view, adapter.getItemViewType(i));
                adapter.onBindViewHolder(holder, i);
                holder.itemView.measure(View.MeasureSpec.makeMeasureSpec(view.getWidth(), View.MeasureSpec.EXACTLY),
                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
                holder.itemView.layout(0, 0, holder.itemView.getMeasuredWidth(), holder.itemView.getMeasuredHeight());
                holder.itemView.setDrawingCacheEnabled(true);
                holder.itemView.buildDrawingCache();
                Bitmap drawingCache = holder.itemView.getDrawingCache();
                if (drawingCache != null) {

                    bitmaCache.put(String.valueOf(i), drawingCache);
                }

                height += holder.itemView.getMeasuredHeight();
            }

            bigBitmap = Bitmap.createBitmap(view.getMeasuredWidth(), height, Bitmap.Config.ARGB_8888);
            Canvas bigCanvas = new Canvas(bigBitmap);
            bigCanvas.drawColor(Color.WHITE);

            Document document = new Document(PageSize.A4);
            File dir = new File(Environment.getExternalStorageDirectory().getAbsoluteFile() + "/NotesApp");
            if (!dir.exists()) {
                dir.mkdirs();
            }
            final File file = new File(dir, "NotesPDF.pdf");
            try {
                PdfWriter.getInstance(document, new FileOutputStream(file));
            } catch (DocumentException | FileNotFoundException e) {
                e.printStackTrace();
            }

            for (int i = 0; i < size; i++) {

                try {
                    Bitmap bmp = bitmaCache.get(String.valueOf(i));
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);

                    com.itextpdf.text.Image image = com.itextpdf.text.Image.getInstance(stream.toByteArray());
                    float scaler = ((document.getPageSize().getWidth() - document.leftMargin()
                            - document.rightMargin() - 0) / image.getWidth()) * 100; // 0 means you have no indentation. If you have any, change it.
                    image.scalePercent(scaler);
                    image.setAlignment(com.itextpdf.text.Image.ALIGN_CENTER | com.itextpdf.text.Image.ALIGN_TOP);
                    if (!document.isOpen()) {
                        document.open();
                    }
                    document.add(image);

                } catch (Exception ex) {
                    Log.e("Exception", ex.getMessage());
                }
            }

            if (document.isOpen()) {
                document.close();
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("Success")
                            .setMessage("PDF File Generated Successfully.")
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {

                                  openFile(file);

                                   // PDFView pdf= findViewById(R.id.activity_main_pdf_view);
                                   // pdf.fromFile(file).show();
                                }

                            }).show();
                }
            });

        }

    }
    private void openFile(File url) {
        try {
            Uri uri;
            File file = new File(url.getAbsolutePath());
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            String ext = file.getName().substring(file.getName().indexOf(".") + 1);
            String type = mime.getMimeTypeFromExtension(ext);

            if (Build.VERSION.SDK_INT >= 24) {
                uri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", url);
            } else {
                uri = Uri.fromFile(url);
            }
            Log.e("", "uri : " + uri);
            Intent openFile = new Intent(Intent.ACTION_VIEW, uri);
            openFile.setDataAndType(uri, type);
            openFile.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                    Intent.FLAG_ACTIVITY_SINGLE_TOP |
                    Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_GRANT_READ_URI_PERMISSION |
                    Intent.FLAG_ACTIVITY_NO_HISTORY);
            startActivity(openFile);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "No application found which can open the file", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0) {


                    boolean writeStateAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    boolean readAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;

                    if (readAccepted && writeStateAccepted) {

                    } else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (!readAccepted || !writeStateAccepted ) {
                                showMessageOKCancel("You need to allow access to the permissions",
                                        (dialog, which) -> {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                                                                Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                                        PERMISSION_REQUEST_CODE);
                                            } else {
                                                ActivityCompat.requestPermissions((Activity) this, new String[]{ Manifest.permission.READ_EXTERNAL_STORAGE,
                                                                Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                                        PERMISSION_REQUEST_CODE);
                                            }
                                        });
                            }
                        } else {

                        }
                    }
                }
                break;

        }
    }
    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new android.app.AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }
    private void generateDataList(ArrayList<NotesModel.NotesDetails> mListItems) {
        mAdapter = new RecyclerViewAdapter(this, mListItems);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter.notifyDataSetChanged();




        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {

                final ArrayList<NotesModel.NotesDetails> filteredModelList = filter(mListItems, query);
                if (filteredModelList.size() > 0) {
                    mAdapter.setFilter(filteredModelList);
                    return true;
                } else {
                    // If not matching search filter data
                    View view = getCurrentFocus();
                    if (view != null) {
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }
                    Snackbar.make(parentLayout, "Record not found", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                    return false;
                }

            }
        });
    }


    private void attachViews() {

        mRecyclerView=findViewById(R.id.recyclerView);
        mSearchView=findViewById(R.id.search_view);
        parentLayout=findViewById(R.id.root);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setLayoutManager(mGridLayoutManager);



        mSearchView.setOnSearchClickListener(v -> {
            LinearLayout.LayoutParams rlp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);
            mSearchView.setLayoutParams(rlp);

        });

        mSearchView.setOnCloseListener(() -> {
            LinearLayout.LayoutParams rlp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);
            mSearchView.setLayoutParams(rlp);

            return false;
        });



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
                                model=gson.fromJson(array.getString(i), NotesModel.NotesDetails.class);
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
    private ArrayList<NotesModel.NotesDetails> filter(ArrayList<NotesModel.NotesDetails> models, String query) {
        query = query.toLowerCase();
        this.searchString = query;

        final ArrayList<NotesModel.NotesDetails> filteredModelList = new ArrayList<>();
        for (NotesModel.NotesDetails model : models) {
            String tag = model.getTags().get(0).toLowerCase();
            String content = model.getContent();
            String author = model.getAuthor().toLowerCase();


                if (tag.contains(query) || content.contains(query) || author.contains(query)) {
                    filteredModelList.add(model);
                }

        }

        generateDataList(filteredModelList);

        return filteredModelList;
    }

}
