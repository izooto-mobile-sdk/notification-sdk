package com.momagic;


import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.provider.Settings;
import android.text.Html;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.text.HtmlCompat;


import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import static com.momagic.ShortpayloadConstant.TAG;


public class Util {

    private static String CIPHER_NAME = "AES/CBC/PKCS5PADDING";
    private static int CIPHER_KEY_LEN = 16;

    public static String decrypt(String key, String data) {
        try {
            if (key.length() < CIPHER_KEY_LEN) {
                int numPad = CIPHER_KEY_LEN - key.length();

                StringBuilder keyBuilder = new StringBuilder(key);
                for (int i = 0; i < numPad; i++) {
                    keyBuilder.append("0"); //0 pad to len 16 bytes
                }
                key = keyBuilder.toString();

            } else if (key.length() > CIPHER_KEY_LEN) {
                key = key.substring(0, CIPHER_KEY_LEN); //truncate to 16 bytes
            }

            String[] parts = data.split(":");

            IvParameterSpec iv = new IvParameterSpec(android.util.Base64.decode(parts[1], android.util.Base64.DEFAULT));
            SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("ISO-8859-1"), "AES");

            Cipher cipher = Cipher.getInstance(CIPHER_NAME);
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);

            byte[] decodedEncryptedData = android.util.Base64.decode(parts[0], android.util.Base64.DEFAULT);

            byte[] original = cipher.doFinal(decodedEncryptedData);

            return new String(original);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static boolean getNetworkState(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }

