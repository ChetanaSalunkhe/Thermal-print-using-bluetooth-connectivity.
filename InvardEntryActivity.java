package com.example.adatdataentry.Activity;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.Html;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.adatdataentry.Adapters.FarmersListAdapter;
import com.example.adatdataentry.Adapters.ItemDetailsAdapter;
import com.example.adatdataentry.BeanClass.BluetoothClass;
import com.example.adatdataentry.BeanClass.INWBean;
import com.example.adatdataentry.BeanClass.Utils;
import com.example.adatdataentry.Common.AdatDataEntryData;
import com.example.adatdataentry.Common.CommonMethods;
import com.example.adatdataentry.Database.DatabaseHelper;
import com.example.adatdataentry.R;
import com.zj.btsdk.BluetoothService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.SoapFault;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import static com.example.adatdataentry.Common.CommonMethods.clearTable;
import static java.lang.Float.parseFloat;
import static java.lang.Float.valueOf;

public class InvardEntryActivity extends AppCompatActivity {
    private Context parent;
    Button btn_date, btn_time, btn_saveprint, btn_next, btn_save, btn_Scan, btn_savelocalprint;
    LinearLayout lay_scanprint, lay_savenext;
    TextView txt_transporter;
    ImageView img_search;
    AutoCompleteTextView edt_vehicleno, edt_freight, edt_totalqty, edt_token, edt_actualqty;
    String INWHID = "", INWBID = "", ActualQTY = "",FarmersCnt = "", TotFreight = "",TotQTY_Bags = "", VehicleNO = "",
            DATE = "", TIME = "", TokenNO = "", TransporterNAME = "",TransporterCODE = "", YARDTitle = "", USERName = "";

    /*add farmer screen content*/
    ImageView addfarmer,viewfarmers;
    LinearLayout layout_add_farmer_details,layout_farmers_list;
    ListView list_farmers;
    Button btn_back;
    ArrayList<INWBean> farmrsWithLotList;
    FarmersListAdapter farmerAdapter;

    /*add farmer details screen content*/
    LinearLayout layfixfarmer;
    ImageView img_search_farmer, img_search_item;
    TextView txt_farmer, txt_total_lot,txt_total_qty;
    AutoCompleteTextView txt_itmname, txt_itmcode, txt_lot, txt_qty, edt_freight_fd,txt_advance,txt_varani;
    Button btn_back_fd, btn_add, btn_next_fd, btn_update;
    ListView list_farmers_details;
    String FarmerNAME = "", MODE = "";
    String INWDID = "", SUPPID = "", SUPPNAME = "", ITEMCODE = "", ITEMNAME = "", LOT = "", QTY = "", FREIGHT = "";
    ArrayList<INWBean> listItems;
    ItemDetailsAdapter itemsadapter;
    int posToUpdate;
    String itemToUpdate = "";

    BluetoothService mService = null;
    BluetoothDevice con_dev = null;
    private boolean deviceConnected = false;
    Dialog dialog;

    DatabaseHelper dbhelper;
    SQLiteDatabase sqldb;
    CommonMethods methods;

    public String xml1, xml2;
    private StringBuilder sb;
    public static String today, todaysDate;
    public static String date = null;
    public static String time = null;
    DatePickerDialog datePickerDialog;
    static int year, month, day;
    private static BluetoothSocket btsocket;
    private static OutputStream btoutputstream;

