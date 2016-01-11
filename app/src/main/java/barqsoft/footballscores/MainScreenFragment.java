package barqsoft.footballscores;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainScreenFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String TAG = MainScreenFragment.class.getSimpleName();

    public ScoresAdapter mAdapter;
    public static final int SCORES_LOADER = 0;
    private String[] fragmentdate = new String[1];
    private int last_selected_item = -1;
    private int mPosition = ListView.INVALID_POSITION;
    private ListView score_list;


    public MainScreenFragment() {}
    public void setFragmentDate(String date)
    {
        fragmentdate[0] = date;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {
        Log.v(TAG, "onCreateView: " );

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        score_list = (ListView) rootView.findViewById(R.id.scores_list);
        mAdapter = new ScoresAdapter(getActivity(),null,0);
        score_list.setAdapter(mAdapter);
        getLoaderManager().initLoader(SCORES_LOADER, null, this);
        mAdapter.detail_match_id = MainActivity.selected_match_id;
        mPosition = MainActivity.mListViewPosition;
        score_list.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                ViewHolder selected = (ViewHolder) view.getTag();
                mAdapter.detail_match_id = selected.match_id;
                MainActivity.selected_match_id = (int) selected.match_id;
                mAdapter.notifyDataSetChanged();
            }
        });
       /* ImageView testIMV = (ImageView) rootView.findViewById(R.id.test_svg_download);
        new DownloadSvgImage(getActivity(), testIMV, "https://upload.wikimedia.org/wikipedia/commons/e/e8/Svg_example3.svg").execute();*/
        return rootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle)
    {
        return new CursorLoader(getActivity(),DatabaseContract.scores_table.buildScoreWithDate(),
                null,null,fragmentdate,null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor)
    {
        //Log.v(FetchScoreTask.LOG_TAG,"loader finished");

        int i = 0;
        cursor.moveToFirst();
        while (!cursor.isAfterLast())
        {
            i++;
            cursor.moveToNext();
        }
        //Log.v(FetchScoreTask.LOG_TAG,"Loader query: " + String.valueOf(i));
        Log.v(TAG, "onLoadFinished: Intent = " + String.valueOf(getActivity().getIntent()==null));
        mAdapter.swapCursor(cursor);
        if (mPosition != ListView.INVALID_POSITION) {
            score_list.setSelection(mPosition);
            mAdapter.detail_match_id = MainActivity.selected_match_id;
            mAdapter.notifyDataSetChanged();
            mPosition = ListView.INVALID_POSITION;
            MainActivity.mListViewPosition = ListView.INVALID_POSITION;
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader)
    {
        mAdapter.swapCursor(null);
    }


}
