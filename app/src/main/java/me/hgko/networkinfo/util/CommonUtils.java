package me.hgko.networkinfo.util;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import me.hgko.networkinfo.service.LocationFusedService;
import me.hgko.networkinfo.service.LocationDataService;
import me.hgko.networkinfo.service.MobileService;
import me.hgko.networkinfo.service.NCubeConnectionService;
import me.hgko.networkinfo.service.SynchronizeService;
import me.hgko.networkinfo.service.WeatherService;
import me.hgko.networkinfo.service.WirelessService;

public class CommonUtils {

    public CommonUtils() {
    }

    // TODO: 인터넷 연결 확인
    public static boolean checkInternetConnection(Context context) {
        ConnectivityManager connec = (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);
        if (connec.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.CONNECTED ||
                connec.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.CONNECTING ||
                connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.CONNECTING ||
                connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.CONNECTED) {
            return true;
        } else if (connec.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.DISCONNECTED ||
                connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.DISCONNECTED) {
            return false;
        }
        return false;
    }

    // TODO: 백그라운드 서비스가 실행 중인지 확인 중입니다.
    public static boolean isMyServiceRunning(Class<?> serviceClass, Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static boolean checkPermission(final Context context) {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED;
    }

    // TODO: 백그라운드 서비스 시작 또는 중지
    public static void controlBackgroundServices(Context context, Integer flag) {
        // TODO: Changing state of Services depends on variable named flag
        List<Class<?>> serviceList = getServiceClassList();
        for (Class<?> serviceClass : serviceList) {
            switch (flag) {
                case 0:
                    stopServiceOnce(serviceClass, context);
                    break;
                case 1:
                    startServiceOnce(serviceClass, context);
                    break;
                default:
                    break;
            }

            Log.d("networkLog", (flag == 1 ? "started => " : "stopped => ") + serviceClass.getName());
        }

        // TODO: BroadCast Receiver의 상태 변경은 broadCast Flag라는 변수에 따라 다릅니다.
        List<Class<?>> receieverList = getRecieverClassList();
        if (receieverList.size() > 0) {
            int broadCastFlag = (flag == 1) ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                    : PackageManager.COMPONENT_ENABLED_STATE_DISABLED;

            for (Class<?> reciever : receieverList) {
                ComponentName component = new ComponentName(context, reciever);
                context.getPackageManager().setComponentEnabledSetting(component, broadCastFlag, PackageManager.DONT_KILL_APP);
                Log.d("networkLog", (flag == 1 ? "started => " : "stopped => ") + reciever.getName());
            }
        }
    }

    // TODO: 실행 중이 아니면 백그라운드 서비스 시작
    public static boolean startServiceOnce(Class<?> serviceClass, Context context) {
        try {
            if (!isMyServiceRunning(serviceClass, context)) {
                context.startService(new Intent(context, serviceClass));
            }
            return true;
        } catch (Exception e) {
            Log.d("serviceStart exception", e.toString());
        }
        return false;
    }

    // TODO: 실행중인 경우 백그라운드 서비스 중지
    public static boolean stopServiceOnce(Class<?> serviceClass, Context context) {
        try {
            if (isMyServiceRunning(serviceClass, context)) {
                context.stopService(new Intent(context, serviceClass));
            }
            return true;
        } catch (Exception e) {
            Log.d("serviceStop exception", e.toString());
        }
        return false;
    }

    // TODO: 서비스 목록 가져오기
    public static List<Class<?>> getServiceClassList() {
        List<Class<?>> serviceList = new ArrayList<>();
        serviceList.add(NCubeConnectionService.class);
        serviceList.add(SynchronizeService.class);
        serviceList.add(MobileService.class);
        serviceList.add(WirelessService.class);
//        serviceList.add(LocationDataService.class);
        serviceList.add(WeatherService.class);
        serviceList.add(LocationFusedService.class);
        return serviceList;
    }

    // TODO: 리시버 목록 가져오기
    public static List<Class<?>> getRecieverClassList() {
        List<Class<?>> receieverList = new ArrayList<>();
//        receieverList.add(DetectConnectionReceiver.class);
        return receieverList;
    }

    // TODO: Get required permissions by arrayString
    public static String[] getPermissions() {
        String[] permissions = new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.INTERNET,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.RECEIVE_BOOT_COMPLETED
        };
        return permissions;
    }

    /**
     *
     * @param context
     * @param lat
     * @param lng
     * @return
     */
    public static String getAddressByLocation(Context context, double lat, double lng) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            Address obj = addresses.get(0);
            String add = obj.getAddressLine(0);
            return add;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 위도,경도로 주소구하기
     * @param lat
     * @param lng
     * @return 주소
     */
    public static String getAddress(Context mContext, double lat, double lng) {
        String nowAddress = "현재 위치를 확인 할 수 없습니다.";
        Geocoder geocoder = new Geocoder(mContext, Locale.KOREA);
        try {
            if (geocoder != null) {
                //세번째 파라미터는 좌표에 대해 주소를 리턴 받는 갯수로
                //한좌표에 대해 두개이상의 이름이 존재할수있기에 주소배열을 리턴받기 위해 최대갯수 설정
                List<Address> address = geocoder.getFromLocation(lat, lng, 1);
                if (address != null && address.size() > 0) {
                    // 주소 받아오기
                    String currentLocationAddress = address.get(0).getAddressLine(0).toString();
                    nowAddress = currentLocationAddress;
                }
            }
        } catch (IOException e) {
            Toast.makeText(mContext, "주소를 가져 올 수 없습니다.", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
        return nowAddress;
    }

    /**
     * 주소로부터 위치정보 취득
     * @param address 주소
     */
    public static Location findGeoPoint(Context mcontext, String address) {
        Location loc = new Location("");
        Geocoder coder = new Geocoder(mcontext);
        List<Address> addr = null;// 한좌표에 대해 두개이상의 이름이 존재할수있기에 주소배열을 리턴받기 위해 설정

        try {
            addr = coder.getFromLocationName(address, 5);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }// 몇개 까지의 주소를 원하는지 지정 1~5개 정도가 적당
        if (addr != null) {
            for (int i = 0; i < addr.size(); i++) {
                Address lating = addr.get(i);
                double lat = lating.getLatitude(); // 위도가져오기
                double lon = lating.getLongitude(); // 경도가져오기
                loc.setLatitude(lat);
                loc.setLongitude(lon);
            }
        }
        return loc;
    }

    public static AlertDialog showAlertDlg(Activity activity, String title, String msg) {
        // 1. Instantiate an AlertDialog.Builder with its constructor
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(activity,
                android.R.style.Theme_DeviceDefault_Light_Dialog));

        // 2. Chain together various setter methods to set the dialog characteristics
        builder.setMessage(msg).setTitle(title);
        return builder.create();
    }
}
