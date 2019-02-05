package app.uangkas;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.leavjenn.smoothdaterangepicker.date.SmoothDateRangePickerFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import app.uangkas.helper.Config;
import app.uangkas.helper.SqliteHelper;

/**
 * Created by Lazday Indonesia
 * www.lazday.com
 * on 5/18/2017.
 */

public class MainActivity extends AppCompatActivity{

    TextView text_masuk, text_keluar, text_total;
    ListView list_kas;
    SwipeRefreshLayout swipe_refresh;
    ArrayList<HashMap<String, String>> aruskas = new ArrayList<HashMap<String, String>>();

    public static TextView text_filter;
    public static String LINK, transaksi_id, status, jumlah, keterangan, tanggal, tanggal2,
            tgl_dari, tgl_ke;
    public static boolean filter;

    String query_kas, query_total;
    SqliteHelper sqliteHelper;
    Cursor cursor;

    // session-intro
    int sess_intro;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        sess_intro = 0;
        sharedPreferences = getSharedPreferences("sess_pref_lazday_uangkasv2", Context.MODE_APPEND);
        sess_intro = sharedPreferences.getInt("intro", 0);
        if (sess_intro == 0){
            // set session
            sharedPreferences = getSharedPreferences("sess_pref_lazday_uangkasv2", Context.MODE_PRIVATE);
            editor = sharedPreferences.edit();
            editor.putInt("intro", 1);
            editor.apply();
            // call intro
            startActivity(new Intent(MainActivity.this, IntroActivity.class));
        }

        LINK = Config.host + "list.php";
        transaksi_id = ""; status = ""; jumlah = ""; keterangan = ""; tanggal = ""; tanggal2 = "";
        tgl_dari = ""; tgl_ke = ""; query_kas = ""; query_total = "";  filter = false;
        sqliteHelper = new SqliteHelper(this);