    public static Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream input = connection.getInputStream();
                return BitmapFactory.decodeStream(input);
            }
            return null;
        } catch (IOException e) {
            // Log exception
            return null;
        }
    }

    public static String getAndroidId(Context mContext){
        String android_id = Settings.Secure.getString(mContext.getContentResolver(), Settings.Secure.ANDROID_ID);
        Log.v(TAG, "android id ---- "+android_id );
        return android_id;
    }

    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.toLowerCase().startsWith(manufacturer.toLowerCase())) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }

    public static boolean isNotificationEnabled(Context context){
        return NotificationManagerCompat.from(context).areNotificationsEnabled();
    }

    private static String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }

    public static String getSDKVersion(Context context) {
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }

    boolean hasFCMLibrary() {
        try {
            return com.google.firebase.messaging.FirebaseMessaging.class != null;
        } catch (Throwable e) {
            return false;
        }
    }

    public boolean isInitializationValid() {
        checkForFcmDependency();
        return true;
    }

    boolean checkForFcmDependency() {
        if (!hasFCMLibrary()) {
            Lg.d(AppConstant.APP_NAME_TAG, AppConstant.CHECKFCMLIBRARY);
            return false;
        }
        return true;

    }
    public static String trimString(String optString) {
        if(optString.length()>32)
        {
            int length=optString.length()-32;
            return optString.substring(0,length);
        }
        return null;
    }

    public static Drawable getApplicationIcon(Context context){
        ApplicationInfo ai;
        Drawable icon = null;
        try {
            ai = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
        } catch ( PackageManager.NameNotFoundException e ) {
            ai = null;
            //e.printStackTrace();
        }

        if ( ai != null ) {
            icon =  context.getPackageManager().getApplicationIcon(ai);
        }


        return icon;
    }
    public static boolean hasHMSLibraries() {
        return hasHMSAGConnectLibrary() && hasHMSPushKitLibrary();
    }
    private static boolean hasHMSAGConnectLibrary() {
        try {
            return com.huawei.agconnect.config.AGConnectServicesConfig.class != null;
        } catch (NoClassDefFoundError e) {
            return false;
        }
    }
    private static boolean hasHMSPushKitLibrary() {
        try {
            return com.huawei.hms.aaid.HmsInstanceId.class != null;
        } catch (NoClassDefFoundError e) {
            return false;
        }
    }

    public static boolean CheckValidationString(String optString) {
        if(optString.length()>32)
        {
            return true;
        }
        else
        {
            return false;
        }
    }
    public static String getDeviceLanguage()
    {
        //return Locale.getDefault().getDisplayLanguage();
        Locale locale;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            locale = DATB.appContext.getResources().getConfiguration().getLocales().get(0);
        } else {
            locale = DATB.appContext.getResources().getConfiguration().locale;
        }
        // Log.e("lanuguage",locale.getCountry());
        return locale.getDisplayLanguage();

    }
    public static String getIntegerToBinary(int number)
    {
        return String.format("%16s", Integer.toBinaryString(number)).replace(' ', '0');

    }
    public static boolean checkNotificationEnable()
    {
        return NotificationManagerCompat.from(DATB.appContext).areNotificationsEnabled();

    }
    public static String getPackageName(Context context) {
        ApplicationInfo ai;
        try {
            ai = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            ai = null;
            //e.printStackTrace();
        }
        return context.getPackageName();
    }
    public static boolean isMatchedString(String s) {
        try {
            Pattern pattern= Pattern.compile("[a-zA-Z0-9-_.~%]{1,900}");
            Matcher matcher = pattern.matcher(s);
            return matcher.matches();
        } catch (RuntimeException e) {
            return false;
        }
    }
    public static int convertStringToDecimal(String number){
        char[] numChar = number.toCharArray();
        int intValue = 0;
        int decimal = 1;
        for(int index = numChar.length ; index > 0 ; index --){
            if(index == 1 ){
                if(numChar[index - 1] == '-'){
                    return intValue * -1;
                } else if(numChar[index - 1] == '+'){
                    return intValue;
                }
            }
            intValue = intValue + (((int)numChar[index-1] - 48) * (decimal));
            System.out.println((int)numChar[index-1] - 48+ " " + (decimal));
            decimal = decimal * 10;
        }
        return intValue;
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static String getDeviceLanguageTag()
    {
        //return Locale.getDefault().getDisplayLanguage();
        Locale locale;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            locale = DATB.appContext.getResources().getConfiguration().getLocales().get(0);
        } else {
            locale = DATB.appContext.getResources().getConfiguration().locale;
        }
        // Log.e("lanuguage",locale.getCountry());
        return locale.getDefault().toLanguageTag();

    }
    public static String getTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd");
        String currentDate = sdf.format(new Date());
        return currentDate;
    }
    public static CharSequence  makeBoldString(CharSequence title) {
        if (Build.VERSION.SDK_INT >= 24) {
            title = Html.fromHtml("<font color=\"" + ContextCompat.getColor(DATB.appContext, R.color.black) + "\"><b>"+title+"</b></font>", HtmlCompat.FROM_HTML_MODE_LEGACY);// for 24 api and more
        } else {
            title = Html.fromHtml("<font color=\"" + ContextCompat.getColor(DATB.appContext, R.color.black) + "\"><b>"+title+"</b></font>"); // or for older api
        }
        return title;
    }

    public static CharSequence makeBlackString(CharSequence title) {
        if (Build.VERSION.SDK_INT >= 24) {
            title = Html.fromHtml("<font color=\"" + ContextCompat.getColor(DATB.appContext, R.color.black) + "\">"+title+"</font>", HtmlCompat.FROM_HTML_MODE_LEGACY); // for 24 api and more
        } else {
            title = Html.fromHtml("<font color=\"" + ContextCompat.getColor(DATB.appContext, R.color.black) + "\">"+title+"</font>"); // or for older api
        }
        return title;
    }
    public static Bitmap makeCornerRounded(Bitmap image){
        Bitmap imageRounded = Bitmap.createBitmap(image.getWidth(), image.getHeight(), image.getConfig());
        Canvas canvas = new Canvas(imageRounded);
        Paint mpaint = new Paint();
        mpaint.setAntiAlias(true);
        mpaint.setShader(new BitmapShader(image, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));
        canvas.drawRoundRect((new RectF(0.0f, 0.0f, image.getWidth(), image.getHeight())), 10, 10, mpaint);
        return imageRounded;
    }
    public static void sleepTime(int time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
        }
    }
    public static String dayDifference(String currentDate, String previousDate){
        if (previousDate.isEmpty())
            return "";
        String dayDifference = "";
        try {
            Date date1;
            Date date2;
            SimpleDateFormat dates = new SimpleDateFormat("yyyy.MM.dd");
            date1 = dates.parse(currentDate);
            date2 = dates.parse(previousDate);
            long difference = date1.getTime() - date2.getTime();
            long differenceDates = difference / (24 * 60 * 60 * 1000);
            dayDifference = Long.toString(differenceDates);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return dayDifference;
    }
    public static int getBinaryToDecimal(int cfg){
        String fourthDg, fifthDg, sixthDg;

        String data = Util.getIntegerToBinary(cfg);
        if(data!=null && !data.isEmpty()) {
            fourthDg = String.valueOf(data.charAt(data.length() - 4));
            fifthDg = String.valueOf(data.charAt(data.length() - 5));
            sixthDg = String.valueOf(data.charAt(data.length() - 6));
        }else {
            fourthDg = "0";
            fifthDg = "0";
            sixthDg = "0";
        }
        String dataCFG = sixthDg + fifthDg + fourthDg;
        int decimalData = Integer.parseInt(dataCFG,2);
        return decimalData;
    }

}
