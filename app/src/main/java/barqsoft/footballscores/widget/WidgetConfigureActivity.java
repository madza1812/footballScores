package barqsoft.footballscores.widget;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RemoteViews;

import barqsoft.footballscores.R;

public class WidgetConfigureActivity extends Activity {

    static final String TAG = WidgetConfigureActivity.class.getSimpleName();

    static final String PREFS_NAME ="barqsoft.footballscores.WIDGET_DATE";
    static final String PREFS_KEY_PREFIX = "prefix_";

    int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set the result to CANCELED. This will cause the widget host to cancel
        // out of the widget placement if they press the back button.
        setResult(RESULT_CANCELED);

        // Set the view layout of the WidgetConfigure
        setContentView(R.layout.activity_widget_configure);
        // Setup the listview of Yesterday, Today, Tomorrow
        String[] dates = new String[]{"Yesterday","Today","Tomorrow"};
        ArrayAdapter<String> listAdapter = new ArrayAdapter<String>(this, R.layout.widget_setting_list_item, dates);
        ListView listView = (ListView) findViewById(R.id.widget_setting_list);
        listView.setAdapter(listAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.v(TAG, "ListView onItemClick");
                saveIntWidgetPrefs(getApplicationContext(), mAppWidgetId, position+1);

                // Push widget update to surface with newly set prefix
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());
                ScoresWidgetProvider.updateAppWidget(getApplicationContext(), appWidgetManager, mAppWidgetId, position+1);

                // Create a return intent
                Intent resultValue = new Intent();
                resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
                setResult(RESULT_OK, resultValue);
                finish();
            }
        });

        // Find the widget id from the intent
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
        }
    }

    static void saveIntWidgetPrefs(Context context, int appWidgetId, int value) {
        SharedPreferences.Editor prefsEditor = context
                        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                        .edit();

        prefsEditor.putInt((PREFS_KEY_PREFIX+appWidgetId), value).commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_widget_configure, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