        text_filter     = (TextView) findViewById(R.id.text_filter);
        text_masuk      = (TextView) findViewById(R.id.text_masuk);
        text_keluar     = (TextView) findViewById(R.id.text_keluar);
        text_total      = (TextView) findViewById(R.id.text_total);
        list_kas        = (ListView) findViewById(R.id.list_kas);
        swipe_refresh   = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);

        swipe_refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                query_kas   =
                        "SELECT *, strftime('%d/%m/%Y', tanggal) AS tgl FROM transaksi ORDER BY transaksi_id DESC";
                query_total =
                        "SELECT SUM(jumlah) AS total, " +
                                "(SELECT SUM(jumlah) FROM transaksi WHERE status='MASUK') AS masuk, " +
                                "(SELECT SUM(jumlah) FROM transaksi WHERE status='KELUAR') AS keluar " +
                                "FROM transaksi";
                LINK = Config.host + "list.php";
                KasAdapter2();
                text_filter.setVisibility(View.GONE);
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                startActivity(new Intent(MainActivity.this, AddActivity.class));
                startActivity(new Intent(MainActivity.this, NewActivity.class));
            }
        });


    }

    private void KasAdapter(){

        aruskas.clear(); list_kas.setAdapter(null);

        SQLiteDatabase database = sqliteHelper.getReadableDatabase();
        cursor = database.rawQuery( query_kas, null);
        cursor.moveToFirst();

        int i;
        for (i=0; i < cursor.getCount(); i++){
            cursor.moveToPosition(i);

            HashMap<String, String> map = new HashMap<String, String>();
            map.put("transaksi_id", cursor.getString(0) );
            map.put("status",       cursor.getString(1) );
            map.put("jumlah",       cursor.getString(2) );
            map.put("keterangan",   cursor.getString(3) );
            map.put("tanggal",      cursor.getString(5) );
            aruskas.add(map);
        }

        if (i == 0){
            Toast.makeText(getApplicationContext(), "Tidak ada transaksi untuk ditampilkan",
                    Toast.LENGTH_LONG).show();
        }

        SimpleAdapter simpleAdapter = new SimpleAdapter(this, aruskas, R.layout.list_kas,
                new String[] { "transaksi_id", "status", "jumlah", "keterangan", "tanggal"},
                new int[] {R.id.text_transaksi_id, R.id.text_status, R.id.text_jumlah, R.id.text_keterangan,
                        R.id.text_tanggal});

        list_kas.setAdapter(simpleAdapter);
        list_kas.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                transaksi_id = ((TextView) view.findViewById(R.id.text_transaksi_id)).getText().toString();
                ListMenu();
            }
        });

        KasTotal();
    }

    private void KasTotal(){
        NumberFormat rupiahFormat = NumberFormat.getInstance(Locale.GERMANY);

        SQLiteDatabase database = sqliteHelper.getReadableDatabase();
        cursor = database.rawQuery( query_total, null);
        cursor.moveToFirst();
//        text_total.setText( rupiahFormat.format(cursor.getDouble(0)) );
        text_masuk.setText( rupiahFormat.format(cursor.getDouble(1)) );
        text_keluar.setText( rupiahFormat.format(cursor.getDouble(2)) );
        text_total.setText(
                rupiahFormat.format(cursor.getDouble(1) - cursor.getDouble(2))
        );

        swipe_refresh.setRefreshing(false);

        if (!filter) { text_filter.setVisibility(View.GONE); }
        filter = false;
    }

    private void KasAdapter2(){

        swipe_refresh.setRefreshing(true);
        aruskas.clear(); list_kas.setAdapter(null);

        Log.d("link", LINK );
        AndroidNetworking.post( LINK )
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // do anything with response

                        NumberFormat rupiahFormat = NumberFormat.getInstance(Locale.GERMANY);

                        text_masuk.setText(
                                rupiahFormat.format(response.optDouble("masuk")));
                        text_keluar.setText(
                                rupiahFormat.format( response.optDouble("keluar") ));
                        text_total.setText(
                                rupiahFormat.format( response.optDouble("saldo") ));

                        try {
                            JSONArray jsonArray = response.optJSONArray("result");
                            for (int i = 0; i < jsonArray.length(); i++) {

                                JSONObject responses    = jsonArray.getJSONObject(i);
                                HashMap<String, String> map = new HashMap<String, String>();

                                map.put("transaksi_id", responses.optString("transaksi_id"));
                                map.put("status",       responses.optString("status"));
                                map.put("jumlah",       responses.optString("jumlah"));
                                map.put("keterangan",   responses.optString("keterangan"));
                                map.put("tanggal",      responses.optString("tanggal"));
                                map.put("tanggal2",      responses.optString("tanggal2"));

                                aruskas.add(map);
                            }

                            Adapter();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    @Override
                    public void onError(ANError error) {
                        // handle error
                    }
                });
    }

    private void Adapter(){
        SimpleAdapter simpleAdapter = new SimpleAdapter(this, aruskas, R.layout.list_kas,
                new String[] { "transaksi_id", "status", "jumlah", "keterangan", "tanggal", "tanggal2"},
                new int[] {R.id.text_transaksi_id, R.id.text_status, R.id.text_jumlah, R.id.text_keterangan,
                        R.id.text_tanggal, R.id.text_tanggal2});

        list_kas.setAdapter(simpleAdapter);
        list_kas.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                transaksi_id    = ((TextView) view.findViewById(R.id.text_transaksi_id)).getText().toString();
                status          = ((TextView) view.findViewById(R.id.text_status)).getText().toString();
                jumlah          = ((TextView) view.findViewById(R.id.text_jumlah)).getText().toString();
                keterangan      = ((TextView) view.findViewById(R.id.text_keterangan)).getText().toString();
                tanggal         = ((TextView) view.findViewById(R.id.text_tanggal)).getText().toString();
                tanggal2        = ((TextView) view.findViewById(R.id.text_tanggal2)).getText().toString();
                ListMenu();
            }
        });

        swipe_refresh.setRefreshing(false);
    }

    @Override
    public void onResume(){
        super.onResume();

        query_kas   =
                "SELECT *, strftime('%d/%m/%Y', tanggal) AS tgl FROM transaksi ORDER BY transaksi_id DESC";
        query_total =
                "SELECT SUM(jumlah) AS total, " +
                "(SELECT SUM(jumlah) FROM transaksi WHERE status='MASUK') AS masuk, " +
                "(SELECT SUM(jumlah) FROM transaksi WHERE status='KELUAR') AS keluar " +
                "FROM transaksi";

        if (filter) {

            query_kas   =
                    "SELECT *, strftime('%d/%m/%Y', tanggal) AS tgl FROM transaksi  " +
                    "WHERE (tanggal >= '" + tgl_dari + "') AND (tanggal <= '" + tgl_ke + "') ORDER BY transaksi_id ASC ";
            query_total =
                    "SELECT SUM(jumlah) AS total, " +
                    "(SELECT SUM(jumlah) FROM transaksi WHERE status='MASUK' AND (tanggal >= '" + tgl_dari + "') AND (tanggal <= '" + tgl_ke + "') ), " +
                    "(SELECT SUM(jumlah) FROM transaksi WHERE status='KELUAR' AND (tanggal >= '" + tgl_dari + "') AND (tanggal <= '" + tgl_ke + "')) " +
                    "FROM transaksi " +
                    "WHERE (tanggal >= '" + tgl_dari + "') AND (tanggal <= '" + tgl_ke + "') ";

            LINK = Config.host + "filter.php?from=" + tgl_dari + "&to=" + tgl_ke;
            filter = false;
        }

//        KasAdapter();
        KasAdapter2();

    }

    private void ListMenu(){

        final Dialog dialog = new Dialog(MainActivity.this);
        dialog.setContentView(R.layout.list_menu);
        dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT);

        TextView text_edit  = (TextView) dialog.findViewById(R.id.text_edit);
        TextView text_hapus = (TextView) dialog.findViewById(R.id.text_hapus);
        dialog.show();

        text_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                startActivity(new Intent(MainActivity.this, EditActivity.class));
            }
        });
        text_hapus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
