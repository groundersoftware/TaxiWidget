package com.groundersoftware.taxiwidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RemoteViews;

import java.util.Timer;
import java.util.TimerTask;

import com.groundersoftware.taxiwidget.locationservice.LocationService;

/**
 * Created by Under on 31/03/14.
 */
public class TaxiWidget extends AppWidgetProvider {

    private static final String ACTION_REFRESH = "com.groundersoftware.taxiwidget.taxiwidget.REFRESH_ACTION";
    private static final String ACTION_CALL = "com.groundersoftware.taxiwidget.taxiwidget.CALL_ACTION";

    private static LocationService mLocationService;
    private static Timer mUpdateTimer;

    @Override
    public void onEnabled(final Context context)

    {
        mLocationService = new LocationService(context);
        mLocationService.enable();

        mUpdateTimer = new Timer();
        mUpdateTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                updateWidget(context);
            }

        }, 0, 30000);


        super.onEnabled(context);
    }

    @Override
    public void onDisabled(Context context) {

        if (mLocationService != null) {
            mLocationService.disable();
        }

        if (mUpdateTimer != null) {
            mUpdateTimer.cancel();
            mUpdateTimer = null;
        }

        super.onDisabled(context);
    }

    @Override
    public void onUpdate( Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds )
    {
        //Toast.makeText(context, "onUpdate", Toast.LENGTH_SHORT).show();
        for (int appWidgetId : appWidgetIds) {
            Bundle myOptions = appWidgetManager.getAppWidgetOptions (appWidgetId);

            // Get the value of OPTION_APPWIDGET_HOST_CATEGORY
            int category = myOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_HOST_CATEGORY, -1);

            // If the value is WIDGET_CATEGORY_KEYGUARD, it's a lockscreen widget
            boolean bLockScreen = category == AppWidgetProviderInfo.WIDGET_CATEGORY_KEYGUARD;

            Intent refresh = new Intent(context, TaxiWidget.class);
            refresh.setAction(ACTION_REFRESH);
            refresh.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            PendingIntent actionRefreshPendingIntent = PendingIntent.getBroadcast(context, 0, refresh, PendingIntent.FLAG_UPDATE_CURRENT);

            Intent call = new Intent(context, TaxiWidget.class);
            call.setAction(ACTION_CALL);
            call.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            PendingIntent actionCallPendingIntent = PendingIntent.getBroadcast(context, 0, call, PendingIntent.FLAG_UPDATE_CURRENT);

            // Get the layout for the App Widget and attach an on-click listener
            // to the button
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.main);
            if ( !bLockScreen ) {
                views.setOnClickPendingIntent(R.id.call_view, actionCallPendingIntent);
                views.setOnClickPendingIntent(R.id.refresh_view, actionRefreshPendingIntent);
            }

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(ACTION_REFRESH)) {
            ComponentName thisWidget = new ComponentName(context, TaxiWidget.class);
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);

            for (int appWidgetId : appWidgetIds) {
                RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.main);

//                remoteViews.setTextViewText(R.id.traffic_info_view, context.getString(R.string.loading_text) );

                appWidgetManager.updateAppWidget(thisWidget, remoteViews);
            }
        } else if (intent.getAction().equals(ACTION_CALL)) {
            // nothing for now
        } else {
            super.onReceive(context, intent);
        }
    }

    /**
     * Updates widget. It is called from timer period time.
     */
    private void updateWidget(Context context) {

        ComponentName thisWidget = new ComponentName(context, TaxiWidget.class);
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);

        for (int appWidgetId : appWidgetIds) {
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.main);

  //          remoteViews.setViewVisibility(R.id.refresh_view, View.VISIBLE);
  //          remoteViews.setViewVisibility(R.id.drive_view, View.INVISIBLE);

            appWidgetManager.updateAppWidget(thisWidget, remoteViews);
        }
    }

}
