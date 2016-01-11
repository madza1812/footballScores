package barqsoft.footballscores.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import barqsoft.footballscores.DatabaseContract;
import barqsoft.footballscores.MainActivity;
import barqsoft.footballscores.R;
import barqsoft.footballscores.Utilies;
import barqsoft.footballscores.ScoresAdapter;

/**
 * Created by An on 10/3/2015.
 */
public class ListWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new ListRemoteViewsFactory(this.getApplicationContext(), intent);
    }
}

class ListRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    public static final String TAG = ListRemoteViewsFactory.class.getSimpleName();

    private Context mContext;
    private int mAppWidgetId;
    private final int FRAG_DEFAULT_POS= 2;
    private int selectedFragPos;
    private String mDate[] = new String[1];
    private Cursor mCursor;

    public ListRemoteViewsFactory(Context context, Intent intent) {
        Log.v(TAG, "onConstructor");
        mContext = context;
        mAppWidgetId = intent.getIntExtra(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
        mDate[0] = intent.getStringExtra(ScoresWidgetProvider.KEY_DATE);
        selectedFragPos = intent.getIntExtra(ScoresWidgetProvider.KEY_FRAGMENT_POSITION, FRAG_DEFAULT_POS);
    }

    @Override
    public void onCreate() {
        Log.v(TAG, "onCreate");
    }

    @Override
    public void onDataSetChanged() {
        final long identityToken = Binder.clearCallingIdentity();
        // Refresh the cursor
        if (mCursor != null) {
            mCursor.close();
        }
        mCursor = mContext.getContentResolver().query(DatabaseContract.scores_table.buildScoreWithDate(),
                null,null,mDate,null);
        Binder.restoreCallingIdentity(identityToken);
    }

    @Override
    public void onDestroy() {
        if (mCursor != null) {
            mCursor.close();
        }
    }

    @Override
    public int getCount() {
        return mCursor.getCount();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        final int itemId = R.layout.scores_list_item_widget;
        RemoteViews rv = new RemoteViews(mContext.getPackageName(), itemId);
        double selectedMatchId = 0;

        if (mCursor.moveToPosition(position)) {
            rv.setTextViewText(R.id.widget_home_name, mCursor.getString(ScoresAdapter.COL_HOME));
            if (Build.VERSION.SDK_INT >= 15)
                rv.setContentDescription(R.id.widget_home_name,
                        mContext.getString(R.string.a11y_home_team)
                                + mCursor.getString(ScoresAdapter.COL_HOME));
            rv.setTextViewText(R.id.widget_away_name, mCursor.getString(ScoresAdapter.COL_AWAY));
            if (Build.VERSION.SDK_INT >= 15)
                rv.setContentDescription(R.id.widget_away_name,
                        mContext.getString(R.string.a11y_away_team)
                                + mCursor.getString(ScoresAdapter.COL_AWAY));
            rv.setTextViewText(R.id.widget_data_textview, Utilies.convert24to12(mCursor.getString(ScoresAdapter.COL_MATCHTIME)));
            if (Build.VERSION.SDK_INT >= 15)
                rv.setContentDescription(R.id.widget_data_textview,
                        mContext.getString(R.string.a11y_match_time)
                                + Utilies.convert24to12(mCursor.getString(ScoresAdapter.COL_MATCHTIME)));
            rv.setTextViewText(
                    R.id.widget_score_textview,
                    Utilies.getScores(
                            mCursor.getInt(ScoresAdapter.COL_HOME_GOALS),
                            mCursor.getInt(ScoresAdapter.COL_AWAY_GOALS)
                    )
            );
            if (Build.VERSION.SDK_INT >= 15)
                rv.setContentDescription(R.id.score_textview,
                        Utilies.getScoreDescription(mContext,
                                mCursor.getInt(ScoresAdapter.COL_HOME_GOALS),
                                mCursor.getInt(ScoresAdapter.COL_AWAY_GOALS)));
            rv.setImageViewResource(
                    R.id.widget_home_crest,
                    Utilies.getTeamCrestByTeamName(mCursor.getString(ScoresAdapter.COL_HOME))
            );
            if (Build.VERSION.SDK_INT >= 15)
                rv.setContentDescription(R.id.widget_home_crest,
                        mContext.getString(R.string.a11y_home_team_icon));
            rv.setImageViewResource(
                    R.id.widget_away_crest,
                    Utilies.getTeamCrestByTeamName(mCursor.getString(ScoresAdapter.COL_AWAY))
            );
            if(Build.VERSION.SDK_INT >= 15)
                rv.setContentDescription(R.id.widget_away_crest,
                        mContext.getString(R.string.a11y_away_team_icon));
            selectedMatchId = mCursor.getDouble(ScoresAdapter.COL_ID);
            // Set the item click
            Bundle bundle = new Bundle();
            bundle.putInt(ScoresWidgetProvider.KEY_FRAGMENT_POSITION, selectedFragPos);
            bundle.putDouble(MainActivity.KEY_MATCH_ID, selectedMatchId);
            bundle.putInt(MainActivity.KEY_LIST_POSITION, position);
            Intent fillInIntent = new Intent();
            fillInIntent.putExtras(bundle);
            rv.setOnClickFillInIntent(R.id.scores_list_widget_item, fillInIntent);
        }

        return rv;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}