//                Hapus();
                Hapus2();
            }
        });
    }

    private void Hapus(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Konfirmasi");
        builder.setMessage("Yakin untuk mengahapus transaksi ini?");
        builder.setPositiveButton(
                "Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();

                        SQLiteDatabase database = sqliteHelper.getWritableDatabase();
                        database.execSQL("DELETE FROM transaksi WHERE transaksi_id = '" + transaksi_id + "'");
                        Toast.makeText(getApplicationContext(), "Tranksaki berhasil dihapus",
                                Toast.LENGTH_LONG).show();

                        KasAdapter();

                    }
                });

        builder.setNegativeButton(
                "No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });

        builder.show();
    }

    private void Hapus2(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Konfirmasi");
        builder.setMessage("Yakin untuk mengahapus transaksi ini?");
        builder.setPositiveButton(
                "Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();

                        AndroidNetworking.post( Config.host + "delete.php")
                                .addBodyParameter("transaksi_id", transaksi_id)
                                .setPriority(Priority.MEDIUM)
                                .build()
                                .getAsJSONObject(new JSONObjectRequestListener() {
                                    @Override
                                    public void onResponse(JSONObject response) {
                                        // do anything with response
                                        if (response.optString("response").toString().equals("success")){
                                            Toast.makeText(getApplicationContext(), "Data berhasil dihapus",
                                                    Toast.LENGTH_LONG).show();

                                            KasAdapter2();

                                        } else {
                                            Toast.makeText(getApplicationContext(), "Gagal",
                                                    Toast.LENGTH_LONG).show();
                                        }
                                    }
                                    @Override
                                    public void onError(ANError error) {
                                        // handle error
                                    }
                                });

                    }
                });

        builder.setNegativeButton(
                "No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });

        builder.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_filter) {
            startActivity(new Intent(MainActivity.this, FilterActivity.class));
//            _filterMysql();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void _filterMysql(){
        SmoothDateRangePickerFragment smoothDateRangePickerFragment =
                SmoothDateRangePickerFragment
                        .newInstance(new SmoothDateRangePickerFragment.OnDateRangeSetListener() {
                            @Override
                            public void onDateRangeSet(SmoothDateRangePickerFragment view,
                                                       int yearStart, int monthStart,
                                                       int dayStart, int yearEnd,
                                                       int monthEnd, int dayEnd) {
                                String date = "You picked the following date range: \n"
                                        + "From " + dayStart + "/" + (++monthStart)
                                        + "/" + yearStart + " To " + dayEnd + "/"
                                        + (++monthEnd) + "/" + yearEnd;
                                Log.d("daterange_result",   date);

                                tgl_dari = yearStart + "-" + monthStart + "-" + dayStart ;
                                tgl_ke = yearEnd + "-" + monthEnd + "-" + dayEnd ;
                                LINK = Config.host + "filter.php?from=" + tgl_dari + "&to=" + tgl_ke;

                                text_filter.setVisibility(View.VISIBLE);
                                text_filter.setText(
                                        dayStart + "/" + monthStart + "/" + yearStart + " - " +
                                                dayStart + "/" + monthStart + "/" + yearStart
                                );
                                KasAdapter2();


                            }
                        });
        smoothDateRangePickerFragment.show(getFragmentManager(), "Datepickerdialog");
    }
}
