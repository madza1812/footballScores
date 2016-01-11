package barqsoft.footballscores.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import barqsoft.footballscores.MainActivity;
import barqsoft.footballscores.R;
import barqsoft.footballscores.Utilies;
import barqsoft.footballscores.ScoresAdapter;
import barqsoft.footballscores.service.MyFetchService;

/**
 * Created by An on 10/2/2015.
 */
public class ScoresWidgetProvider extends AppWidgetProvider {

    public static final String TAG = ScoresWidgetProvider.class.getSimpleName();
    public static final String KEY_DATE = "date_key";
    public static final String KEY_FRAGMENT_POSITION = "position_fragment_key";
    public static final String ACTION_REFRESH = "refresh_scores_action";
    private final int FRAGMENT_DEFAULT_POS = 1;
    private int selectedFragPos;
    private ScoresAdapter mAdapter;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        Log.v(TAG, "onUpdate");

        final int N = appWidgetIds.length;

        // fetching scores data
        if (Utilies.isNetworkAvailable(context)) {
            Intent fetchSrvIntent = new Intent(context, MyFetchService.class);
            context.startService(fetchSrvIntent);
        }

        for (int i = 0; i< N; i++) {
            Log.v(TAG, "inFORLOOP: i = " + i + ", appWidgetId = " + appWidgetIds[i]);
            int appWidgetId = appWidgetIds[i];

            SharedPreferences sharedprefs = context.getSharedPreferences(
                    WidgetConfigureActivity.PREFS_NAME,
                    Context.MODE_PRIVATE
            );
            selectedFragPos = sharedprefs.getInt(
                    WidgetConfigureActivity.PREFS_KEY_PREFIX + appWidgetId,
                    FRAGMENT_DEFAULT_POS
            );
            if (Build.VERSION.SDK_INT >= 17 &&
                    context.getResources().getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL){
                selectedFragPos = selectedFragPos == 1 ? 3 : (selectedFragPos == 3 ? 1: 2);
            }
            // Update App Widget
            updateAppWidget(context, appWidgetManager, appWidgetId, selectedFragPos);

        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
        Log.v(TAG, "onDeleted");
        final int N = appWidgetIds.length;
        for (int i=0; i<N;i++) {
            WidgetConfigureActivity.saveIntWidgetPrefs(context, appWidgetIds[i], FRAGMENT_DEFAULT_POS);
        }
    }

    public static void updateAppWidget (Context context, AppWidgetManager appWidgetManager,
                                        int appWidgetId, int fragPosition) {
        Log.d(TAG, "updateAppWidget appWidgetId=" + appWidgetId + " fragment position=" + fragPosition);

        // Inflate the RemoteView
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.scores_widget);
        // Set Widget Title
        views.setTextViewText(R.id.scores_date_widget, Utilies.getWidgetFragmentTitle(context, fragPosition));
        if (Build.VERSION.SDK_INT >= 15)
            views.setContentDescription(R.id.scores_date_widget,
                    Utilies.getWidgetFragmentTitle(context, fragPosition)
                            + context.getString(R.string.a11y_page_date_scores));
        // Setup refresh button
        Intent refreshIntent = new Intent(context, ScoresWidgetProvider.class);
        refreshIntent.setAction(ACTION_REFRESH);
        refreshIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        refreshIntent.putExtra(KEY_FRAGMENT_POSITION, fragPosition);
        PendingIntent refreshPendingIntent = PendingIntent
                .getBroadcast(context, 0, refreshIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.refresh_action_widget, refreshPendingIntent);

        // Set intent for widget service creating the views
        Intent serviceIntent = new Intent(context, ListWidgetService.class);
        serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                .putExtra(KEY_DATE, Utilies.convertFragPosToDate(fragPosition))
                .putExtra(KEY_FRAGMENT_POSITION, fragPosition)
                .setData(Uri.parse(serviceIntent.toUri(Intent.URI_INTENT_SCHEME)));
        if (Build.VERSION.SDK_INT >= 14)
            views.setRemoteAdapter(R.id.scores_list_widget, serviceIntent);
        else
            views.setRemoteAdapter(appWidgetId, R.id.scores_list_widget, serviceIntent);
        // Set Empty Listview
        views.setEmptyView(R.id.scores_list_widget, R.id.empty_view_widget);

        // Set intent for item click
        Intent itemIntent = new Intent(context, MainActivity.class);
        itemIntent.setAction(MainActivity.ACTION_ITEM_VIEW_WIDGET)
                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                .setData(Uri.parse(itemIntent.toUri(Intent.URI_INTENT_SCHEME)));
        PendingIntent pendingIntent = PendingIntent.getActivity(context,0, itemIntent, 0);
        views.setPendingIntentTemplate(R.id.scores_list_widget, pendingIntent);

        // update widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v(TAG, "onReceived");
        if (intent != null) {
            String action = intent.getAction();
            if (ACTION_REFRESH.equals(action)) {
                // fetching scores data
                if (Utilies.isNetworkAvailable(context)) {
                    Intent fetchSrvIntent = new Intent(context, MyFetchService.class);
                    context.startService(fetchSrvIntent);
                }

                Bundle extras = intent.getExtras();
                int appWidgetId = extras.getInt(
                        AppWidgetManager.EXTRA_APPWIDGET_ID,
                        AppWidgetManager.INVALID_APPWIDGET_ID);
                int fragPosition = extras.getInt(KEY_FRAGMENT_POSITION, FRAGMENT_DEFAULT_POS);
                Log.v(TAG, "onReceived: appWidgetId = " + appWidgetId +", fragPosition = " + fragPosition);
                updateAppWidget(
                        context,
                        AppWidgetManager.getInstance(context),
                        appWidgetId,
                        fragPosition);
            }
        }

        super.onReceive(context, intent);
    }
}