    JSONObject jMAin_INWH;
    JSONArray jsonINWBArray, jsonINWDArray, jsonINWHArray, jMAin_INWH_array;
    String keyInwh = "", keyInwb = "", keyInwd = "";
    String valInwh = "", valInwb = "", valInwd = "";
    int counAPI = 0;
    boolean printFlag = false, receiptflag = false;
    String FIRMNAME = "";
    String mainFrght = "", main_Qty = "";
    float frt_for_single_qty=0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invard_entry);
        //getSupportActionBar().setTitle("Inward Entry");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimary));
        }else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimary));
        }

        init();

        int actqty_cnt = Integer.parseInt(edt_actualqty.getText().toString().trim());

        if(actqty_cnt == 0){
           // btn_saveprint.setVisibility(View.GONE);
            btn_save.setVisibility(View.GONE);
        }else {
           // btn_saveprint.setVisibility(View.VISIBLE);
            btn_save.setVisibility(View.VISIBLE);
        }

        if(INWHID == "" || INWHID == null){
           INWHID = generateINWHID();
        }else {

        }

        getFirmDetails();

        setListeners();
    }

    public void init(){
        parent = InvardEntryActivity.this;

        final ActionBar ab = getSupportActionBar();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
       // getSupportActionBar().setTitle(Html.fromHtml("<small>"+getResources().getString(R.string.merchlist)+"</small>"));
        getSupportActionBar().setTitle("Inward Entry");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        btn_date = findViewById(R.id.btn_date);
        btn_date.setClickable(false);
        btn_date.setFocusable(false);
        btn_time = findViewById(R.id.btn_time);
        btn_time.setClickable(false);
        btn_time.setFocusable(false);
        btn_saveprint = findViewById(R.id.btn_saveprint);
        btn_saveprint.setClickable(false);
        btn_saveprint.setEnabled(false);
        btn_save = findViewById(R.id.btn_save);
        btn_Scan = findViewById(R.id.btn_Scan);
        btn_savelocalprint = findViewById(R.id.btn_savelocalprint);
        btn_next = findViewById(R.id.btn_next);
        btn_next.setVisibility(View.GONE);
        lay_scanprint = findViewById(R.id.lay_scanprint);
        lay_scanprint.setVisibility(View.GONE);
        lay_savenext = findViewById(R.id.lay_savenext);
        lay_savenext.setVisibility(View.GONE);

        txt_transporter = findViewById(R.id.txt_transporter);
        edt_vehicleno = findViewById(R.id.edt_vehicleno);
        edt_freight = findViewById(R.id.edt_freight);
        /*edt_freight.setClickable(false);
        edt_freight.setFocusable(false);
        edt_freight.setEnabled(false);*/

        edt_totalqty = findViewById(R.id.edt_totalqty);
        edt_token = findViewById(R.id.edt_token);
        edt_actualqty = findViewById(R.id.edt_actualqty);
        img_search = findViewById(R.id.img_search);

        layout_add_farmer_details = findViewById(R.id.layout_add_farmer_details);
        layout_farmers_list = findViewById(R.id.layout_farmers_list);

        /*add farmer list screen*/
        list_farmers = findViewById(R.id.list_farmers);
        btn_back = findViewById(R.id.btn_back_fdlist);
        addfarmer = findViewById(R.id.addfarmer);
        viewfarmers = findViewById(R.id.viewfarmers);

        /*add farmers details screen*/
        layfixfarmer = findViewById(R.id.layfixfarmer);
        img_search_farmer = findViewById(R.id.img_search_farmer);
        img_search_item = findViewById(R.id.img_search_item);
        txt_farmer = findViewById(R.id.txt_farmer);
        txt_itmcode = findViewById(R.id.txt_itmcode);
        txt_itmname = findViewById(R.id.txt_itmname);
        txt_lot = findViewById(R.id.txt_lot);
        txt_qty = findViewById(R.id.txt_qty);
        edt_freight_fd = findViewById(R.id.edt_freight_fd);
        txt_advance = findViewById(R.id.txt_advance);
        txt_varani = findViewById(R.id.txt_varani);
        txt_total_lot = findViewById(R.id.txt_total_lot);
        txt_total_qty = findViewById(R.id.txt_total_qty);
        btn_back_fd = findViewById(R.id.btn_back_fd);
        btn_update = findViewById(R.id.btn_update);
        btn_add = findViewById(R.id.btn_add);
        list_farmers_details = findViewById(R.id.list_farmers_details);

        txt_farmer.setText(FarmerNAME);

        dbhelper = new DatabaseHelper(parent, AdatDataEntryData.SHOPNO);
        sqldb = dbhelper.getWritableDatabase();
        methods = new CommonMethods(parent);

        farmrsWithLotList = new ArrayList<INWBean>();
        listItems = new ArrayList<INWBean>();

        mService = new BluetoothService(parent, mHandler);

        if (mService.isAvailable() == false) {
            Toast.makeText(parent, "Bluetooth is not available", Toast.LENGTH_LONG).show();
        }

        final Calendar c = Calendar.getInstance();
        year = c.get(Calendar.YEAR);
        month = c.get(Calendar.MONTH);
        day = c.get(Calendar.DAY_OF_MONTH);

        date = day + "-"+ String.format("%02d", (month + 1))+ "-" + year;
        btn_date.setText(date);
        DATE = btn_date.getText().toString().trim();

        Calendar mcurrentTime = Calendar.getInstance();
        int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
        int minute = mcurrentTime.get(Calendar.MINUTE);

        time = methods.updateTime(hour, minute);
        btn_time.setText(time);
        TIME = btn_time.getText().toString().trim();

    }

    public void setListeners(){

        viewfarmers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layout_farmers_list.setVisibility(View.VISIBLE);
                btn_save.setVisibility(View.GONE);

                txt_farmer.setText("");
                txt_advance.setText("");
                txt_varani.setText("");

                MODE = "ADDNEW";
            }
        });

        addfarmer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                float frt_ = 0f, qty_ = 0f;

                String frt = edt_freight.getText().toString().trim();
                String mainqty = edt_totalqty.getText().toString().trim();

                if((frt.equals("") || frt.equals(null)) || (mainqty.equals("") || mainqty.equals(null))){
                    //plz fill freight and qty
                    Toast.makeText(parent,"Please fill freight and quantity first",Toast.LENGTH_SHORT).show();
                }else {

                    mainFrght = frt;
                    main_Qty = mainqty;

                    frt_ = Float.parseFloat(frt);
                    qty_ = Float.parseFloat(mainqty);

                    //frt_for_single_qty = frt/qty;
                    frt_for_single_qty = frt_/qty_;

                    //create new farmer
                    //INWBID = generateINWBID();
                    MODE = "ADDNEW";

                    layout_add_farmer_details.setVisibility(View.VISIBLE);
                    addfarmer.setVisibility(View.INVISIBLE);
                    list_farmers_details.setVisibility(View.GONE);

                    if(MODE.equalsIgnoreCase("ADDNEW")){
                        INWBID = generateINWBID();
                        INWDID = generateINWDID();

                        txt_farmer.setText("");
                        edt_freight_fd.setText("");
                        txt_advance.setText("");
                        txt_varani.setText("");

                        layfixfarmer.setFocusable(true);
                        layfixfarmer.setClickable(true);
                        txt_farmer.setSelected(true);
                        img_search_farmer.setVisibility(View.VISIBLE);
                        txt_farmer.setEnabled(true);
                        img_search_farmer.setEnabled(true);
                        img_search_farmer.setClickable(true);
                        img_search_farmer.setFocusableInTouchMode(true);
                        img_search_farmer.setFocusable(true);
                        edt_freight_fd.setClickable(true);
                        edt_freight_fd.setEnabled(true);
                        edt_freight_fd.setFocusable(true);
                        edt_freight_fd.setFocusableInTouchMode(true);
                        txt_advance.setClickable(true);
                        txt_advance.setEnabled(true);
                        txt_advance.setFocusable(true);
                        txt_advance.setFocusableInTouchMode(true);
                        txt_varani.setClickable(true);
                        txt_varani.setEnabled(true);
                        txt_varani.setFocusable(true);
                        txt_varani.setFocusableInTouchMode(true);

                    }else if(MODE.equalsIgnoreCase("EDIT")){

                        layfixfarmer.setFocusable(false);
                        layfixfarmer.setClickable(false);
                        txt_farmer.setSelected(false);
                        img_search_farmer.setVisibility(View.VISIBLE);
                        txt_farmer.setEnabled(false);
                        img_search_farmer.setEnabled(false);
                        img_search_farmer.setClickable(false);
                        img_search_farmer.setFocusableInTouchMode(false);
                        img_search_farmer.setFocusable(false);
                   /* edt_freight_fd.setClickable(false);
                    edt_freight_fd.setEnabled(false);
                    edt_freight_fd.setFocusable(false);
                    edt_freight_fd.setFocusableInTouchMode(false);*/
                        txt_advance.setClickable(false);
                        txt_advance.setEnabled(false);
                        txt_advance.setFocusable(false);
                        txt_advance.setFocusableInTouchMode(false);
                        txt_varani.setClickable(false);
                        txt_varani.setEnabled(false);
                        txt_varani.setFocusable(false);
                        txt_varani.setFocusableInTouchMode(false);

                        //  INWDID = intent.getStringExtra("INWDID");
                        // getDataFramDatabase();
                    }

                    if(farmrsWithLotList.size() == 0){
                        btn_back.setText("Close");
                    }else {
                        btn_back.setText("Save");
                    }
                }
            }
        });

        list_farmers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                layout_add_farmer_details.setVisibility(View.VISIBLE);
                addfarmer.setVisibility(View.INVISIBLE);
                list_farmers_details.setVisibility(View.VISIBLE);

                txt_itmcode.setText("");
                txt_itmname.setText("");
                txt_lot.setText("");
                txt_qty.setText("");
                edt_freight_fd.setText("");

                MODE = "EDIT";

                layfixfarmer.setFocusable(false);
                layfixfarmer.setClickable(false);
                txt_farmer.setSelected(false);
                img_search_farmer.setVisibility(View.VISIBLE);
                txt_farmer.setEnabled(false);
                img_search_farmer.setEnabled(false);
                img_search_farmer.setClickable(false);
                img_search_farmer.setFocusableInTouchMode(false);
                img_search_farmer.setFocusable(false);
                /*edt_freight.setClickable(false);
                edt_freight.setEnabled(false);
                edt_freight.setFocusable(false);
                edt_freight.setFocusableInTouchMode(false);*/

                //edit mode
                String inwhID = farmrsWithLotList.get(position).getInwh_id().trim();
                String inwbID = farmrsWithLotList.get(position).getInwb_id().trim();
                String inwdID = farmrsWithLotList.get(position).getInwd_id().trim();

                getDataFramDatabase_fdtl(inwbID,inwdID); //get farmers items details list

                /*Intent intent = new Intent(InvardEntryActivity.this, AddNewFarmerDetailsActivity.class);
                intent.putExtra("INWHID", inwhID);
                intent.putExtra("INWBID", inwbID);
                intent.putExtra("INWDID",inwdID);
                intent.putExtra("MODE","EDIT");
                //startActivity(intent);
                startActivityForResult(intent,1);*/
            }
        });

        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //add data in Inwb table
                Cursor chk = sqldb.rawQuery("Select * from "+dbhelper.TABLE_INWB+" where INWH_ID='"+INWHID+"'", null);
                if(chk.getCount() > 0){
                    sqldb.delete(dbhelper.TABLE_INWB, "INWH_ID=?",new String[]{INWHID});
                }else {
                    //no data proceed as it is
                }

                Cursor c = sqldb.rawQuery("SELECT * FROM INWB_"+AdatDataEntryData.SHOPNO+" WHERE INWH_ID='"+INWHID+"'", null);
                if(c.getCount() == 0){

                    if (farmrsWithLotList.isEmpty()) {
                        //do not save just finish activity
                     //   finish();
                    } else {

                        float RCVDqty = 0.0F, totFreightAmt = 0.0F;

                        for (int j = 0; j < farmrsWithLotList.size(); j++) {
                            RCVDqty = RCVDqty + Float.parseFloat(farmrsWithLotList.get(j).getTotQty().trim());
                        }

                        for (int j = 0; j < farmrsWithLotList.size(); j++) {
                            totFreightAmt = totFreightAmt + Float.parseFloat(farmrsWithLotList.get(j).getFreight_inwb().trim());
                        }

                        for (int i = 0; i < farmrsWithLotList.size(); i++) {
                            methods.addINWB(farmrsWithLotList.get(i).getInwb_id().trim(), INWHID,
                                    farmrsWithLotList.get(i).getSupp_code().trim(),
                                    farmrsWithLotList.get(i).getSupp_name().trim(),
                                    farmrsWithLotList.get(i).getFreight_inwb().trim(),
                                    farmrsWithLotList.get(i).getTotQty().trim(),
                                    farmrsWithLotList.get(i).getAdv_inwb().trim(),
                                    farmrsWithLotList.get(i).getVarani_inwb().trim());
                        }

                       /* Intent intent = new Intent();
                        intent.putExtra("INWHID", INWHID);
                        intent.putExtra("ActualQTY", String.valueOf(RCVDqty));   //total qty, rcvd qty
                        intent.putExtra("TotFarmers", FarmersCnt);   //farmers count
                        intent.putExtra("TotFreight", String.valueOf(totFreightAmt)); //total freight
                        setResult(AdatDataEntryData.INWBDATA, intent);

                        finish();*/

                        INWHID = INWHID;
                        ActualQTY = String.valueOf(RCVDqty);
                        FarmersCnt = FarmersCnt;
                        TotFreight = String.valueOf(totFreightAmt);

                        edt_actualqty.setText(String.format("%.2f",Float.valueOf(ActualQTY)));
                    //    edt_totalqty.setText(String.format("%.2f",Float.valueOf(ActualQTY)));
                    //    edt_freight.setText(String.format("%.2f",Float.valueOf(TotFreight)));

                        layout_add_farmer_details.setVisibility(View.GONE);
                        layout_farmers_list.setVisibility(View.GONE);
                        addfarmer.setVisibility(View.VISIBLE);
                        lay_savenext.setVisibility(View.VISIBLE);

                        if(!ActualQTY.equalsIgnoreCase("0")){
                            //  btn_saveprint.setVisibility(View.VISIBLE);
                            btn_save.setVisibility(View.VISIBLE);
                        }else {
                            //  btn_saveprint.setVisibility(View.GONE);
                            btn_save.setVisibility(View.GONE);
                        }
                    }

                }else {

                }
            }
        });

        btn_back_fd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //INWHID = data.getStringExtra("INWHID");

                if(farmrsWithLotList.size() == 0){
                    btn_back.setText("Close");
                }else {
                    btn_back.setText("Save");
                }

                layout_add_farmer_details.setVisibility(View.GONE);
                addfarmer.setVisibility(View.VISIBLE);
                layout_farmers_list.setVisibility(View.VISIBLE);

                //MODE = "ADDNEW";

                //check INWD id is present in table or not if present then update that record if not then add that record
                if(listItems.isEmpty()){
                    //do not add just finish
                 //   finish();
                    list_farmers_details.setVisibility(View.GONE);
                }else {
                    if(MODE.equalsIgnoreCase("EDIT")){
                        checkUpdateAdd();
                    }else if(MODE.equalsIgnoreCase("ADDNEW")){
                        //add data in INWD table
                        for(int i =0; i< listItems.size(); i++){
                            methods.addINWD(INWDID,INWBID, INWHID,
                                    listItems.get(i).getItem_code().trim(),
                                    listItems.get(i).getItem_name().trim(),
                                    listItems.get(i).getLot().trim(),
                                    listItems.get(i).getQty_inwd().trim(),
                                    listItems.get(i).getFreight_inwd().trim(),
                                    SUPPNAME, SUPPID,listItems.get(i).getAdv_inwd().trim(),
                                    listItems.get(i).getVarani_inwd().trim());
                        }

                        getDataFramDatabase_farmersList();  //get added farmers list

                       /* Intent intent = new Intent();
                        intent.putExtra("INWBID", INWBID);
                        intent.putExtra("INWHID", INWHID);
                        setResult(1, intent);

                        finish();*/
                    }
                }

               // getDataFramDatabase_farmersList();  //get added farmers list
            }
        });

        img_search_farmer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(InvardEntryActivity.this, FarmersListActivity.class);
                startActivityForResult(intent,AdatDataEntryData.SUPPName);
            }
        });

        img_search_item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(InvardEntryActivity.this, ItemsListActivity.class);
                startActivityForResult(intent,AdatDataEntryData.ITEMName);
            }
        });

        btn_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(validate_fd()){
                    ITEMNAME = txt_itmname.getText().toString().trim();
                    ITEMCODE = txt_itmcode.getText().toString().trim();

                    /*if(edt_freight.getText().toString().equalsIgnoreCase("") || edt_freight.getText().toString().equalsIgnoreCase(null)){
                        edt_freight.setText("0.00");
                    }*/

                    if(txt_advance.getText().toString().equalsIgnoreCase("") || txt_advance.getText().toString().equalsIgnoreCase(null)){
                        txt_advance.setText("0.00");
                    }

                    if(txt_varani.getText().toString().equalsIgnoreCase("") || txt_varani.getText().toString().equalsIgnoreCase(null)){
                        txt_varani.setText("0.00");
                    }


                    INWBean inwBean = new INWBean();

                    inwBean.setInwh_id(INWHID);
                    inwBean.setInwb_id(INWBID);
                    inwBean.setInwd_id(INWDID);
                    inwBean.setFarmer_code(SUPPID);
                    inwBean.setFarmer_name(SUPPNAME);
                    inwBean.setFreight_inwd(String.format("%.2f",Float.valueOf(edt_freight_fd.getText().toString())));
                    inwBean.setAdv_inwd(String.format("%.2f",Float.valueOf(txt_advance.getText().toString())));
                    inwBean.setVarani_inwd(String.format("%.2f",Float.valueOf(txt_varani.getText().toString())));
                    inwBean.setItem_code(ITEMCODE);
                    inwBean.setItem_name(ITEMNAME);
                    //inwBean.setLot(String.format("%.2f",Float.valueOf(txt_lot.getText().toString())));
                    inwBean.setLot(txt_lot.getText().toString());
                    inwBean.setQty_inwd(String.format("%.2f",Float.valueOf(txt_qty.getText().toString())));

                    listItems.set(posToUpdate, inwBean);

                    itemsadapter = new ItemDetailsAdapter(parent, listItems);
                    list_farmers_details.setAdapter(itemsadapter);
                    setListViewHeightBasedOnChildren(list_farmers_details,1);
                    itemsadapter.notifyDataSetChanged();

                    txt_itmname.setText("");
                    txt_itmcode.setText("");
                    txt_lot.setText("");
                    txt_qty.setText("");
                    edt_freight_fd.setText("");

                    btn_add.setVisibility(View.VISIBLE);
                    btn_update.setVisibility(View.GONE);
                }else {
                    //do not add blank data
                }
            }
        });

        btn_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //add data in listview and show list to
                //apply validations if empty list do not add

                list_farmers_details.setVisibility(View.VISIBLE);

                if(validate_fd()){
                    ITEMNAME = txt_itmname.getText().toString().trim();
                    ITEMCODE = txt_itmcode.getText().toString().trim();

                   /* if(edt_freight_fd.getText().toString().equalsIgnoreCase("") || edt_freight_fd.getText().toString().equalsIgnoreCase(null)){
                        edt_freight_fd.setText("0.00");
                    }*/

                    if(txt_advance.getText().toString().equalsIgnoreCase("") || txt_advance.getText().toString().equalsIgnoreCase(null)){
                        txt_advance.setText("0.00");
                    }

                    if(txt_varani.getText().toString().equalsIgnoreCase("") || txt_varani.getText().toString().equalsIgnoreCase(null)){
                        txt_varani.setText("0.00");
                    }

                    INWBean inwBean = new INWBean();

                    inwBean.setInwh_id(INWHID);
                    inwBean.setInwb_id(INWBID);
                    inwBean.setInwd_id(INWDID);
                    inwBean.setFarmer_code(SUPPID);
                    inwBean.setFarmer_name(SUPPNAME);
                    inwBean.setFreight_inwd(String.format("%.2f",Float.valueOf(edt_freight_fd.getText().toString())));
                    inwBean.setAdv_inwd(String.format("%.2f",Float.valueOf(txt_advance.getText().toString())));
                    inwBean.setVarani_inwd(String.format("%.2f",Float.valueOf(txt_varani.getText().toString())));
                    inwBean.setItem_code(ITEMCODE);
                    inwBean.setItem_name(ITEMNAME);
                    //inwBean.setLot(String.format("%.2f",Float.valueOf(txt_lot.getText().toString())));
                    inwBean.setLot(txt_lot.getText().toString());
                    inwBean.setQty_inwd(String.format("%.2f",Float.valueOf(txt_qty.getText().toString())));

                    listItems.add(inwBean);

                    itemsadapter = new ItemDetailsAdapter(parent, listItems);
                    list_farmers_details.setAdapter(itemsadapter);
                    setListViewHeightBasedOnChildren(list_farmers_details,1);
                    itemsadapter.notifyDataSetChanged();

                    txt_itmname.setText("");
                    txt_itmcode.setText("");
                    txt_lot.setText("");
                    txt_qty.setText("");
                    edt_freight_fd.setText("");
                }else {
                    //do not add blank data
                }

                //getDataFramDatabase_fdtl(inwbID,inwdID); //get farmers items details list
            }
        });

        list_farmers_details.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //btn_add.setText("Update");
                btn_add.setVisibility(View.GONE);
                btn_update.setVisibility(View.VISIBLE);

                posToUpdate = position;
                itemToUpdate = listItems.get(position).getItem_code().trim();

                txt_itmname.setText(listItems.get(position).getItem_name().toString().trim());
                txt_itmcode.setText(listItems.get(position).getItem_code().toString().trim());
                txt_lot.setText(listItems.get(position).getLot().toString().trim());
                txt_qty.setText(listItems.get(position).getQty_inwd().toString().trim());
                edt_freight_fd.setText(listItems.get(position).getFreight_inwd().toString().trim());

                INWDID = listItems.get(position).getInwd_id();
                INWBID = listItems.get(position).getInwb_id();
                INWHID = listItems.get(position).getInwh_id();
                SUPPNAME = listItems.get(position).getFarmer_name();
                SUPPID = listItems.get(position).getFarmer_code();
            }
        });

       /* btn_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final Calendar c = Calendar.getInstance();
                year = c.get(Calendar.YEAR);
                month = c.get(Calendar.MONTH);
                day = c.get(Calendar.DAY_OF_MONTH);

                datePickerDialog = new DatePickerDialog(parent,
                        new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker datePicker, int year,
                                                  int monthOfYear, int dayOfMonth) {

                                datePicker.setMinDate(c.getTimeInMillis());

                                date = dayOfMonth + "-"
                                        + String.format("%02d", (monthOfYear + 1))
                                        + "-" + year;*//*

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                    // only for gingerbread and newer versions
                                    datePickerDialog.getDatePicker().setMinDate(c.getTimeInMillis()-1000);
                                    //   datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
                                }*//*

                                if (compare_date(date) == true) {
                                    btn_date.setText(date);
                                    DATE = btn_date.getText().toString().trim();
                                } else {
                                    btn_date.setText(date);
                                    DATE = btn_date.getText().toString().trim();
                                    Toast.makeText(parent,
                                            "You cannot select a day earlier than today!",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        }, year, month, day);
                datePickerDialog.setTitle("Select Date");
                datePickerDialog.show();
            }
        });

        btn_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;

                //  btntime.setText(hour+":"+ minute + " ");

                mTimePicker = new TimePickerDialog(parent,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker timePicker,
                                                  int selectedHour, int selectedMinute) {
                                time = methods.updateTime(selectedHour, selectedMinute);

                                if (date == null) {
                                    Toast.makeText(parent,
                                            "Please select date first!", Toast.LENGTH_SHORT).show();
                                } else {
                                    if (compare_datetime(date + " " + time, selectedHour, selectedMinute) == true) {
                                        btn_time.setText(time);
                                        TIME = btn_time.getText().toString().trim();
                                    } else {
                                        btn_time.setText(time);
                                        TIME = btn_time.getText().toString().trim();
                                        Toast.makeText(parent,
                                                "You cannot select a time earlier than current time!", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        }, hour, minute, true);// Yes 24 hour time
                mTimePicker.setTitle("Select Time");
                mTimePicker.show();

            }
        });*/

        img_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(parent, TransportersListActivity.class);
                startActivityForResult(intent,AdatDataEntryData.HUNDName);
            }
        });

        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //INWHID = generateINWHID();

                /*connectDevice();
                // dailog
                dialog = new Dialog(parent);
                dialog.setContentView(R.layout.message_print);
                TextView txtMsg = (TextView) dialog.findViewById(R.id.textMsg);
                Button btnyes = (Button) dialog.findViewById(R.id.btn_yes);

                String text = "   Vritti Solutions Ltd. Pune  \n VRITTI \n TOKEN - 100";
                String data[] = text.split("\n");

                Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
                paint.setTextSize(23);
                paint.setColor(Color.BLACK);
                paint.setTextAlign(Paint.Align.CENTER);
                float baseline = - paint.ascent(); //ascent() is negative0
                int width = (int) (paint.measureText(text) + 0.5f); //round
                int height = (int) (baseline + paint.descent() + 0.5f);
                final Bitmap image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                final Rect bounds = new Rect();
                paint.getTextBounds(text, 0, text.length(), bounds);
                Canvas canvas = new Canvas(image);
                int xPos = (canvas.getWidth() / 2);
                int yPos = 500*//*(int) ((canvas.getHeight() / 2) - ((paint.descent() + paint.ascent()) / 2))*//* ;
                canvas.drawColor(-1);
                canvas.drawText(text, xPos, baseline, paint);

                if (deviceConnected) {
                    txtMsg.setText("Your device is connected to printer.");
                    Toast.makeText(parent, "Device connected to printer", Toast.LENGTH_SHORT).show();

                } else {
                    txtMsg.setText("Your device is not connected to printer, do you want to try again?");
                    //    createXml();
                    Toast.makeText(parent, "Device not connected to printer. Please try agin!", Toast.LENGTH_SHORT).show();
                    //   startService(new Intent(ItemListCB.this, MarchantService.class));
                }

                btnyes.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                        Toast.makeText(parent, "Connecting to device...", Toast.LENGTH_SHORT).show();
                        try {
                            if(image!=null){
                                byte[] command = Utils.decodeBitmap(image);
                           //    mService.write(command);
                                //printText(command);
                            }else{
                                Log.e("Print Photo error", "the file isn't exists");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.e("PrintTools", "the file isn't exists");
                        }
                    }
                });
                dialog.show();*/

               Intent intent = new Intent(parent, AddNewFarmerActivity.class);
               intent.putExtra("INWHID", INWHID);
               startActivityForResult(intent, AdatDataEntryData.INWBDATA);
            }
        });


        btn_savelocalprint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(BluetoothClass.isPrinterConnected(getApplicationContext(),InvardEntryActivity.this)) {
                    //BluetoothService mService = null;
                    mService = BluetoothClass.getServiceInstance();
                    print_CGST_SGST();
                }else {
                    BluetoothClass.connectPrinter(getApplicationContext(),InvardEntryActivity.this);
                }
            }
        });

        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

               // if(validate()){
                    TransporterNAME = txt_transporter.getText().toString().trim();
                    VehicleNO = edt_vehicleno.getText().toString().trim();
                    TotFreight = edt_freight.getText().toString().trim();
                    TotQTY_Bags = edt_totalqty.getText().toString().trim();
                    USERName = AdatDataEntryData.USERNAME.trim();
                    String DateTime = DATE + "_"+TIME;
                    TokenNO = edt_token.getText().toString().trim();

                    //add data in table
                    methods.addINWH(INWHID, DateTime, TransporterCODE, TransporterNAME, VehicleNO, TotFreight, TotQTY_Bags, ActualQTY, TokenNO, USERName);

                    createJSON();   //create JSON data and send to server

                /*}else {

                }*/

            }
        });

        btn_Scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(BluetoothClass.isPrinterConnected(getApplicationContext(),InvardEntryActivity.this)){
                    //BluetoothService mService = null;
                    mService = BluetoothClass.getServiceInstance();

                    btn_saveprint.setClickable(true);
                    btn_saveprint.setEnabled(true);
                    btn_saveprint.setAlpha(1);

                   /* String text = "   Vritti Solutions Ltd. Pune  \n VRITTI \n TOKEN - 100";
                    String data[] = text.split("\n");

                    Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
                    paint.setTextSize(23);
                    paint.setColor(Color.BLACK);
                    paint.setTextAlign(Paint.Align.CENTER);
                    float baseline = - paint.ascent(); //ascent() is negative0
                    int width = (int) (paint.measureText(text) + 0.5f); //round
                    int height = (int) (baseline + paint.descent() + 0.5f);
                    final Bitmap image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                    final Rect bounds = new Rect();
                    paint.getTextBounds(text, 0, text.length(), bounds);
                    Canvas canvas = new Canvas(image);
                    int xPos = (canvas.getWidth() / 2);
                    int yPos = (int) ((canvas.getHeight() / 2) - ((paint.descent() + paint.ascent()) / 2));
                    canvas.drawColor(-1);
                    canvas.drawText(text, xPos, baseline, paint);

                    if(image!=null){
                        byte[] command = Utils.decodeBitmap(image);
                            mService.write(command);
                        //printText(command);
                    }else{
                        Log.e("Print Photo error", "the file isn't exists");
                    }*/
                }else {
                    BluetoothClass.connectPrinter(getApplicationContext(),InvardEntryActivity.this);
                    btn_saveprint.setClickable(true);
                    btn_saveprint.setEnabled(true);
                    btn_saveprint.setAlpha(1);
                }

            //  connectDevice();
               /* // dailog
                dialog = new Dialog(parent);
                dialog.setContentView(R.layout.message_print);
                TextView txtMsg = (TextView) dialog.findViewById(R.id.textMsg);
                Button btnyes = (Button) dialog.findViewById(R.id.btn_yes);
                //Button btnno = (Button) dialog.findViewById(R.id.btn_no);

                *//*if(validate()){
                    TransporterNAME = txt_transporter.getText().toString().trim();
                    VehicleNO = edt_vehicleno.getText().toString().trim();
                    TotFreight = edt_freight.getText().toString().trim();
                    TotQTY_Bags = edt_totalqty.getText().toString().trim();
                    USERName = AdatDataEntryData.USERNAME.trim();
                    String DateTime = DATE + "_"+TIME;
                    TokenNO = edt_token.getText().toString().trim();
                    methods.addINWH(INWHID, DateTime, TransporterCODE, TransporterNAME, VehicleNO, TotFreight, TotQTY_Bags, ActualQTY, TokenNO);

                    // createJSON();   //create JSON data and send to server

                }else {

                }*//*

                if (deviceConnected) {
                    txtMsg.setText("Your device is connected to printer.");
                    Toast.makeText(parent, "Device connected to printer", Toast.LENGTH_SHORT).show();

                } else {
                    txtMsg.setText("Your device is not connected to printer, do you want to try again?");
                    Toast.makeText(parent, "Device not connected to printer. Please try agin!", Toast.LENGTH_SHORT).show();
                }

                btnyes.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                        Toast.makeText(parent, "Connecting to device...", Toast.LENGTH_SHORT).show();
                        connectDevice();

                        *//*if(validate()){
                            if(validate()){
                                TransporterNAME = txt_transporter.getText().toString().trim();
                                VehicleNO = edt_vehicleno.getText().toString().trim();
                                TotFreight = edt_freight.getText().toString().trim();
                                TotQTY_Bags = edt_totalqty.getText().toString().trim();
                                USERName = AdatDataEntryData.USERNAME.trim();

                                // createJSON();   //create JSON data and send to server
                                //print_CGST_SGST();


                            }else {
                                Toast.makeText(parent,"Please fill all details", Toast.LENGTH_SHORT).show();
                            }

                            dialog.dismiss();

                        }else {
                            Toast.makeText(parent,"Please fill all details", Toast.LENGTH_SHORT).show();
                        }*//*
                        //new SaveBillDetails().execute();  //send bill details to server...
                        //startService(new Intent(ItemListCB.this, MarchantService.class));
                    }
                });

                dialog.show();*/
            }
        });

        btn_saveprint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(BluetoothClass.isPrinterConnected(getApplicationContext(),InvardEntryActivity.this)) {
                    //BluetoothService mService = null;
                    mService = BluetoothClass.getServiceInstance();
                    print_CGST_SGST();
                }else {
                    BluetoothClass.connectPrinter(getApplicationContext(),InvardEntryActivity.this);
                    if(BluetoothClass.isPrinterConnected(getApplicationContext(),InvardEntryActivity.this)){
                        mService = BluetoothClass.getServiceInstance();
                        print_CGST_SGST();
                    }
                }
            }
        });

        txt_qty.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Float ss = null;
                float pc = 0;
                float qty_ = 0f;
                float frgt_ = 0f;
                EditText edtqty = findViewById(R.id.txt_qty);

                if (((s.toString().trim() == "") || (s.toString() == null) || (s
                        .toString().length() == 0))) {

                    /*qty_ = Float.parseFloat(s.toString().trim());
                    frgt_ = frt_for_single_qty * qty_;

                    edt_freight_fd.setText(String.format("%.2f",frgt_)); //₹*/
                    edt_freight_fd.setText(""); //₹
                }
                else{

                    qty_ = Float.parseFloat(s.toString().trim());
                    frgt_ = frt_for_single_qty * qty_;

                    edt_freight_fd.setText(String.format("%.2f",frgt_)); //₹
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                Float ss = null;
                float pc = 0;
                float qty_ = 0f;
                float frgt_ = 0f;
                EditText edtqty = findViewById(R.id.txt_qty);

                if(s.toString().trim().equals("") || s.toString().trim().equals(null)){
                    edt_freight_fd.setText(""); //₹
                }else {
                    qty_ = Float.parseFloat(s.toString().trim());
                    frgt_ = frt_for_single_qty * qty_;

                    edt_freight_fd.setText(String.format("%.2f",frgt_)); //₹
                }
            }
        });
    }

    public static String generateINWHID() {
        String inwhID = UUID.randomUUID().toString();
        return inwhID;
    }

    public static String generateINWBID() {
        String inwbID = UUID.randomUUID().toString();
        return inwbID;
    }

    public static String generateINWDID() {
        String inwdID = UUID.randomUUID().toString();
        return inwdID;
    }

    public void getFirmDetails(){
        String query = "Select Name from "+dbhelper.TABLE_FIRM_MASTER;
        Cursor c = sqldb.rawQuery(query,null);
        if(c.getCount() > 0){
            c.moveToFirst();
            FIRMNAME = c.getString(c.getColumnIndex("Name"));
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        Calendar mcurrentTime = Calendar.getInstance();
        int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
        int minute = mcurrentTime.get(Calendar.MINUTE);

        time = methods.updateTime(hour, minute);
        btn_time.setText(time);
        TIME = btn_time.getText().toString().trim();
    }

    private void connectDevice() {
        // TODO
        String address = getBluetoothAddress(parent);
        if (address != null) {
            con_dev = mService.getDevByMac(address);
            mService.connect(con_dev);
            Log.e("Auto connected", "state : " + mService.getState());
        } else {
            scanBluetooth();
        }
    }

    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg1) {
            switch (msg1.what) {
                case BluetoothService.MESSAGE_STATE_CHANGE:
                    switch (msg1.arg1) {
                        case BluetoothService.STATE_CONNECTED: // ÒÑÁ¬½Ó
                            Toast.makeText(parent, "Connect successful",
                                    Toast.LENGTH_SHORT).show();
                            deviceConnected = true;
                            btn_saveprint.setAlpha(1);
                            btn_saveprint.setEnabled(true);
                            btn_saveprint.setClickable(true);
                            break;
                        case BluetoothService.STATE_CONNECTING: // ÕýÔÚÁ¬½Ó
                            Log.d("À¶ÑÀµ÷ÊÔ", "ÕýÔÚÁ¬½Ó.....");
                            break;
                        case BluetoothService.STATE_LISTEN: // ¼àÌýÁ¬½ÓµÄµ½À´
                        case BluetoothService.STATE_NONE:
                            Log.d("À¶ÑÀµ÷ÊÔ", "µÈ´ýÁ¬½Ó.....");
                            break;
                    }
                    break;
                case BluetoothService.MESSAGE_CONNECTION_LOST: // À¶ÑÀÒÑ¶Ï¿ªÁ¬½Ó
                    Toast.makeText(parent, "Device connection was lost",
                            Toast.LENGTH_SHORT).show();
                    deviceConnected = false;
                    break;
                case BluetoothService.MESSAGE_UNABLE_CONNECT: // ÎÞ·¨Á¬½ÓÉè±¸
                    Toast.makeText(parent, "Unable to connect device",
                            Toast.LENGTH_SHORT).show();
                    deviceConnected = false;
                    break;
            }
        }
    };

    public String getBluetoothAddress(Context parent) {
        Cursor cursor = sqldb.rawQuery("Select * from Bluetooth_Address", null);

        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            String str = cursor.getString(0);

            return str;

        } else {

            return null;
        }
    }

    private void scanBluetooth() {

        startActivityForResult(new Intent(parent, DeviceListActivity.class),
                AdatDataEntryData.REQUEST_CONNECT_DEVICE);
    }

    private void createJSON(){
        //INWH JSON
        JSONObject jsonINWH = new JSONObject();
        jsonINWHArray = new JSONArray();
        String dateTime = DATE + " "+TIME;

        try {
            jsonINWH.put("INWH_ID",INWHID.trim());
            jsonINWH.put("_DateTime",dateTime);
            jsonINWH.put("AgentCode",TransporterCODE.trim());
            jsonINWH.put("AgentName",TransporterNAME.trim());
            jsonINWH.put("VehicleNo",VehicleNO.trim());
            jsonINWH.put("Freight",TotFreight.trim());
            jsonINWH.put("Bags",TotQTY_Bags.trim());
            jsonINWH.put("ActualQty",ActualQTY.trim());
            jsonINWH.put("TokenNo",TokenNO.trim());
            jsonINWH.put("UserName",AdatDataEntryData.USERNAME.trim());

            jsonINWHArray.put(jsonINWH);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        jMAin_INWH = jsonINWH;

        String inwh_table = dbhelper.TABLE_INWH.replace("_","");
        keyInwh = inwh_table;
        String inwhArray = jsonINWHArray.toString().trim();

        new PostTablesDataToServer().execute(inwh_table, inwhArray);

    }

    public JSONArray getINWD_Data(){
        String INWD_ID = "", INWB_ID = "", INWH_ID = "", ItemCode = "", ItemName = "", LOT = "",
                QTY = "", Freight = "", SUPP_Name = "", SUPP_Code = "",Advance="",Varani="";
        String query = "Select * from INWD_"+AdatDataEntryData.SHOPNO+" WHERE INWH_ID='"+INWHID+"' AND isUploaded='No'";
        Cursor c = sqldb.rawQuery(query,null);

        jsonINWDArray = new JSONArray();

        if(c.getCount() > 0){
            c.moveToFirst();
            do{
                INWD_ID = c.getString(c.getColumnIndex("INWD_ID"));
                INWB_ID = c.getString(c.getColumnIndex("INWB_ID"));
                INWH_ID = c.getString(c.getColumnIndex("INWH_ID"));
                ItemCode = c.getString(c.getColumnIndex("ItemCode"));
                ItemName = c.getString(c.getColumnIndex("ItemName"));
                LOT = c.getString(c.getColumnIndex("LOT"));
                QTY = c.getString(c.getColumnIndex("QTY"));
                Freight = c.getString(c.getColumnIndex("Freight"));
                SUPP_Name = c.getString(c.getColumnIndex("SUPP_Name"));
                SUPP_Code = c.getString(c.getColumnIndex("SUPP_Code"));
                Advance = c.getString(c.getColumnIndex("Advance"));
                Varani = c.getString(c.getColumnIndex("Varani"));

                JSONObject jsonINWD = new JSONObject();

                try {
                    jsonINWD.put("INWD_ID", INWD_ID);
                    jsonINWD.put("INWB_ID", INWB_ID);
                    jsonINWD.put("INWH_ID", INWH_ID);
                    jsonINWD.put("ItemCode", ItemCode);
                    jsonINWD.put("ItemName", ItemName);
                    jsonINWD.put("LOT", LOT);
                    jsonINWD.put("QTY", QTY);
                    jsonINWD.put("Freight", Freight);
                    jsonINWD.put("SUPP_Name", SUPP_Name);
                    jsonINWD.put("SUPP_Code", SUPP_Code);
                   /* jsonINWD.put("Advance", Advance);
                    jsonINWD.put("Varani", Varani);*/

                    jsonINWDArray.put(jsonINWD);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }while (c.moveToNext());

        }else {

        }
        return jsonINWDArray;
    }

    public JSONArray getINWB_Data(){
        String INWB_ID = "", INWH_ID = "", RCVD_Qty = "", Freight = "", SUPP_Name = "", SUPP_Code = "",Advance="",Varani="";
        String query = "Select * from INWB_"+AdatDataEntryData.SHOPNO+" WHERE INWH_ID='"+INWHID+"'";
        Cursor c = sqldb.rawQuery(query,null);

        jsonINWBArray = new JSONArray();

        if(c.getCount() > 0){
            c.moveToFirst();
            do{
                INWB_ID = c.getString(c.getColumnIndex("INWB_ID"));
                INWH_ID = c.getString(c.getColumnIndex("INWH_ID"));
                RCVD_Qty = c.getString(c.getColumnIndex("RCVD_Qty"));
                Freight = c.getString(c.getColumnIndex("Freight"));
                SUPP_Name = c.getString(c.getColumnIndex("SUPP_Name"));
                SUPP_Code = c.getString(c.getColumnIndex("SUPP_Code"));
                Advance = c.getString(c.getColumnIndex("Advance"));
                Varani = c.getString(c.getColumnIndex("Varani"));

                JSONObject jsonINWB = new JSONObject();

                try {
                    jsonINWB.put("INWB_ID", INWB_ID);
                    jsonINWB.put("INWH_ID", INWH_ID);
                    jsonINWB.put("RCVD_Qty", RCVD_Qty);
                    jsonINWB.put("Freight", Freight);
                    jsonINWB.put("SUPP_Name", SUPP_Name);
                    jsonINWB.put("SUPP_Code", SUPP_Code);
                    jsonINWB.put("Advance", Advance);
                    jsonINWB.put("Varani", Varani);

                    jsonINWBArray.put(jsonINWB);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }while (c.moveToNext());

        }else {

        }
        return jsonINWBArray;
    }

    private void print_CGST_SGST() {

        final byte[] ALIGN_LEFT = {0x1B, 0x61, 0};
        final byte[] ALIGN_CENTER = {0x1B, 0x61, 1};
        final byte[] ALIGN_RIGHT = {0x1B, 0x61, 2};

        String msg = null, company = "";

        TransporterNAME = txt_transporter.getText().toString().trim();
        VehicleNO = edt_vehicleno.getText().toString().trim();
        TotFreight = edt_freight.getText().toString().trim();
        TotQTY_Bags = edt_totalqty.getText().toString().trim();
        USERName = AdatDataEntryData.USERNAME.trim();
        String DateTime = DATE + "_"+TIME;
        TokenNO = edt_token.getText().toString().trim();

        /*yard name image print*/
       //String name = " के. डी. चौधरी डालिम, यार्ड पुणे ";
        String name = "व्रित्ती सोल्युशन्स लि. कोथरूड, पुणे";
       // String name = "Vritti Solutions Limited, Pune";

       // String msg1 = "\n " +name+" \n";
        String msg1 = "\n " +FIRMNAME+" \n";

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setTextSize(28);
        paint.setColor(Color.BLACK);
        paint.setFakeBoldText(true);
        paint.setTextAlign(Paint.Align.CENTER);
        float baseline = - paint.ascent(); //ascent() is negative0
        int width = (int) (paint.measureText(msg1) + 0.5f); //round
        // int width = 400;
        int height = (int) (baseline + paint.descent() + 0.5f);
        final Bitmap image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(image);
        int xPos = (canvas.getWidth() / 2);
        int yPos = (int) ((canvas.getHeight() / 2) - ((paint.descent() + paint.ascent()) / 2)) ;
        canvas.drawColor(-1);
        canvas.drawText(msg1, xPos, baseline, paint);

        if(image!=null){
            byte[] command = Utils.decodeBitmap(image);
            mService.write(command);
            //printText(command);
        }else{
            Log.e("Print Photo error", "the file isn't exists");
        }

        msg = null;
        //token print
        msg = "--------------------------------\n";
        msg += " Token -  "+TokenNO+" \n";
        msg += "--------------------------------";
        if (msg.length() > 0) {
            mService.sendMessage(msg + "", "GBK");
        }

        /*transporter image print*/
        TransporterNAME = txt_transporter.getText().toString().trim();
        String transporter = TransporterNAME.trim();
       /* if (transporter.length() > 32) {
            transporter = transporter.substring(0, 32);
        } else if (transporter.length() <= 32) {
            int diff = 32 - transporter.length();
            for (int i = 0; i < diff; i++) {
                transporter += " ";
            }
        }*/

        String msg2 = " "+transporter+"\n";

        Paint paint1 = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint1.setTextSize(25);
        paint1.setColor(Color.BLACK);
        paint1.setFakeBoldText(true);
        paint1.setTextAlign(Paint.Align.CENTER);
        float baseline1 = - paint1.ascent(); //ascent() is negative0
        int width1 = (int) (paint1.measureText(msg2) + 0.5f); //round
        // int width = 400;
        int height1 = (int) (baseline1 + paint1.descent() + 0.5f);
        final Bitmap image1 = Bitmap.createBitmap(width1, height1, Bitmap.Config.ARGB_8888);
        Canvas canvas1 = new Canvas(image1);
        int xPos1 = (canvas1.getWidth() / 2);
        int yPos1 = (int) ((canvas1.getHeight() / 2) - ((paint1.descent() + paint1.ascent()) / 2)) ;
        canvas1.drawColor(-1);
        canvas1.drawText(msg2, xPos1, baseline1, paint1);

        if(image1!=null){
            byte[] command = Utils.decodeBitmap(image1);
            mService.write(command);
            //printText(command);
        }else{
            Log.e("Print Photo error", "the file isn't exists");
        }

        msg = null;
        /*msg = "--------------------------------\n";
        msg += "         Token -  "+TokenNO+" \n";
        msg += "--------------------------------\n";*/
       // msg += "\n    "+TransporterNAME+"    \n";
        msg =  "\n Vehicle No.: "+VehicleNO+"\n";
        msg += " Farmers  :  "+FarmersCnt+"\n";
        msg += " Quantity :  "+TotQTY_Bags+"\n";    //32
        msg += " Freight  :  "+TotFreight+"\n";   //32
        msg += "--------------------------------\n";
        msg += " "+DATE+"  "+TIME+"\n";    //32
        msg += " User Name  : "+USERName+"\n";
        msg += "--------------------------------\n";
        msg += "     Thank You! Visit Again.    \n";
        msg += "________________________________\n";

        if (msg.length() > 0) {
            mService.sendMessage(msg + "\n", "GBK");
        }

        //createJSON();   //create JSON data and send to server

       finish();

        /*if(receiptflag == true && printFlag == true){
            finish();
        }else {

        }*/

    }

    public static String getCurrentDate() {
        Calendar c = Calendar.getInstance();
        System.out.println("Current time => " + c.getTime());

        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy hh:mm a");
        String formattedDate = df.format(c.getTime());
        return formattedDate;
    }

    public static boolean compare_date(String fromdate) {
        boolean b = false;
        SimpleDateFormat dfDate = new SimpleDateFormat("dd-MM-yyyy");

        today = dfDate.format(new Date());
        try {
            if ((dfDate.parse(today).before(dfDate.parse(fromdate)) ||
                    dfDate.parse(today).equals(dfDate.parse(fromdate)))) {
                b = true;
            } else {
                date = today;
                b = false;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return b;
    }

    public boolean compare_datetime(String fromdate, int selectedHour, int selectedMinute) {
        boolean b = false;
        SimpleDateFormat dfDate = new SimpleDateFormat("dd-MM-yyyy hh:mm a");

        todaysDate = dfDate.format(new Date());
        try {
            if ((dfDate.parse(todaysDate).before(dfDate.parse(fromdate)))) {
                b = true;
            } else if ((dfDate.parse(todaysDate).equals(dfDate.parse(fromdate)))) {
                time = methods.updateTime(selectedHour, selectedMinute);
                b = true;
            } else {
                Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);
                time = methods.updateTime(hour+2, minute);
                b = false;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return b;
    }

    class PostTablesDataToServer extends AsyncTask<String, Void, Void> {
        ProgressDialog progressDialog;
        String exceptionString = "ok";
        String fullURL = AdatDataEntryData.URL;
        String responsemsg = "";
        String[] params;
        String tabName = "";
        JSONArray jArr;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(parent);
            progressDialog.setMessage("Submitting Data to Server...");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(String... params) {

            try {
                SoapObject request = new SoapObject(AdatDataEntryData.NAMESPACE,
                        AdatDataEntryData.api_PostDataToServer);
                PropertyInfo propInfo = new PropertyInfo();
                propInfo.type = PropertyInfo.STRING_CLASS;
                request.addProperty("key", params[0]);
                request.addProperty("data", params[1]);
                tabName = params[0];
                jArr = new JSONArray(params[1]);

                SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
                envelope.dotNet = true;
                envelope.setOutputSoapObject(request);
                HttpTransportSE androidHttpTransport = new HttpTransportSE(
                        fullURL);
                androidHttpTransport.call(AdatDataEntryData.NAMESPACE+ AdatDataEntryData.api_PostDataToServer, envelope);

                SoapObject response = null;

                if (envelope.bodyIn instanceof SoapFault) {
                    String str = ((SoapFault) envelope.bodyIn).faultstring;
                    Log.i("", str);
                    exceptionString = "error";

                } else {

                    try{
                        response = (SoapObject) envelope.bodyIn;
                        responsemsg = response.getProperty(0).toString();
                        exceptionString = "ok";
                    }catch (Exception e){
                        e.printStackTrace();
                        exceptionString = "error";
                    }
                }

            } catch (Exception e) {
                exceptionString = "error";
                e.printStackTrace();
            }
            return null;

        }

        @Override
        protected void onPostExecute(Void result) {

            super.onPostExecute(result);
            progressDialog.dismiss();
            if (exceptionString == "error") {
                Toast.makeText(parent, "Data not submitted to server.", Toast.LENGTH_LONG).show();

                btn_savelocalprint.setVisibility(View.VISIBLE);
                btn_save.setVisibility(View.GONE);

                if(tabName.equalsIgnoreCase(keyInwh)){
                    for(int i =0; i< jArr.length(); i++){
                        try {
                            JSONObject jObj = jArr.getJSONObject(i);
                            String inwhid = jObj.getString("INWH_ID");

                            ContentValues values = new ContentValues();
                            values.put("isUploaded", "No");
                            sqldb.update(dbhelper.TABLE_INWH,values,"INWH_ID=?",new String[]{inwhid});

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }else if(tabName.equalsIgnoreCase(keyInwb)){
                    for(int i =0; i< jArr.length(); i++){
                        try {
                            JSONObject jObj = jArr.getJSONObject(i);
                            String inwbid = jObj.getString("INWB_ID");

                            ContentValues values = new ContentValues();
                            values.put("isUploaded", "No");
                            sqldb.update(dbhelper.TABLE_INWB,values,"INWB_ID=?",new String[]{inwbid});

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                }else if(tabName.equalsIgnoreCase(keyInwd)){
                    for(int i =0; i< jArr.length(); i++){
                        try {
                            JSONObject jObj = jArr.getJSONObject(i);
                            String inwdid = jObj.getString("INWD_ID");

                            ContentValues values = new ContentValues();
                            values.put("isUploaded", "No");
                            sqldb.update(dbhelper.TABLE_INWD,values,"INWD_ID=?",new String[]{inwdid});

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }

            } else {
                if(tabName.equalsIgnoreCase(keyInwh)){
                    valInwh = "Success";
                    //   Toast.makeText(parent,"INWH Data submitted successfully.", Toast.LENGTH_LONG).show();

                    //update INWH table isUploaded
                    for(int i =0; i< jArr.length(); i++){
                        try {
                            JSONObject jObj = jArr.getJSONObject(i);
                            String inwhid = jObj.getString("INWH_ID");

                            ContentValues values = new ContentValues();
                            values.put("isUploaded", "Yes");
                            sqldb.update(dbhelper.TABLE_INWH,values,"INWH_ID=?",new String[]{inwhid});

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    //INWB JSON
                    String inwb_table = dbhelper.TABLE_INWB.replace("_","");
                    keyInwb = inwb_table;
                    String inwbArray = getINWB_Data().toString().trim();
                    new PostTablesDataToServer().execute(inwb_table,inwbArray);

                }else if(tabName.equalsIgnoreCase(keyInwb)){
                    valInwb = "Success";
                    //    Toast.makeText(parent,"INWB Data submitted successfully.", Toast.LENGTH_LONG).show();

                    //update INWB table isUploaded
                    for(int i =0; i< jArr.length(); i++){
                        try {
                            JSONObject jObj = jArr.getJSONObject(i);
                            String inwbid = jObj.getString("INWB_ID");

                            ContentValues values = new ContentValues();
                            values.put("isUploaded", "Yes");
                            sqldb.update(dbhelper.TABLE_INWB,values,"INWB_ID=?",new String[]{inwbid});

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    //INWD JSON
                    String inwd_table = dbhelper.TABLE_INWD.replace("_","");
                    keyInwd = inwd_table;
                    String inwdArray = getINWD_Data().toString().trim();
                    new PostTablesDataToServer().execute(inwd_table, inwdArray);

                }else if(tabName.equalsIgnoreCase(keyInwd)){
                    valInwd = "Success";
                    //update INWD table isUploaded
                    for(int i =0; i< jArr.length(); i++){
                        try {
                            JSONObject jObj = jArr.getJSONObject(i);
                            String inwdid = jObj.getString("INWD_ID");

                            ContentValues values = new ContentValues();
                            values.put("isUploaded", "Yes");
                            sqldb.update(dbhelper.TABLE_INWD,values,"INWD_ID=?",new String[]{inwdid});

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    //    Toast.makeText(parent,"INWD Data submitted successfully.", Toast.LENGTH_LONG).show();
                }else {

                }

                if(valInwh.equalsIgnoreCase("Success") &&
                        valInwb.equalsIgnoreCase("Success") &&
                        valInwd.equalsIgnoreCase("Success")){
                    //print receipt
                    printFlag = true;
                    Toast.makeText(parent,"Data submitted successfully.", Toast.LENGTH_LONG).show();

                    /*if(printFlag == true){
                        print_CGST_SGST();
                    }*/
                    //  finish();
                    btn_save.setVisibility(View.GONE);
                    lay_savenext.setVisibility(View.GONE);
                    lay_scanprint.setVisibility(View.VISIBLE);

                }else {
                    //do not print
                    printFlag = false;
                }
            }
        }
    }

    public void checkUpdateAdd(){
        //delete from table, apply where inwhid and inwbid condition
       /* String query = "DELETE FROM INWD_"+AdatDataEntryData.SHOPNO+" WHERE INWD_ID='"+INWDID+"' AND INWB_ID='"+INWBID+"'";
        Cursor cursor = sqldb.rawQuery(query,null);
        sqldb.execSQL(query);*/

        sqldb.delete(dbhelper.TABLE_INWD, "INWD_ID=?",new String[]{INWDID});

        Cursor c = sqldb.rawQuery("SELECT * FROM INWD_"+AdatDataEntryData.SHOPNO+" WHERE INWD_ID='"+INWDID+"' AND INWB_ID='"+INWBID+"'",
                null);
        if(c.getCount() == 0){
            //add data in it
            //add data in INWD table
            for(int i =0; i< listItems.size(); i++){
                methods.addINWD(INWDID,INWBID, INWHID,
                        listItems.get(i).getItem_code().trim(),
                        listItems.get(i).getItem_name().trim(),
                        listItems.get(i).getLot().trim(),
                        listItems.get(i).getQty_inwd().trim(),
                        listItems.get(i).getFreight_inwd().trim(),
                        SUPPNAME, SUPPID,listItems.get(i).getAdv_inwd().trim(),
                        listItems.get(i).getVarani_inwd().trim());
            }

            /*Intent intent = new Intent();
            intent.putExtra("INWBID", INWBID);
            intent.putExtra("INWHID", INWHID);
            setResult(1, intent);

            finish();*/
        }else {
            //data present
             c.close();
        }

        getDataFramDatabase_farmersList();  //get added farmers list

    }

    public void getDataFramDatabase_fdtl(String inwbid,String inwdid){
        listItems.clear();

        int lot = 0;
        float lot1 = 0.0F, qty = 0.0F;
        String INWD_ID = "", INWB_ID ="", INWH_ID = "", ItemCode = "", ItemName = "", Freight = "", SUPP_Name = "", SUPP_Code = "",
        Advance="", Varani="";

        String query = "Select * from INWD_"+ AdatDataEntryData.SHOPNO+" WHERE INWH_ID='"+INWHID+"' AND INWB_ID='"+inwbid+"'";
        Cursor c = sqldb.rawQuery(query,null);
        if(c.getCount() > 0){
            c.moveToFirst();

            do{
                INWD_ID = c.getString(c.getColumnIndex("INWD_ID")).trim();
                INWDID = INWD_ID;
                INWH_ID = c.getString(c.getColumnIndex("INWH_ID")).trim();
                INWB_ID = c.getString(c.getColumnIndex("INWB_ID")).trim();
                ItemCode = c.getString(c.getColumnIndex("ItemCode")).trim();
                ItemName = c.getString(c.getColumnIndex("ItemName")).trim();
                Freight = c.getString(c.getColumnIndex("Freight")).trim();
                SUPP_Name = c.getString(c.getColumnIndex("SUPP_Name")).trim();
                SUPP_Code = c.getString(c.getColumnIndex("SUPP_Code")).trim();
                lot1 = Float.parseFloat(c.getString(c.getColumnIndex("LOT")).trim());
                lot = (int)lot1;
                qty = Float.parseFloat(c.getString(c.getColumnIndex("QTY")).trim());
                Advance = c.getString(c.getColumnIndex("Advance")).trim();
                Varani = c.getString(c.getColumnIndex("Varani")).trim();

                INWBean inwBean = new INWBean();

                inwBean.setInwh_id(INWH_ID);
                inwBean.setInwb_id(INWB_ID);
                inwBean.setInwd_id(INWD_ID);
                inwBean.setFarmer_code(SUPP_Code);
                inwBean.setFarmer_name(SUPP_Name);
                inwBean.setFreight_inwd(Freight);
                inwBean.setAdv_inwd(Advance);
                inwBean.setAdv_inwb(Advance);
                inwBean.setVarani_inwd(Varani);
                inwBean.setVarani_inwb(Varani);
                inwBean.setItem_code(ItemCode);
                inwBean.setItem_name(ItemName);
                inwBean.setSupp_name(SUPP_Name);
                inwBean.setSupp_code(SUPP_Code);
                inwBean.setLot(String.valueOf(lot));
                inwBean.setQty_inwd(String.format("%.2f",qty));

                listItems.add(inwBean);

            }while (c.moveToNext());

            txt_farmer.setText(SUPP_Name);
            txt_advance.setText(Advance);
            txt_varani.setText(Varani);
       //     edt_freight.setText(Freight);

            SUPPNAME = SUPP_Name;
            SUPPID = SUPP_Code;

            itemsadapter = new ItemDetailsAdapter(parent, listItems);
            list_farmers_details.setAdapter(itemsadapter);
            setListViewHeightBasedOnChildren(list_farmers_details,1);
            itemsadapter.notifyDataSetChanged();

        }else {
            Toast.makeText(parent,"No records to show. Add New Records", Toast.LENGTH_SHORT).show();
        }
    }

    public void getDataFramDatabase_farmersList(){
        farmrsWithLotList.clear();
        listItems.clear();  //to clear existing qty

        int totLot = 0;
        float totLot1 = 0.0F, totQty = 0.0F;
        String INWD_ID = "", INWB_ID ="", INWH_ID = "", ItemCode = "", ItemName = "", Freight = "", SUPP_Name = "", SUPP_Code = "",
        Advance="", Varani="";
        float frt_supp = 0f;

        String query = "Select DISTINCT INWB_ID, INWH_ID from INWD_"+ AdatDataEntryData.SHOPNO+" WHERE INWH_ID='"+INWHID+"'"; /*INWB_ID='"+INWBID+"' AND*/
        Cursor c = sqldb.rawQuery(query,null);
        if(c.getCount() > 0){
            c.moveToFirst();

            FarmersCnt = String.valueOf(c.getCount());

            do{
                INWH_ID = c.getString(c.getColumnIndex("INWH_ID")).trim();
                INWB_ID = c.getString(c.getColumnIndex("INWB_ID")).trim();

                String query1 = "Select sum(QTY) as TotQTY,sum(LOT) as TotLOT,sum(Freight) as TotFreight,* from INWD_"+ AdatDataEntryData.SHOPNO+" WHERE INWH_ID='"+INWH_ID+"' AND INWB_ID='"+INWB_ID+"' GROUP by INWB_ID";
                Cursor c1 = sqldb.rawQuery(query1, null);
                if(c1.getCount() > 0){
                    c1.moveToFirst();
                    do{
                        INWD_ID = c1.getString(c1.getColumnIndex("INWD_ID")).trim();
                        /*totLot = Float.parseFloat(c.getString(c.getColumnIndex("LOT")).trim());
                        totQty = Float.parseFloat(c.getString(c.getColumnIndex("QTY")).trim());*/
                        totLot1 = Float.parseFloat(c1.getString(c1.getColumnIndex("TotLOT")).trim());
                        totLot = (int)totLot1;
                        totQty = Float.parseFloat(c1.getString(c1.getColumnIndex("TotQTY")).trim());
                        frt_supp = Float.parseFloat(c1.getString(c1.getColumnIndex("TotFreight")).trim());
                        ItemCode = c1.getString(c1.getColumnIndex("ItemCode")).trim();
                        ItemName = c1.getString(c1.getColumnIndex("ItemName")).trim();
                        Freight = c1.getString(c1.getColumnIndex("Freight")).trim();
                        SUPP_Name = c1.getString(c1.getColumnIndex("SUPP_Name")).trim();
                        SUPP_Code = c1.getString(c1.getColumnIndex("SUPP_Code")).trim();
                        Advance = c1.getString(c1.getColumnIndex("Advance")).trim();
                        Varani = c1.getString(c1.getColumnIndex("Varani")).trim();

                          /*totLot += totLot;
                          totQty += totQty;*/

                    }while (c1.moveToNext());

                    INWBean inwBean = new INWBean();

                    inwBean.setInwh_id(INWH_ID);
                    inwBean.setInwb_id(INWB_ID);
                    inwBean.setInwd_id(INWD_ID);
                    inwBean.setFarmer_code(SUPP_Code);
                    inwBean.setFarmer_name(SUPP_Name);
                    //inwBean.setFreight_inwd(Freight);
                    inwBean.setFreight_inwd(String.valueOf(frt_supp));
                    //inwBean.setFreight_inwb(Freight);
                    inwBean.setFreight_inwb(String.valueOf(frt_supp));
                    inwBean.setItem_code(ItemCode);
                    inwBean.setItem_name(ItemName);
                    inwBean.setSupp_name(SUPP_Name);
                    inwBean.setSupp_code(SUPP_Code);
                    inwBean.setLot(String.valueOf(totLot));
                    inwBean.setQty_inwd(String.valueOf(totQty));
                    inwBean.setTotQty(String.valueOf(totQty));
                    inwBean.setAdv_inwb(Advance);
                    inwBean.setAdv_inwd(Advance);
                    inwBean.setVarani_inwb(Varani);
                    inwBean.setVarani_inwd(Varani);

                    farmrsWithLotList.add(inwBean);

                }else {

                }

            }while (c.moveToNext());

            if(farmrsWithLotList.size() == 0){
                btn_back.setText("Close");
            }else {
                btn_back.setText("Save");
            }

            farmerAdapter = new FarmersListAdapter(parent, farmrsWithLotList);
            list_farmers.setAdapter(farmerAdapter);
            setListViewHeightBasedOnChildren(list_farmers,1);

           // listItems.clear();  //to clear existing qty

        }else {
            Toast.makeText(parent,"No records to show. Add New Records", Toast.LENGTH_SHORT).show();
        }
    }

    public void setListViewHeightBasedOnChildren(ListView gridView, int columns) {
        ListAdapter listAdapter = gridView.getAdapter();
        if (listAdapter == null) {
            // pre-condition
            return;
        }

        int totalHeight = 0;
        int items = listAdapter.getCount();
        int rows = 0;

        View listItem = listAdapter.getView(0, null, gridView);
        listItem.measure(0, 0);
        totalHeight = listItem.getMeasuredHeight();

        float x = 1;
        if( items > columns ){
            x = items/columns;
            rows = (int) (x+0.5);
            totalHeight *= rows;
        }

        ViewGroup.LayoutParams params = gridView.getLayoutParams();
        params.height = totalHeight;
        gridView.setLayoutParams(params);
    }

    private boolean validate() {
        // TODO Auto-generated method stub
        if(txt_transporter.getText().toString().trim().equalsIgnoreCase("") ||
                txt_transporter.getText().toString().trim().equalsIgnoreCase(null)){
            Toast.makeText(parent,"Please select transporter.",Toast.LENGTH_SHORT).show();
            return false;
        }else if(edt_vehicleno.getText().toString().trim().equalsIgnoreCase("") ||
                edt_vehicleno.getText().toString().trim().equalsIgnoreCase(null)){
            Toast.makeText(parent,"Please enter vehicle no.",Toast.LENGTH_SHORT).show();
            return false;
        }else if(edt_freight.getText().toString().trim().isEmpty() && edt_freight.getText().toString().trim().isEmpty()){
            Toast.makeText(parent,"Please enter freight amount",Toast.LENGTH_SHORT).show();
            return false;
        }else if(edt_totalqty.getText().toString().trim().isEmpty() && edt_totalqty.getText().toString().trim().isEmpty()){
            Toast.makeText(parent,"Please enter total quantity",Toast.LENGTH_SHORT).show();
            return false;
        }else if(edt_actualqty.getText().toString().trim().isEmpty() && edt_actualqty.getText().toString().trim().isEmpty()){
            Toast.makeText(parent,"Actual quantity should be greater than 0.",Toast.LENGTH_SHORT).show();
            return false;
        }else if(edt_token.getText().toString().trim().isEmpty() && edt_token.getText().toString().trim().isEmpty()){
            Toast.makeText(parent,"Please enter token no.",Toast.LENGTH_SHORT).show();
            return false;
        }else if(btn_date.getText().toString().trim().isEmpty() && btn_date.getText().toString().trim().isEmpty()){
            Toast.makeText(parent,"Please enter token no.",Toast.LENGTH_SHORT).show();
            return false;
        }else if(btn_time.getText().toString().trim().isEmpty() && btn_time.getText().toString().trim().isEmpty()){
            Toast.makeText(parent,"Please enter token no.",Toast.LENGTH_SHORT).show();
            return false;
        }else {
            return true;
        }
    }

    private boolean validate_fd() {
        // TODO Auto-generated method stub
        if(txt_farmer.getText().toString().trim().equalsIgnoreCase("") ||
                txt_farmer.getText().toString().trim().equalsIgnoreCase(null)){
            Toast.makeText(parent,"Please select farmer.",Toast.LENGTH_SHORT).show();
            return false;
        }/*else if(edt_freight_fd.getText().toString().trim().equalsIgnoreCase("") ||
                edt_freight_fd.getText().toString().trim().equalsIgnoreCase(null)){
            Toast.makeText(parent,"Please enter freight amount.",Toast.LENGTH_SHORT).show();
            return false;
        }*/else if(txt_itmname.getText().toString().trim().equalsIgnoreCase("") ||
                txt_itmname.getText().toString().trim().equalsIgnoreCase(null)){
            Toast.makeText(parent,"Please select item.",Toast.LENGTH_SHORT).show();
            return false;
        }else if(txt_itmcode.getText().toString().trim().equalsIgnoreCase("") ||
                txt_itmcode.getText().toString().trim().equalsIgnoreCase(null)){
            Toast.makeText(parent,"Please select item.",Toast.LENGTH_SHORT).show();
            return false;
        }else if(txt_qty.getText().toString().trim().equalsIgnoreCase("") ||
                txt_qty.getText().toString().trim().equalsIgnoreCase(null)){
            Toast.makeText(parent,"Please enter quantity.",Toast.LENGTH_SHORT).show();
            return false;
        }else if(txt_lot.getText().toString().trim().equalsIgnoreCase("") ||
                txt_lot.getText().toString().trim().equalsIgnoreCase(null)){
            Toast.makeText(parent,"Please enter lot.",Toast.LENGTH_SHORT).show();
            return false;
        }else {
            return true;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String transporterName = "",transporter_accno = "";
        String farmerName = "",farmer_accno = "", itemDesc = "", itemcode = "";

        try{
            if (requestCode == AdatDataEntryData.HUNDName && resultCode == AdatDataEntryData.HUNDName) {
                transporterName = data.getStringExtra("HUNDName");
                transporter_accno = data.getStringExtra("HUNDAccno");

                TransporterNAME = transporterName.trim();
                TransporterCODE = transporter_accno.trim();

                txt_transporter.setText(transporterName);
            }else if(requestCode == AdatDataEntryData.INWBDATA && resultCode == AdatDataEntryData.INWBDATA){
                INWHID = data.getStringExtra("INWHID");
                ActualQTY = data.getStringExtra("ActualQTY");
                FarmersCnt = data.getStringExtra("TotFarmers");
                TotFreight = data.getStringExtra("TotFreight");

                edt_actualqty.setText(String.format("%.2f",Float.valueOf(ActualQTY)));
        //        edt_totalqty.setText(String.format("%.2f",Float.valueOf(ActualQTY)));
        //        edt_freight.setText(String.format("%.2f",Float.valueOf(TotFreight)));

                if(!ActualQTY.equalsIgnoreCase("0")){
                    //  btn_saveprint.setVisibility(View.VISIBLE);
                    btn_save.setVisibility(View.VISIBLE);
                }else {
                    //  btn_saveprint.setVisibility(View.GONE);
                    btn_save.setVisibility(View.GONE);
                }
            }else if (requestCode == AdatDataEntryData.SUPPName && resultCode == AdatDataEntryData.SUPPName) {
                farmerName = data.getStringExtra("SUPPName");
                farmer_accno = data.getStringExtra("SUPPAccno");

                SUPPNAME = farmerName;
                SUPPID = farmer_accno;

                String FarmerNAME = farmerName;

                txt_farmer.setText(farmerName);

            }else if(requestCode == AdatDataEntryData.ITEMName && resultCode == AdatDataEntryData.ITEMName){
                itemDesc = data.getStringExtra("ITEMDesc");
                itemcode = data.getStringExtra("ITEMCode");

                ITEMCODE = itemcode;
                ITEMNAME = itemDesc;

                txt_itmname.setText(itemDesc);
                txt_itmcode.setText(itemcode);

                layfixfarmer.setFocusable(false);
                layfixfarmer.setClickable(false);
                txt_farmer.setSelected(false);
                img_search_farmer.setVisibility(View.VISIBLE);
                txt_farmer.setEnabled(false);
                img_search_farmer.setEnabled(false);
                img_search_farmer.setClickable(false);
                img_search_farmer.setFocusableInTouchMode(false);
                img_search_farmer.setFocusable(false);
               /* edt_freight_fd.setClickable(false);
                edt_freight_fd.setEnabled(false);
                edt_freight_fd.setFocusable(false);
                edt_freight_fd.setFocusableInTouchMode(false);*/
                txt_advance.setClickable(false);
                txt_advance.setEnabled(false);
                txt_advance.setFocusable(false);
                txt_advance.setFocusableInTouchMode(false);
                txt_varani.setClickable(false);
                txt_varani.setEnabled(false);
                txt_varani.setFocusable(false);
                txt_varani.setFocusableInTouchMode(false);
            }/*else if(requestCode == AdatDataEntryData.REQUEST_ENABLE_BT *//*&& resultCode == AdatDataEntryData.REQUEST_ENABLE_BT*//*){
                if(resultCode == RESULT_OK){
                    Toast.makeText(parent, "Bluetooth open successful",
                            Toast.LENGTH_LONG).show();
                }

            }else if(requestCode == AdatDataEntryData.REQUEST_CONNECT_DEVICE *//*&& resultCode == AdatDataEntryData.REQUEST_CONNECT_DEVICE*//*){
                if(resultCode == RESULT_OK){
                    String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    clearTable(parent, "Bluetooth_Address");
                    methods.AddBluetooth(address);
                    con_dev = mService.getDevByMac(address);
                    mService.connect(con_dev);
                    Log.e("bluetooth state", "state : " + mService.getState());
                }

            }*/else if (requestCode == AdatDataEntryData.REQUEST_ENABLE_BT && resultCode == RESULT_OK) {
                //bluetooth enabled and request for showing available bluetooth devices
                // Toast.makeText(parent, "Bluetooth open successful", Toast.LENGTH_LONG).show();
                BluetoothClass.pairPrinter(getApplicationContext(),InvardEntryActivity.this);
            }else if (requestCode == AdatDataEntryData.REQUEST_CONNECT_DEVICE && resultCode == RESULT_OK) {
                //bluetooth device selected and request pairing with device
                String address = data.getExtras()
                        .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                BluetoothClass.pairedPrinterAddress(getApplicationContext(),InvardEntryActivity.this,address);
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();

        /*******************************************************/

        int COUNT = 0;

        String qr1 = "Select * from "+dbhelper.TABLE_INWH+" WHERE INWH_ID='"+INWHID+"'";
        Cursor c1 = sqldb.rawQuery(qr1,null);
        if(c1.getCount() > 0){
            c1.moveToFirst();
            //then finish screen.
            COUNT = c1.getCount();
            /*Intent intent = new Intent(parent, EntryTabsActivity.class);
            startActivity(intent);*/
            finish();
        }else {
            //delete record from SLB table
            COUNT = c1.getCount();
            sqldb.delete(dbhelper.TABLE_INWB,"INWH_ID=?", new String[]{INWHID});
            sqldb.delete(dbhelper.TABLE_INWD,"INWH_ID=?", new String[]{INWHID});
            // then finish screen.
            /*Intent intent = new Intent(parent, EntryTabsActivity.class);
            startActivity(intent);*/
            finish();
        }

        /*******************************************************/
    }
}