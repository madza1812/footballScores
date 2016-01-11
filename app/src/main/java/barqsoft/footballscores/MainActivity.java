package barqsoft.footballscores;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import barqsoft.footballscores.service.MyFetchService;
import barqsoft.footballscores.widget.ScoresWidgetProvider;

public class MainActivity extends ActionBarActivity
{
    public static final String TAG= MainActivity.class.getSimpleName();

    public static final String ACTION_ITEM_VIEW_WIDGET = "widget_view_item_action";
    public static final String KEY_MATCH_ID = "id_match_key";
    public static final String KEY_LIST_POSITION = "position_list_key";
    public static final String KEY_SAVE_CURRENT_PAGER = "pager_current_save_key";
    public static final String KEY_SAVE_SELECTED_MATCH = "match_selected_save_key";

    public static int selected_match_id;
    public static int current_fragment = 2;
    public static int mListViewPosition = ListView.INVALID_POSITION;
    private final String save_tag = "Save Test";
    private PagerFragment my_main;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "Reached MainActivity onCreate");
        if (savedInstanceState == null) {
            update_scores();
            // Handle the item click on Widget
            Bundle bundle;
            if (getIntent() != null && ACTION_ITEM_VIEW_WIDGET.equals(getIntent().getAction())) {
                Log.v(TAG, "onCreate: Intent from Widget is NOT NULL");
                bundle = getIntent().getExtras();
                if (bundle != null) {
                    Log.v(TAG, "onCreate: Bundle is NOT NULL");
                    selected_match_id = (int) bundle.getDouble(KEY_MATCH_ID);
                    current_fragment = bundle.getInt(ScoresWidgetProvider.KEY_FRAGMENT_POSITION);
                    if (Build.VERSION.SDK_INT >= 17 &&
                            getResources().getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL){
                        current_fragment = current_fragment == 1 ? 3 : (current_fragment == 3 ? 1: 2);
                    }
                    mListViewPosition = bundle.getInt(MainActivity.KEY_LIST_POSITION);
                    Log.v(TAG, "onCreate: Bundle is NOT NULL, " +
                    "selected_match_id = " + selected_match_id +
                    " , currentFragPos = " + current_fragment +
                    " , mListViewPosition = " + mListViewPosition);
                }
            }
            my_main = new PagerFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, my_main)
                    .commit();
        }
    }

    private void update_scores()
    {
        if (Utilies.isNetworkAvailable(getApplicationContext())) {
            Intent service_start = new Intent(getApplicationContext(), MyFetchService.class);
            this.startService(service_start);
        } else {
            Utilies.displayToast(getApplicationContext(),
                    getString(R.string.network_error_message));
        }
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
        if (id == R.id.action_about) {
            Intent start_about = new Intent(this,AboutActivity.class);
            startActivity(start_about);
            return true;
        } else if (id == R.id.scores_refresh_action) {
            update_scores();
            my_main = new PagerFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, my_main)
                    .commit();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        Log.v(save_tag,"will save");
        Log.v(save_tag,"fragment: "+String.valueOf(my_main.mPagerHandler.getCurrentItem()));
        Log.v(save_tag,"selected id: "+selected_match_id);
        outState.putInt(KEY_SAVE_CURRENT_PAGER,my_main.mPagerHandler.getCurrentItem());
        outState.putInt(KEY_SAVE_SELECTED_MATCH,selected_match_id);
        getSupportFragmentManager().putFragment(outState,TAG ,my_main);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
        Log.v(save_tag,"will retrieve");
        Log.v(save_tag,"fragment: "+String.valueOf(savedInstanceState.getInt(KEY_SAVE_CURRENT_PAGER)));
        Log.v(save_tag,"selected id: "+savedInstanceState.getInt(KEY_SAVE_SELECTED_MATCH));
        current_fragment = savedInstanceState.getInt(KEY_SAVE_CURRENT_PAGER);
        selected_match_id = savedInstanceState.getInt(KEY_SAVE_SELECTED_MATCH);
        my_main = (PagerFragment) getSupportFragmentManager().getFragment(savedInstanceState,TAG);
        super.onRestoreInstanceState(savedInstanceState);
    }
}
