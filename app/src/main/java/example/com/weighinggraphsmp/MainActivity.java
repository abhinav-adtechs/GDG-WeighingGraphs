package example.com.weighinggraphsmp;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.FillFormatter;
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.gson.Gson;
import com.gordonwong.materialsheetfab.MaterialSheetFab;

import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements
        View.OnClickListener, SwipeRefreshLayout.OnRefreshListener{

    private LineChart mChart;
    private String TAG = "MAINACTIVITY";

    private ChartElementsList chartElementsList ;
    private ChartElementsList.ChartElement[] listChartElement ;

    private Spinner dateSpinner ;
    private TextView tvStartDate ;
    private TextView tvStopDate ;
    private ImageButton btnStartPicker ;
    private ImageButton btnStopPicker ;
    private Button plotPickerButton ;

    private DatePickerDialog datePickerDialog;
    private int mYear;
    private int mMonth;
    private int mDay;
    private int selectedDay;
    private int selectedMonth;
    private int selectedYear;
    private Dialog dialogPicker ;

    private Toolbar tbActionBar ;

    private SwipeRefreshLayout swipeRefreshLayout ;

    private FabModified fabModified ;
    private MaterialSheetFab<FabModified> materialSheetFab ;
    private View overlayView ;
    private View sheetView ;

    private int plotChoice = 0 ;

    private AlertDialog.Builder alertDialog ;
    /**
     * 0 - Weight against time
     * 1 - Air Quality against time
     * 2 - Temperature against time
     * */


    //private Typeface tf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        setSupportActionBar(tbActionBar);


        dialogPicker = new Dialog(this);
        dialogPicker.setContentView(R.layout.dialog_date) ;

        init() ;
        performChart();

        //dialogPicker.show();
        /*
                .setPositiveButton("Plot this!", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .setNegativeButton("Plot default", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        performChart();
                    }
                })
                .show() ;*/

    }

    private void init() {
        tbActionBar = (Toolbar) findViewById(R.id.toolbar) ;
        tbActionBar.setTitle("Weighing Graphs");

        tvStartDate = (TextView) dialogPicker.findViewById(R.id.date_tv_start) ;
        tvStopDate = (TextView) dialogPicker.findViewById(R.id.date_tv_stop) ;

        btnStartPicker = (ImageButton) dialogPicker.findViewById(R.id.date_btn_picker_start) ;
        btnStopPicker = (ImageButton) dialogPicker.findViewById(R.id.date_btn_picker_stop) ;

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout) ;

        fabModified = (FabModified) findViewById(R.id.floating_action_button) ;
        overlayView = findViewById(R.id.activity_main_overlay) ;
        sheetView = findViewById(R.id.fab_sheet) ;

        plotPickerButton = (Button) findViewById(R.id.main_plotX_btn) ;

        plotPickerButton.setOnClickListener(this);
        btnStartPicker.setOnClickListener(this);
        btnStopPicker.setOnClickListener(this);

        swipeRefreshLayout.setOnRefreshListener(this);


        int sheetColor = getResources().getColor(R.color.fadedBlack);
        int fabColor = getResources().getColor(R.color.dullRed);

        // Initialize material sheet FAB
        materialSheetFab = new MaterialSheetFab<>(fabModified, sheetView, overlayView,
                sheetColor, fabColor);


    }

    @Override
    public void onBackPressed() {
        if (materialSheetFab.isSheetVisible()) {
            materialSheetFab.hideSheet();
        } else {
            super.onBackPressed();
        }

    }

    private void handleItemSelected(int pos) {
        setData(chartElementsList.getChartElements().size(), 100, pos);

    }

    private void performChart() {

        mChart = (LineChart) findViewById(R.id.chart1);
        mChart.setViewPortOffsets(0, 20, 0, 0);
        mChart.setBackgroundColor(getResources().getColor(R.color.fadedBlack));
        mChart.setDescription("Weight against date plot");

        mChart.setTouchEnabled(true);
        mChart.setAutoScaleMinMaxEnabled(true) ;
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);
        mChart.setPinchZoom(false);
        mChart.setDrawGridBackground(false);

        XAxis x = mChart.getXAxis();
        x.setEnabled(false);

        YAxis y = mChart.getAxisLeft();
        y.setLabelCount(9, false);
        y.setTextColor(Color.DKGRAY);
        y.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART);
        y.setDrawGridLines(false);
        y.setAxisLineColor(Color.YELLOW);

        mChart.getAxisRight().setEnabled(false);

        fetchData() ;
        //setData(chartElementsList.getChartElements().size(), 100, plotChoice);
        // add data


        mChart.getLegend().setEnabled(false);

        mChart.animateXY(2000, 2000);

        // don't forget to refresh the drawing
        mChart.invalidate();



    }

    private void fetchData() {

        String API_URL = "http://gdgvit.cloudapp.net:5000/show" ;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, API_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.v("res ok", response);
                        handleResponse(response) ;
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Failed to fetch Json" + error) ;
            }
        });
        int socketTimeout = 30000000;
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        stringRequest.setRetryPolicy(policy);


        AppController.getInstance().addToRequestQueue(stringRequest);
    }

    private void handleResponse(String response) {

        int count = 0 ;

        Gson gson = new Gson() ;
        chartElementsList  = gson.fromJson(response, ChartElementsList.class);


        for (int i = 0 ; i< chartElementsList.getChartElements().size() ; i++){

            ExtractDateTime(i, chartElementsList.getChartElements().get(i).getTime());

            //chartElementsList.getChartElements().get(i).setAirQuality(Float.toString(Float.parseFloat(chartElementsList.getChartElements().get(i).getAirQuality())/10));

            Log.d(TAG, "INSIDE called with: " +
                    count++ + " " +
                    chartElementsList.getChartElements().get(i).getAirQuality() + " " +
                    chartElementsList.getChartElements().get(i).getDate() + " " +
                    chartElementsList.getChartElements().get(i).getWeight() + " " +
                    chartElementsList.getChartElements().get(i).getTime() );
        }
        setData(chartElementsList.getChartElements().size(), 100, plotChoice);


    }

    private void ExtractDateTime(int i, String time) {
        chartElementsList.getChartElements().get(i).setDate(time.substring(0, 10).substring(8,10));
        chartElementsList.getChartElements().get(i).setTime(time.substring(12).substring(0,1));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return true;
    }


    private void setData(int count, float range, int plotChoiceLocal) {

        ArrayList<String> xVals = new ArrayList<String>();
        for (int i = 0; i < count; i++) {
            xVals.add((20 +i) + "");
        }

        ArrayList<Entry> vals1 = new ArrayList<Entry>();

        listChartElement = new ChartElementsList.ChartElement[chartElementsList.getChartElements().size()] ;
        for (int i = 0; i < chartElementsList.getChartElements().size(); i++) {
            listChartElement[i] = chartElementsList.getChartElements().get(i) ;
        }
        Arrays.sort(listChartElement) ;


        /*for (int i = 0; i < listChartElement.length; i++) {
            listChartElement[i].printData();
        }*/

        switch (plotChoiceLocal){
            case 0:
                for(int i=0 ; i<19 ; i++){
                    vals1.add(new Entry(Float.parseFloat(chartElementsList.getChartElements().get(i).getWeight()),
                            i)) ;
                }
                break;
            case 1:
                for(int i=0 ; i<19 ; i++){
                    vals1.add(new Entry(Float.parseFloat(chartElementsList.getChartElements().get(i).getAirQuality()),
                            i)) ;
                }
                break;
            case 2:
                for(int i=0 ; i<19 ; i++){
                    vals1.add(new Entry(Float.parseFloat(chartElementsList.getChartElements().get(i).getTemperature()),
                            i)) ;
                }
                break;

        }

        /*for(int i=0 ; i<19 ; i++){
            vals1.add(new Entry(Float.parseFloat(chartElementsList.getChartElements().get(i).getAirQuality()),
                    Integer.parseInt(chartElementsList.getChartElements().get(i).getDate()))) ;
        }*/




        // create a dataset and give it a type
        LineDataSet set1 = new LineDataSet(vals1, "DataSet 1");
        set1.setDrawCubic(true);
        set1.setCubicIntensity(0.3f);
        set1.setDrawFilled(true);
        set1.setDrawCircles(true);
        set1.setLineWidth(1.8f);
        set1.setCircleRadius(4f);
        set1.setCircleColor(getResources().getColor(R.color.fadedBlack));
        set1.setHighLightColor(Color.rgb(244, 117, 117));
        set1.setColor(Color.WHITE);
        set1.setFillColor(getResources().getColor(R.color.dullRed));
        set1.setFillAlpha(255);
        set1.setDrawHorizontalHighlightIndicator(false);
        set1.setFillFormatter(new FillFormatter() {
            @Override
            public float getFillLinePosition(ILineDataSet dataSet, LineDataProvider dataProvider) {
                return -10;
            }
        });

        // create a data object with the datasets
        LineData data = new LineData(xVals, set1);
       // data.setValueTypeface(tf);
        data.setValueTextSize(9f);
        data.setDrawValues(false);

        // set data
        mChart.setData(data);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.date_btn_picker_start:
                dialogPicker.dismiss();
                break;
            case R.id.date_btn_picker_stop:
                datePickerDialog = new DatePickerDialog(getApplicationContext(),
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                                selectedDay = dayOfMonth;
                                selectedMonth = monthOfYear;
                                selectedYear = year;

                                Toast.makeText(getApplicationContext(),"Start Date: " +
                                        selectedDay + "/" + selectedMonth,Toast.LENGTH_SHORT).show();
                            }
                        }, mYear, mMonth, mDay);
                //datePickerDialog.show();
                break;
            case R.id.main_plotX_btn:

                String names[] ={"Weight","Air Quality","Temperature"};

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Plot time against")
                        .setItems(names, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                setData(chartElementsList.getChartElements().size(), 100, which);
                            }
                        });
                builder.create();
                builder.show() ;
                /**
                * Get a choice return
                * */
                fetchData();
                break;
        }


    }

    @Override
    public void onRefresh() {
        swipeRefreshLayout.setRefreshing(true);
        fetchData();
    }


}
