package com.momagic;



import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.text.HtmlCompat;

import com.momagic.shortcutbadger.ShortcutBadger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Random;
public class NotificationEventManager {
    private static Bitmap notificationIcon, notificationBanner;//,act1Icon,act2Icon;
    private static int icon;
    private static  int badgeColor;
    private static int priority,lockScreenVisibility;
    private static boolean addCheck;

    public static void manageNotification(Payload payload) {
        if (payload.getFetchURL() == null || payload.getFetchURL().isEmpty())
            showNotification(payload);
        else
            addCheck = true;
        processPayload(payload);

    }

    private static void processPayload(final Payload payload) {
        RestClient.get(payload.getFetchURL(), new RestClient.ResponseHandler() {
            @Override
            void onSuccess(String response) {
                super.onSuccess(response);
                if (response != null) {
                    try {

                        Object json = new JSONTokener(response).nextValue();
                        if(json instanceof JSONObject)
                        {
                            JSONObject jsonObject = new JSONObject(response);
                            parseJson(payload,jsonObject);

                        }
                        else if(json instanceof  JSONArray)
                        {
                            JSONArray jsonArray=new JSONArray(response);
                            JSONObject jsonObject=new JSONObject();
                            jsonObject.put("",jsonArray);
                            parseJson(payload,jsonObject);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            void onFailure(int statusCode, String response, Throwable throwable) {
                super.onFailure(statusCode, response, throwable);

            }
        });
    }


    private static void parseJson(Payload payload, JSONObject jsonObject) {
        try {
            payload.setLink(getParsedValue(jsonObject, payload.getLink()));
            if (!payload.getLink().startsWith("http://") && !payload.getLink().startsWith("https://")) {
                String url = payload.getLink();
                url = "http://" + url;
                payload.setLink(url);

            }
            payload.setBanner(getParsedValue(jsonObject, payload.getBanner()));
            payload.setTitle(getParsedValue(jsonObject, payload.getTitle()));
            payload.setMessage(getParsedValue(jsonObject, payload.getMessage()));
            payload.setIcon(getParsedValue(jsonObject, payload.getIcon()));
            payload.setAct1name(getParsedValue(jsonObject,payload.getAct1name()));
            payload.setAct1link(getParsedValue(jsonObject,payload.getAct1link()));
            if (!payload.getAct1link().startsWith("http://") && !payload.getAct1link().startsWith("https://")) {
                String url = payload.getAct1link();
                url = "http://" + url;
                payload.setAct1link(url);

            }
            payload.setAp("");
            payload.setInapp(0);
            if(payload.getTitle()!=null && !payload.getTitle().equalsIgnoreCase("")) {
                showNotification(payload);
                Log.e("Notification Send","Yes");
            }
            else {
                Log.e("Notification Send","No");
            }



        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getParsedValue(JSONObject jsonObject, String sourceString) {
        try {
            if (sourceString.startsWith("~"))
                return sourceString.replace("~", "");
            else {
                if (sourceString.contains(".")) {
                    JSONObject jsonObject1 = null;
                    String[] linkArray = sourceString.split("\\.");
                    if(linkArray.length==2 || linkArray.length==3)
                    {
                        for (int i = 0; i < linkArray.length; i++) {
                            if (linkArray[i].contains("[")) {
                                String[] linkArray1 = linkArray[i].split("\\[");

                                if (jsonObject1 == null)
                                    jsonObject1 = jsonObject.getJSONArray(linkArray1[0]).getJSONObject(Integer.parseInt(linkArray1[1].replace("]", "")));
                                else {
                                    jsonObject1 = jsonObject1.getJSONArray(linkArray1[0]).getJSONObject(Integer.parseInt(linkArray1[1].replace("]", "")));

                                }

                            } else {
                                return jsonObject1.optString(linkArray[i]);
                            }

                        }
                    }
//                    else if(linkArray.length==3)
//                    {
//                            if (linkArray[1].contains("[")) {
//                                String[] link1 = linkArray[1].split("\\[");
//
//                                jsonObject1 = jsonObject1.getJSONObject(linkArray[0]).getJSONObject(linkArray[1]).getJSONArray(link1[0]).getJSONObject(Integer.parseInt(link1[1].replace("]", "")));
//                                Log.e("ABC1", jsonObject1.toString());
//                                return jsonObject1.getString(linkArray[2]);
//
//
//                            } else {
//
//                                return jsonObject.getString(sourceString);
//                            }
//
//
//                    }
                    else if(linkArray.length==4)
                    {
                        if (linkArray[2].contains("[")) {
                            String[] linkArray1 = linkArray[2].split("\\[");
                            if(jsonObject1==null) {
                                jsonObject1 = jsonObject.getJSONObject(linkArray[0]).getJSONObject(linkArray[1]).getJSONArray(linkArray1[0]).getJSONObject(Integer.parseInt(linkArray1[1].replace("]", "")));
                            }
                            else
                                jsonObject1 = jsonObject.getJSONObject(linkArray[0]).getJSONObject(linkArray[1]).getJSONArray(linkArray1[0]).getJSONObject(Integer.parseInt(linkArray1[1].replace("]", "")));

                            return jsonObject1.getString(linkArray[3]);

                        }

                    }
                    else if(linkArray.length==5)
                    {
                        if (linkArray[2].contains("[")) {
                            String[] link1 = linkArray[2].split("\\[");
                            if (jsonObject1 == null)
                                jsonObject1 = jsonObject.getJSONObject(linkArray[0]).getJSONObject(linkArray[1]).getJSONArray(link1[0]).getJSONObject(Integer.parseInt(link1[1].replace("]", ""))).getJSONObject(linkArray[3]);
                            else
                                jsonObject1 = jsonObject1.getJSONObject(linkArray[0]).getJSONObject(linkArray[1]).getJSONArray(link1[0]).getJSONObject(Integer.parseInt(link1[1].replace("]", ""))).getJSONObject(linkArray[3]);


                            return jsonObject1.optString(linkArray[4]);
                        }
                    }
                    else
                    {
                        jsonObject.getString(sourceString);
                    }


                } else
                    return jsonObject.getString(sourceString);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private static void showNotification(final Payload payload) {
        if (addCheck){
            receivedNotification(payload);
        }else {

            if (isAppInForeground(DATAB.appContext)){
                if (DATAB.inAppOption==null || DATAB.inAppOption.equalsIgnoreCase(AppConstant.NOTIFICATION_)){
                    receivedNotification(payload);
                }else if (DATAB.inAppOption.equalsIgnoreCase(AppConstant.INAPPALERT)){
                    showAlert(payload);
                }
            }else {
                receivedNotification(payload);
            }
        }
    }

    private static void receivedNotification(final Payload payload){
        final Handler handler = new Handler(Looper.getMainLooper());
        final Runnable notificationRunnable = new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void run() {

                String clickIndex = "0";
                String impressionIndex ="0";

                String data=Util.getIntegerToBinary(payload.getCfg());
                if(data!=null && !data.isEmpty()) {
                    clickIndex = String.valueOf(data.charAt(data.length() - 2));
                    impressionIndex = String.valueOf(data.charAt(data.length() - 1));
                }
                else
                {
                    clickIndex = "0";
                    impressionIndex="0";
                }

                badgeCountUpdate(payload.getBadgeCount());


                String channelId = DATAB.appContext.getString(R.string.default_notification_channel_id);
                NotificationCompat.Builder notificationBuilder = null;
                Notification summaryNotification = null;
                int SUMMARY_ID = 0;
                Intent intent = null;
                if (DATAB.icon!=0)
                {
                    icon= DATAB.icon;
                }
                else
                {
                    if (payload.getBadgeicon().equalsIgnoreCase(AppConstant.DEFAULT_ICON)){
                        icon=R.drawable.ic_notifications_black_24dp;
                    }else {

                        if (isInt(payload.getBadgeicon())){
                            icon = DATAB.appContext.getApplicationInfo().icon;
                        }else {
                            int checkExistence = DATAB.appContext.getResources().getIdentifier(payload.getBadgeicon(), "drawable", DATAB.appContext.getPackageName());
                            if ( checkExistence != 0 ) {  // the resource exists...
                                icon = checkExistence;

                            }
                            else {  // checkExistence == 0  // the resource does NOT exist!!
                                int checkExistenceMipmap = DATAB.appContext.getResources().getIdentifier(payload.getBadgeicon(), "mipmap", DATAB.appContext.getPackageName());
                                if ( checkExistenceMipmap != 0 ) {  // the resource exists...
                                    icon = checkExistenceMipmap;

                                }else {

                                    icon =R.drawable.ic_notifications_black_24dp;// iZooto.appContext.getApplicationInfo().logo;
                                }

                            }

                        }

                    }

                }





                if (payload.getBadgecolor().contains("#")){
                    try{
                        badgeColor = Color.parseColor(payload.getBadgecolor());
                    } catch(IllegalArgumentException ex){
                        // handle your exceptizion
                        badgeColor = Color.TRANSPARENT;
                        ex.printStackTrace();
                    }
                }else if (payload.getBadgecolor()!=null&&!payload.getBadgecolor().isEmpty()){
                    try{
                        badgeColor = Color.parseColor("#"+payload.getBadgecolor());
                    } catch(IllegalArgumentException ex){ // handle your exception
                        badgeColor = Color.TRANSPARENT;
                        ex.printStackTrace();
                    }
                }else {
                    badgeColor = Color.TRANSPARENT;
                }

                lockScreenVisibility = setLockScreenVisibility(payload.getLockScreenVisibility());

                intent = notificationClick(payload, payload.getLink(),payload.getAct1link(),payload.getAct2link(),AppConstant.NO,clickIndex,100,0);
                Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

                PendingIntent pendingIntent = PendingIntent.getBroadcast(DATAB.appContext, new Random().nextInt(100) /* Request code */, intent,
                        PendingIntent.FLAG_ONE_SHOT);

                notificationBuilder = new NotificationCompat.Builder(DATAB.appContext, channelId)
                        .setContentTitle(payload.getTitle())
                        .setSmallIcon(icon)
                        .setContentText(payload.getMessage())
                        .setContentIntent(pendingIntent)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(payload.getMessage()))
                        .setDefaults(NotificationCompat.DEFAULT_LIGHTS | NotificationCompat.DEFAULT_SOUND).setVibrate(new long[]{1000, 1000})
                        .setSound(defaultSoundUri)
                        .setVisibility(lockScreenVisibility)
                        .setAutoCancel(true);




                if (!(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)){

                        priority = priorityForLessOreo(payload.getPriority());
                        notificationBuilder.setPriority(priority);


                }
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
                    if (payload.getGroup() == 1) {

                        notificationBuilder.setGroup(payload.getGroupKey());

                        summaryNotification =
                                new NotificationCompat.Builder(DATAB.appContext, channelId)
                                        .setContentTitle(payload.getTitle())
                                        .setContentText(payload.getMessage())
                                        .setSmallIcon(icon)
                                        .setColor(badgeColor)
                                        .setStyle(new NotificationCompat.InboxStyle()
                                                .addLine(payload.getMessage())
                                                .setBigContentTitle(payload.getGroupMessage()))
                                        .setGroup(payload.getGroupKey())
                                        .setGroupSummary(true)
                                        .build();


                    }
                }

                if (!payload.getSubTitle().contains(AppConstant.NULL)&&payload.getSubTitle()!=null&&!payload.getSubTitle().isEmpty()) {
                    notificationBuilder.setSubText(payload.getSubTitle());

                }
                if (payload.getBadgecolor()!=null&&!payload.getBadgecolor().isEmpty()){
                    notificationBuilder.setColor(badgeColor);
                }
                if(payload.getLedColor()!=null && !payload.getLedColor().isEmpty())
                    notificationBuilder.setColor(Color.parseColor(payload.getLedColor()));
                if (notificationIcon != null)
                    notificationBuilder.setLargeIcon(notificationIcon);
                else if (notificationBanner != null)
                    notificationBuilder.setLargeIcon(notificationBanner);
                if (notificationBanner != null && !payload.getSubTitle().contains(AppConstant.NULL) && payload.getSubTitle()!=null&&!payload.getSubTitle().isEmpty()) {
                    notificationBuilder.setStyle(new NotificationCompat.BigPictureStyle()
                            .bigPicture(notificationBanner)
                            .bigLargeIcon(notificationIcon)/*.setSummaryText(payload.getMessage())*/);
                }else if (notificationBanner != null)
                {
                    notificationBuilder.setStyle(new NotificationCompat.BigPictureStyle()
                            .bigPicture(notificationBanner)
                            .bigLargeIcon(notificationIcon).setSummaryText(payload.getMessage()));

                }

                NotificationManager notificationManager =
                        (NotificationManager) DATAB.appContext.getSystemService(Context.NOTIFICATION_SERVICE);
                int notificaitionId = (int) System.currentTimeMillis();
                if (payload.getAct1name() != null && !payload
                        .getAct1name().isEmpty()) {
                    String phone = getPhone(payload.getAct1link());
                    Intent btn1 = notificationClick(payload,payload.getAct1link(),payload.getLink(),payload.getAct2link(),phone,clickIndex,notificaitionId,1);
                    PendingIntent pendingIntent1 = PendingIntent.getBroadcast(DATAB.appContext, new Random().nextInt(100), btn1, PendingIntent.FLAG_UPDATE_CURRENT);
                    NotificationCompat.Action action1 =
                            new NotificationCompat.Action.Builder(
                                    0,  HtmlCompat.fromHtml("<font color=\"" + ContextCompat.getColor(DATAB.appContext, R.color.gray) + "\">" + payload.getAct1name() + "</font>", HtmlCompat.FROM_HTML_MODE_LEGACY),
                                    pendingIntent1).build();
                    notificationBuilder.addAction(action1);


                }


                if (payload.getAct2name() != null && !payload.getAct2name().isEmpty()) {
//                    btn2.setAction(AppConstant.ACTION_BTN_TWO);
                    String phone = getPhone(payload.getAct2link());
                    Intent btn2 = notificationClick(payload,payload.getAct2link(),payload.getLink(),payload.getAct1link(),phone,clickIndex,notificaitionId,2);
                    PendingIntent pendingIntent2 = PendingIntent.getBroadcast(DATAB.appContext, new Random().nextInt(100), btn2, PendingIntent.FLAG_UPDATE_CURRENT);
                    NotificationCompat.Action action2 =
                            new NotificationCompat.Action.Builder(
                                    0, HtmlCompat.fromHtml("<font color=\"" + ContextCompat.getColor(DATAB.appContext, R.color.gray) + "\">" + payload.getAct2name() + "</font>", HtmlCompat.FROM_HTML_MODE_LEGACY),
                                    pendingIntent2).build();
                    notificationBuilder.addAction(action2);
                }
                assert notificationManager != null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {


                        priority = priorityForImportance(payload.getPriority());
                    NotificationChannel channel = new NotificationChannel(channelId,
                            AppConstant.CHANNEL_NAME, priority);

                    notificationManager.createNotificationChannel(channel);




                }
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
                    if (payload.getGroup() == 1) {
                        notificationManager.notify(SUMMARY_ID, summaryNotification);
                    }
                }
                notificationManager.notify(notificaitionId, notificationBuilder.build());
                try {

                    if(impressionIndex.equalsIgnoreCase("1")) {
                        viewNotificationApi(payload);
                    }
                    DATAB.notificationView(payload);

                } catch (Exception e) {
                    e.printStackTrace();
                }

                notificationBanner = null;
                notificationIcon = null;
                /*link = "";
                link1 = "";
                link2 = "";*/

            }

        };


        new AppExecutors().networkIO().execute(new Runnable() {
            @Override
            public void run() {
                String smallIcon = payload.getIcon();
                String banner = payload.getBanner();
                try {
                    if (smallIcon != null && !smallIcon.isEmpty())
                        notificationIcon = Util.getBitmapFromURL(smallIcon);
                    if (banner != null && !banner.isEmpty()) {
                        notificationBanner = Util.getBitmapFromURL(banner);

                    }
                    handler.post(notificationRunnable);
                } catch (Exception e) {
                    Lg.e("Error", e.getMessage());
                    e.printStackTrace();
                    handler.post(notificationRunnable);
                }
            }
        });
    }

    private static boolean isInt(String s)//1234
    {
        try
        {
           Integer.parseInt(s);//1234//what is use case variable i // Number format exception check kiya tha
        return true;
        }

        catch(NumberFormatException er)
        {
            return false;
        }

    }

    private static String getFinalUrl(Payload payload) {
        byte[] data = new byte[0];
        try {
            data = payload.getLink().getBytes(AppConstant.UTF);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String encodedLink = Base64.encodeToString(data, Base64.DEFAULT);
        Uri builtUri = Uri.parse(payload.getLink())
                .buildUpon()
                .appendQueryParameter(AppConstant.URL_ID, payload.getId())
                .appendQueryParameter(AppConstant.URL_CLIENT, payload.getKey())
                .appendQueryParameter(AppConstant.URL_RID, payload.getRid())
                .appendQueryParameter(AppConstant.URL_BKEY_, PreferenceUtil.getInstance(DATAB.appContext).getStringData(AppConstant.FCM_DEVICE_TOKEN))
                .appendQueryParameter(AppConstant.URL_FRWD___, encodedLink)
                .build();
        return builtUri.toString();
    }
    public static String decodeURL(String url)
    {


        if(url.contains(AppConstant.URL_FWD)) {
            String[] arrOfStr = url.split(AppConstant.URL_FWD_);
            String[] second = arrOfStr[1].split(AppConstant.URL_BKEY);
            String decodeData = new String(Base64.decode(second[0], Base64.DEFAULT));
            return decodeData;
        }
        else
        {
            return url;
        }



    }

    private static int priorityForImportance(int priority) {
        if (priority > 9)
            return NotificationManagerCompat.IMPORTANCE_MAX;
        if (priority > 7)
            return NotificationManagerCompat.IMPORTANCE_HIGH;
        return NotificationManagerCompat.IMPORTANCE_HIGH;
    }
    private static int priorityForLessOreo(int priority) {
        if (priority > 0)
            return Notification.PRIORITY_HIGH;
        return Notification.PRIORITY_HIGH;
    }
    private static int setLockScreenVisibility(int visibility) {
        if (visibility < 0)
            return NotificationCompat.VISIBILITY_SECRET;
        if (visibility == 0)
            return NotificationCompat.VISIBILITY_PRIVATE;
        return NotificationCompat.VISIBILITY_PUBLIC;

    }

    private static void badgeCountUpdate(int count){
        final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(DATAB.appContext);
        try {
            if (count > 0) {
                if (preferenceUtil.getIntData(AppConstant.NOTIFICATION_COUNT)>=1){
                    preferenceUtil.setIntData(AppConstant.NOTIFICATION_COUNT,preferenceUtil.getIntData(AppConstant.NOTIFICATION_COUNT)+1);
                }else {
                    preferenceUtil.setIntData(AppConstant.NOTIFICATION_COUNT,1);
                }
            }
            ShortcutBadger.applyCountOrThrow(DATAB.appContext,preferenceUtil.getIntData(AppConstant.NOTIFICATION_COUNT));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean isAppInForeground(Context context) {
        List<ActivityManager.RunningTaskInfo> task =
                ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE))
                        .getRunningTasks(1);
        if (task.isEmpty()) {
            // app is in background
            return false;
        }
        return task
                .get(0)
                .topActivity
                .getPackageName()
                .equalsIgnoreCase(context.getPackageName());
    }

    private static void showAlert(final Payload payload){
        final Activity activity = DATAB.curActivity;
        if (activity!=null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    AlertDialog.Builder mBuilder = new AlertDialog.Builder(activity);
                    mBuilder.setTitle(payload.getTitle());
                    mBuilder.setMessage(payload.getMessage());

                    if (Util.getApplicationIcon(DATAB.appContext)!=null){
                        mBuilder.setIcon(Util.getApplicationIcon(DATAB.appContext));
                    }

                    String clickIndex = "0";
                    String impressionIndex ="0";

                    String data=Util.getIntegerToBinary(payload.getCfg());
                    if(data!=null && !data.isEmpty()) {
                        clickIndex = String.valueOf(data.charAt(data.length() - 2));
                        impressionIndex = String.valueOf(data.charAt(data.length() - 1));
                    }
                    else
                    {
                        clickIndex = "0";
                        impressionIndex="0";
                    }

                    mBuilder.setNeutralButton(AppConstant.DIALOG_DISMISS, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    final String finalClickIndex1 = clickIndex;
                    mBuilder.setPositiveButton(AppConstant.DIALOG_OK,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    dialog.dismiss();
                                    Intent intent = notificationClick(payload, payload.getLink(), payload.getAct1link(), payload.getAct2link(), AppConstant.NO, finalClickIndex1, 100, 0);
                                    activity.sendBroadcast(intent);
                                }
                            });


                    mBuilder.setCancelable(true);
                    AlertDialog alertDialog = mBuilder.create();
                    alertDialog.setCanceledOnTouchOutside(false);
                    alertDialog.show();
                    try {

                        if(impressionIndex.equalsIgnoreCase("1")) {
                            viewNotificationApi(payload);
                        }
                        DATAB.notificationView(payload);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            });
        }

    }

    private static Intent notificationClick(Payload payload, String getLink ,String getLink1, String getLink2, String phone, String finalClickIndex, int notificationId, int button){
        String link = getLink;
        String link1 = getLink1;
        String link2 = getLink2;
        if (payload.getFetchURL() == null || payload.getFetchURL().isEmpty()) {
            if (link.contains(AppConstant.BROWSERKEYID))
                link = link.replace(AppConstant.BROWSERKEYID, PreferenceUtil.getInstance(DATAB.appContext).getStringData(AppConstant.FCM_DEVICE_TOKEN));
            if (link1.contains(AppConstant.BROWSERKEYID))
                link1 = link1.replace(AppConstant.BROWSERKEYID, PreferenceUtil.getInstance(DATAB.appContext).getStringData(AppConstant.FCM_DEVICE_TOKEN));
            if (link2.contains(AppConstant.BROWSERKEYID))
                link2 = link2.replace(AppConstant.BROWSERKEYID, PreferenceUtil.getInstance(DATAB.appContext).getStringData(AppConstant.FCM_DEVICE_TOKEN));
        } else {
            String notificationLink = payload.getLink();
            notificationLink = getFinalUrl(payload);
        }

        Intent intent = new Intent(DATAB.appContext, NotificationActionReceiver.class);
        intent.putExtra(AppConstant.KEY_WEB_URL, link);
        intent.putExtra(AppConstant.KEY_NOTIFICITON_ID, notificationId);
        intent.putExtra(AppConstant.KEY_IN_APP, payload.getInapp());
        intent.putExtra(AppConstant.KEY_IN_CID, payload.getId());
        intent.putExtra(AppConstant.KEY_IN_RID, payload.getRid());
        intent.putExtra(AppConstant.KEY_IN_BUTOON, button);
        intent.putExtra(AppConstant.KEY_IN_ADDITIONALDATA, payload.getAp());
        intent.putExtra(AppConstant.KEY_IN_PHONE, phone);
        intent.putExtra(AppConstant.KEY_IN_ACT1ID, payload.getAct1ID());
        intent.putExtra(AppConstant.KEY_IN_ACT2ID, payload.getAct2ID());
        intent.putExtra(AppConstant.LANDINGURL, payload.getLink());
        intent.putExtra(AppConstant.ACT1TITLE, payload.getAct1name());
        intent.putExtra(AppConstant.ACT2TITLE, payload.getAct2name());
        intent.putExtra(AppConstant.ACT1URL, payload.getAct1link());
        intent.putExtra(AppConstant.ACT2URL, payload.getAct2link());
        intent.putExtra(AppConstant.CLICKINDEX, finalClickIndex);


        return intent;
    }

    private static void viewNotificationApi(final Payload payload){

        final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(DATAB.appContext);

        String api_url = AppConstant.API_PID +  preferenceUtil.getDataBID(AppConstant.APPPID) +
                AppConstant.CID_ + payload.getId() + AppConstant.TOKEN + preferenceUtil.getStringData(AppConstant.FCM_DEVICE_TOKEN) + AppConstant.RID_ + payload.getRid() + "&op=view";

        RestClient.postRequest(RestClient.IMPRESSION_URL + api_url, new RestClient.ResponseHandler() {


            @Override
            void onFailure(int statusCode, String response, Throwable throwable) {
                super.onFailure(statusCode, response, throwable);
            }

            @Override
            void onSuccess(String response) {
                super.onSuccess(response);
                if (payload != null)
                    Log.e("imp","call");

            }
        });
    }

    private static String getPhone(String getActLink){
        String phone;

        String checkNumber =decodeURL(getActLink);
        if (checkNumber.contains(AppConstant.TELIPHONE))
            phone = checkNumber;
        else
            phone = AppConstant.NO;
        return phone;
    }
}