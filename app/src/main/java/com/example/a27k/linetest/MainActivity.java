package com.example.a27k.linetest;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    ImageView imgView;
    TextView txtView;
    List<String> listUrl;
    int listCnt = 0;
    final String jsonStr = "{'title':'Civil War','image':['http://movie.phinf.naver.net/20151127_272/1448585271749MCMVs_JPEG/movie_image.jpg?type=m665_443_2','http://movie.phinf.naver.net/20151127_84/1448585272016tiBsF_JPEG/movie_image.jpg?type=m665_443_2','http://movie.phinf.naver.net/20151125_36/1448434523214fPmj0_JPEG/movie_image.jpg?type=m665_443_2']}";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imgView = findViewById(R.id.ivImage);
        txtView = findViewById(R.id.tvTitle);

        imgView.setOnClickListener(this);

        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    100);

        } else {
            init();
        }

    }

    private void init() {
        try {
            JSONObject jo = new JSONObject(jsonStr);

            String title = jo.getString("title");
            JSONArray imageUrls = jo.getJSONArray("image");

            listUrl = new ArrayList<>();

            for (int i = 0; i < imageUrls.length(); i++) {
                listUrl.add(imageUrls.getString(i));
            }

            txtView.setText(title);

            //Gson gson = new Gson();
            //Movie movieinfo = gson.fromJson(jsonStr, Movie.class);

            OpenHttpConnection task = new OpenHttpConnection(MainActivity.this);
            //execute에 param 여러 개를 던질 수 있었음
            //doInBackground에서 받아 사용할 때, 한 개일 경우에도 param[0] 식으로 사용해야 한다.
            task.execute(listUrl.get(listCnt));

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View view) {
        new OpenHttpConnection(MainActivity.this).execute(listUrl.get(listCnt));
    }

    private class OpenHttpConnection extends AsyncTask<Object, String, String> {

        private Context mContext;
        ProgressDialog mDlg;

        public OpenHttpConnection(Context context) {
            mContext = context;
        }

        @Override
        protected void onPreExecute() {
            mDlg = new ProgressDialog(mContext);
            mDlg.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mDlg.setMessage("Downloading");
            mDlg.show();

            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(String... progress) {
            mDlg.setProgress(Integer.parseInt(progress[0]));
        }


        @Override
        protected String doInBackground(Object... params) {
            int count = 0;
            byte data[] = new byte[1024];
            long total = 0;

            try {
                //mBitmap = BitmapFactory.decodeStream((InputStream) new URL(url).getContent());
                //mBitmap = BitmapFactory.decodeStream(input);
                URL url = new URL(params[0].toString());

                InputStream input = new java.net.URL(url.toString()).openStream();

                File testDirectory = new File(Environment.getExternalStorageDirectory() + "/Folder");
                if (!testDirectory.exists()) {
                    testDirectory.mkdir();
                }

                FileOutputStream fos = new FileOutputStream(testDirectory + "/Downloadtest" + listCnt + ".jpg");
                URLConnection connection = url.openConnection();
                connection.connect();
                int lenghtOfFile = connection.getContentLength();

                int progress = 0;

                while ((count = input.read(data)) != -1) {
                    total += count;
                    int progress_temp = (int) total * 100 / lenghtOfFile;
                    publishProgress("" + progress_temp);

                    if (progress_temp % 10 == 0 && progress != progress_temp) {
                        progress = progress_temp;
                    }

                    fos.write(data, 0, count);
                }
                input.close();
                fos.close();

            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return "/storage/emulated/0/Folder/Downloadtest" + listCnt + ".jpg";
        }

        @Override
        protected void onPostExecute(String bm) {
            super.onPostExecute(bm);
            mDlg.dismiss();
            imgView.setImageURI(Uri.parse(bm));

            listCnt++;
            if (listCnt >= listUrl.size()) {
                listCnt = 0;
            }
        }
    }


    public class Movie {
        String title;
        ArrayList<String> image;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public ArrayList<String> getUrls() {
            return image;
        }

        public void setUrls(ArrayList<String> urls) {
            this.image = urls;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 100: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    init();
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }
}

