package com.dionys.mymap.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;
import com.dionys.mymap.R;
import com.dionys.mymap.db.DBHelper;
import com.dionys.mymap.entity.PathRecord;
import com.dionys.mymap.step.BindService;
import com.dionys.mymap.step.UpdateUiCallBack;
import com.dionys.mymap.util.HttpUtil;
import com.dionys.mymap.util.TimeUtil;
import com.dionys.mymap.util.LocUtil;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class MapActivity extends AppCompatActivity implements View.OnClickListener, SensorEventListener {

    private static final String TAG = "Main";
    private SensorManager mSensorManager;
    private Sensor mAcc;
    private Button settingButton;
    private Button locateButton;
    private Button startButton;
    private Button stopButton;
    private TextView speedText;
    private TextView timeText;
    private TextView distanceText;
    private TextView stepText;
    private MapView mMapView;
    private AMap aMap;
    private AMapLocationClient mLocationClient;
    private AMapLocationClientOption mLocationOption;
    private LocationSource.OnLocationChangedListener mListener;

    public static String SITTING = "0";
    public static String WALKING = "1";
    public static String RUNNING = "2";

    private double distance;
    private DBHelper mDatabaseHelper;
    // 记录当前的路径
    private PathRecord mPath;
    private long startTime;
    private long endTime;
    private LatLng currentLatLng;
    private Timer mTimer;
    private TimerTask mTimerTask;

    private PolylineOptions mPolyoptions;
    private Polyline mPolyline;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    if (timeText != null) {
                        timeText.setText(TimeUtil.getFormateTime(System.currentTimeMillis() - startTime));
                    }
                    break;
                case 1:
                    if(msg.arg1 != 0 && stepText != null) {
                        stepText.setText(msg.arg1 + "");
                    }
                    break;
                case 2:
                    String status = "";
                    Toast.makeText(getApplicationContext(),status,Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };


    public AMapLocationListener mAMapLocationListener = new AMapLocationListener() {
        @Override
        public void onLocationChanged(AMapLocation amapLocation) {

            if (amapLocation != null) {

                if (amapLocation.getErrorCode() == AMapLocation.LOCATION_SUCCESS) {
                    mListener.onLocationChanged(amapLocation);// 显示系统小蓝点,不写这一句无法显示到当前位置
                    // 更新当前的位置
                    LatLng lan = new LatLng(amapLocation.getLatitude(), amapLocation.getLongitude());
                    if (currentLatLng != null) {
                        double dis = AMapUtils.calculateLineDistance(lan, currentLatLng);
                        distance += dis;
                        speedText.setText(String.format("%4.2f", amapLocation.getSpeed()));
                        distanceText.setText(String.format("%4.2f", distance / 1000));
                    }
                    currentLatLng = lan;

                    if (isRunning) {
                        mPath.addpoint(amapLocation);
                        mPolyoptions.add(currentLatLng);
                        drawLines();
                    }

                }

            } else {
                //定位失败时，可通过ErrCode（错误码）信息来确定失败的原因，errInfo是错误信息，详见错误码表。
                Log.e("AmapError", "location Error, ErrCode:"
                        + amapLocation.getErrorCode() + ", errInfo:" + amapLocation.getErrorInfo());
            }
        }

    };
    private boolean isRunning;
    private BindService bindService;
    private boolean isBind;


    private void drawLines() {
        if (mPolyoptions.getPoints().size() > 1) {
            if (mPolyline != null) {
                mPolyline.setPoints(mPolyoptions.getPoints());
            } else {
                mPolyline = aMap.addPolyline(mPolyoptions);
            }
        }
    }


    public LocationSource mLocationSource = new LocationSource() {
        @Override
        public void activate(OnLocationChangedListener onLocationChangedListener) {
            mListener = onLocationChangedListener;
            initAMapLocation();
        }

        @Override
        public void deactivate() {
            mListener = null;
            if (mLocationClient != null) {
                mLocationClient.stopLocation();
                mLocationClient.onDestroy();
            }
            mLocationClient = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //获取地图控件引用
        mMapView = findViewById(R.id.map);
        //在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，创建地图
        mMapView.onCreate(savedInstanceState);
        //初始化地图控制器对象
        if (aMap == null) {
            aMap = mMapView.getMap();
            if (currentLatLng != null) {
                aMap.animateCamera(CameraUpdateFactory.
                                newCameraPosition(new CameraPosition(currentLatLng, 250, 0, 0)),
                        500, null);
            }
        }

        setupMap();
        init();
//        testInit();
    }


    // 设置map的属性
    private void setupMap() {
        UiSettings uiSettings = aMap.getUiSettings();
        // 指南针
        uiSettings.setCompassEnabled(false);

        // 缩放按钮
        uiSettings.setZoomControlsEnabled(false);
        // 比例尺
        uiSettings.setScaleControlsEnabled(true);
        // 默认定位按钮
        uiSettings.setMyLocationButtonEnabled(false);
        uiSettings.setLogoBottomMargin(-200);
        // 设置定位小兰点
        MyLocationStyle myLocationStyle = new MyLocationStyle();

        myLocationStyle.strokeColor(Color.argb(0, 0, 0, 0));// 设置圆形的边框颜色
        myLocationStyle.radiusFillColor(Color.argb(0, 0, 0, 0));// 设置圆形的填充颜色

        aMap.setMyLocationStyle(myLocationStyle);
        aMap.setLocationSource(mLocationSource);// 设置定位监听

        // 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
        aMap.setMyLocationEnabled(true);


    }

    private void init() {

        // 设置状态栏颜色及字体颜色
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            // 设置状态栏透明
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

            // 设置状态栏字体黑色
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        settingButton = findViewById(R.id.button_setting);
        locateButton = findViewById(R.id.button_locate);
        startButton = findViewById(R.id.button_start);
        stopButton = findViewById(R.id.button_stop);
        speedText = findViewById(R.id.text_speed);
        distanceText = findViewById(R.id.text_distance);
        timeText = findViewById(R.id.text_time);
        stepText = findViewById(R.id.step_count);
        startButton.setOnClickListener(this);
        stopButton.setOnClickListener(this);
        settingButton.setOnClickListener(this);
        locateButton.setOnClickListener(this);

        speedText.setText("00:00");
        timeText.setText("00:00:00");
        distanceText.setText("0.00");
//        stepText.setText("0");

        mPolyoptions = new PolylineOptions();
        mPolyoptions.width(10);
        mPolyoptions.color(Color.GREEN);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAcc = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    private void initAMapLocation() {
        //初始化定位
        mLocationClient = new AMapLocationClient(this);
        //设置定位回调监听
        mLocationClient.setLocationListener(mAMapLocationListener);
        //初始化AMapLocationClientOption对象
        mLocationOption = new AMapLocationClientOption();
        // 设置定位场景，目前支持三种场景（签到、出行、运动，默认无场景）
        mLocationOption.setLocationPurpose(AMapLocationClientOption.AMapLocationPurpose.Transport);
        //设置定位模式为AMapLocationMode.Hight_Accuracy，高精度
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        //设置定位间隔,单位毫秒,默认为2000ms，最低1000ms。
        mLocationOption.setInterval(2000);
        //设置是否返回地址信息（默认返回地址信息）
        mLocationOption.setNeedAddress(true);
        //单位是毫秒，默认30000毫秒，建议超时时间不要低于8000毫秒。
        mLocationOption.setHttpTimeOut(20000);
        if (null != mLocationClient) {
            mLocationClient.setLocationOption(mLocationOption);
            //设置场景模式后最好调用一次stop，再调用start以保证场景模式生效
            mLocationClient.startLocation();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，销毁地图
        mMapView.onDestroy();

        if(isBind){
            unbindService(serviceConnection);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView.onResume ()，重新绘制加载地图
        mMapView.onResume();
        mSensorManager.registerListener(this, mAcc, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView.onPause ()，暂停地图的绘制
        mMapView.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，保存地图当前的状态
        mMapView.onSaveInstanceState(outState);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_locate:
                Log.d(TAG, "button_locate..");
                location();
                break;

            case R.id.button_setting:
                Log.d(TAG, "button_settig..");
                Intent intent = new Intent(MapActivity.this, RecordActivity.class);
                startActivity(intent);
                break;

            case R.id.button_start:
                start();
                break;

            case R.id.button_stop:
                stop();
                break;
        }
    }

    private void stop() {
        if (stopButton.getVisibility() == View.VISIBLE) {
            stopButton.setVisibility(View.INVISIBLE);
            startButton.setVisibility(View.VISIBLE);
        }
        endTime = System.currentTimeMillis();
        isRunning = false;
        save();
        unbindService(serviceConnection);
        distance = 0;
        Log.d(TAG, "button_stop");
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        if (mTimerTask != null) {
            mTimerTask.cancel();
            mTimerTask = null;
        }
        // new
//        aMap.clear();

    }

    private void start() {
        if (mPath != null) {
            mPath = null;
        }
        aMap.clear(true);
        mPath = new PathRecord();
        distance = 0;
        isRunning = true;
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd  HH:mm:ss", Locale.CHINA);
        mPath.setDate(df.format(new Date()));
        Toast.makeText(getApplicationContext(), "开始时间:" + mPath.getDate(), Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(MapActivity.this, BindService.class);

        // 开始计步
        isBind =  bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        Log.d("isbind",""+isBind);
        startService(intent);

        startTime = System.currentTimeMillis();
        Log.d(TAG, "button_start..");
        if (startButton.getVisibility() == View.VISIBLE) {
            startButton.setVisibility(View.INVISIBLE);
            stopButton.setVisibility(View.VISIBLE);
        }
        speedText.setText("0.00");
        distanceText.setText("0.00");
        // 计时
        if (mTimer == null) {
            mTimer = new Timer();
        }
        if (mTimerTask == null) {
            mTimerTask = new TimerTask() {
                @Override
                public void run() {
                    Message message = new Message();
                    message.what = 0;
                    mHandler.sendMessage(message);
                }
            };
        }
        mTimer.schedule(mTimerTask, 0, 1000);

    }

    // 保存到本地数据库
    protected void save() {
        List<AMapLocation> list = mPath.getPathline();
        if (list != null && list.size() > 0) {
            mDatabaseHelper = new DBHelper(this);
            mDatabaseHelper.open();
            String duration = getDuration();
            double distance = getDistance();
            String average = getAverage(distance);
            String pathlineSring = LocUtil.getPathLineString(list);
            AMapLocation firstLocaiton = list.get(0);
            AMapLocation lastLocaiton = list.get(list.size() - 1);
            String stratpoint = LocUtil.amapLocationToString(firstLocaiton);
            String endpoint = LocUtil.amapLocationToString(lastLocaiton);
            mDatabaseHelper.createrecord(String.valueOf(distance), duration, average,
                    pathlineSring, stratpoint, endpoint, mPath.getDate());
            mDatabaseHelper.close();
            Toast.makeText(getApplicationContext(), "保存记录成功!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(MapActivity.this, "距离过短..", Toast.LENGTH_SHORT).show();
        }
    }


    //和绷定服务数据交换的桥梁，可以通过IBinder service获取服务的实例来调用服务的方法或者数据

    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override

        public void onServiceConnected(ComponentName name, IBinder service) {

            BindService.LcBinder lcBinder = (BindService.LcBinder) service;

            bindService = lcBinder.getService();

            bindService.registerCallback(new UpdateUiCallBack() {

                @Override

                public void updateUi(int stepCount) {

                    //当前接收到stepCount数据，就是最新的步数

                    Message message = Message.obtain();

                    message.what = 1;

                    message.arg1 = stepCount;

                    mHandler.sendMessage(message);

                    Log.i("MapActivity—updateUi", "当前步数" + stepCount);
//                    Toast.makeText(getApplicationContext(), "步数:" + stepCount, Toast.LENGTH_SHORT).show();
                }

            });

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }


    };

    public String sendMessage(float x, float y, float z){
        String s = "";

        JSONObject object =new JSONObject();
        object.put("x",x);
        object.put("y",y);
        object.put("z",z);


        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(HttpUtil.getUrl())
                .post(RequestBody.create(HttpUtil.JASON,object.toJSONString()))
                .build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MapActivity.this, "服务器错误", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                final String res = response.body().string();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                    // TODO 响应成功


                    }


                });

            }
        });

        return s;
    }


    private double getDistance() {
        return distance;
    }

    private String getDuration() {
        return String.valueOf((endTime - startTime) / 1000);
    }

    private String getAverage(double distance) {
        return String.valueOf(distance / (endTime - startTime));
    }

    private void location() {
        if (currentLatLng == null) {
            Toast.makeText(getApplicationContext(), "正在搜索位置...", Toast.LENGTH_SHORT).show();
        } else {
            aMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(currentLatLng, 250, 0, 0)), 500, null);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // 传感器发生变化
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
//            sendMessage(x,y,z);
//            Toast.makeText(getApplicationContext(),x+","+y+","+z,Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}