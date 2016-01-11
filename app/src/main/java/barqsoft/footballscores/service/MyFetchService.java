package barqsoft.footballscores.service;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.Vector;

import barqsoft.footballscores.DatabaseContract;
import barqsoft.footballscores.R;
import barqsoft.footballscores.Utilies;

/**
 * Created by yehya khaled on 3/2/2015.
 */
public class MyFetchService extends IntentService
{
    public static final String LOG_TAG = "MyFetchService";
    public MyFetchService()
    {
        super("MyFetchService");
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        getData("p2");
        getData("n2");
    }

    private void getData (String timeFrame)
    {
        //Creating fetch URL
        final String BASE_URL = "http://api.football-data.org/alpha/fixtures"; //Base URL
        final String QUERY_TIME_FRAME = "timeFrame"; //Time Frame parameter to determine days
        //final String QUERY_MATCH_DAY = "matchday";

        Uri fetch_build = Uri.parse(BASE_URL).buildUpon().
                appendQueryParameter(QUERY_TIME_FRAME, timeFrame).build();
        Log.v(LOG_TAG, "The url we are looking at is: "+fetch_build.toString()); //log spam
        HttpURLConnection m_connection = null;
        BufferedReader reader = null;
        String JSON_data = null;
        //Opening Connection
        try {
            URL fetch = new URL(fetch_build.toString());
            m_connection = (HttpURLConnection) fetch.openConnection();
            m_connection.setRequestMethod("GET");
            m_connection.addRequestProperty("X-Auth-Token",getString(R.string.api_key));
            m_connection.connect();

            // Read the input stream into a String
            InputStream inputStream = m_connection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }
            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return;
            }
            JSON_data = buffer.toString();
        }
        catch (Exception e)
        {
            Log.e(LOG_TAG,"Exception here" + e.getMessage());
            JSON_data = null;
        }
        finally {
            if(m_connection != null)
            {
                m_connection.disconnect();
            }
            if (reader != null)
            {
                try {
                    reader.close();
                }
                catch (IOException e)
                {
                    Log.e(LOG_TAG,"Error Closing Stream");
                }
            }
        }
        try {
            if (JSON_data != null) {
                //This bit is to check if the data contains any matches. If not, we call processJson on the dummy data
                JSONArray matches = new JSONObject(JSON_data).getJSONArray("fixtures");
                if (matches.length() == 0) {
                    //if there is no data, call the function on dummy data
                    //this is expected behavior during the off season.
                    //processJSONdata(getString(R.string.dummy_data), getApplicationContext(), false);
                    Log.v(LOG_TAG, "There is no data !");
                    return;
                }
                processJSONdata(JSON_data, getApplicationContext(), true);
            } else {
                //Could not Connect
                Log.d(LOG_TAG, "Could not connect to server.");
            }
        }
        catch(Exception e)
        {
            Log.e(LOG_TAG,e.getMessage());
        }
    }
    private void processJSONdata (String JSONdata,Context mContext, boolean isReal)
    {
        //JSON data
        final String SEASON_LINK = "http://api.football-data.org/alpha/soccerseasons/";
        final String MATCH_LINK = "http://api.football-data.org/alpha/fixtures/";
        final String FIXTURES = "fixtures";
        final String LINKS = "_links";
        final String SOCCER_SEASON = "soccerseason";
        final String SELF = "self";
        final String MATCH_DATE = "date";
        final String HOME_TEAM_NAME = "homeTeamName";
        final String AWAY_TEAM_NAME = "awayTeamName";
        final String RESULT = "result";
        final String HOME_GOALS = "goalsHomeTeam";
        final String AWAY_GOALS = "goalsAwayTeam";
        final String MATCH_DAY = "matchday";
        final String HOME_TEAM = "homeTeam";
        final String AWAY_TEAM = "awayTeam";
        final String HREF = "href";

        //Match data
        String League = null;
        String mDate = null;
        String mTime = null;
        String Home = null;
        String Away = null;
        String Home_goals = null;
        String Away_goals = null;
        String match_id = null;
        String match_day = null;
        String homeHref;
        String awayHref;
        String homeCrestUrl;
        String awayCrestUrl;


        try {
            //Log.v(LOG_TAG, JSONdata);
            JSONArray matches = new JSONObject(JSONdata).getJSONArray(FIXTURES);
            Log.v(LOG_TAG,"ProcessJSONData: match length = " + String.valueOf(matches.length()));

            //ContentValues to be inserted
            Vector<ContentValues> values = new Vector <ContentValues> (matches.length());
            for(int i = 0;i < matches.length();i++)
            {

                JSONObject match_data = matches.getJSONObject(i);
                League = match_data.getJSONObject(LINKS).getJSONObject(SOCCER_SEASON).
                        getString(HREF);
                League = League.replace(SEASON_LINK, "");

                match_id = match_data.getJSONObject(LINKS).getJSONObject(SELF).
                        getString(HREF);
                match_id = match_id.replace(MATCH_LINK, "");
                if(!isReal){
                    //This if statement changes the match ID of the dummy data so that it all goes into the database
                    match_id=match_id+Integer.toString(i);
                }
                // Added by An
                // Get the homeTeam href and awayTeam href
                homeHref = match_data.getJSONObject(LINKS).getJSONObject(HOME_TEAM)
                        .getString(HREF);
                homeCrestUrl = getCrestUrl(homeHref);
                Log.v(LOG_TAG, "homeCrestUrl = " + homeCrestUrl);
                awayHref = match_data.getJSONObject(LINKS).getJSONObject(AWAY_TEAM)
                        .getString(HREF);
                awayCrestUrl = getCrestUrl(awayHref);
                Log.v(LOG_TAG, "awayCrestUrl = " + awayCrestUrl);
                // End

                mDate = match_data.getString(MATCH_DATE);
                mTime = mDate.substring(mDate.indexOf("T") + 1, mDate.indexOf("Z"));
                mDate = mDate.substring(0,mDate.indexOf("T"));
                SimpleDateFormat match_date = new SimpleDateFormat("yyyy-MM-ddHH:mm:ss");
                match_date.setTimeZone(TimeZone.getTimeZone("UTC"));
                try {
                    Date parseddate = match_date.parse(mDate+mTime);
                    SimpleDateFormat new_date = new SimpleDateFormat("yyyy-MM-dd:HH:mm");
                    new_date.setTimeZone(TimeZone.getDefault());
                    mDate = new_date.format(parseddate);
                    mTime = mDate.substring(mDate.indexOf(":") + 1);
                    mDate = mDate.substring(0,mDate.indexOf(":"));

                    if(!isReal){
                        //This if statement changes the dummy data's date to match our current date range.
                        Date fragmentdate = new Date(System.currentTimeMillis()+((i-2)*86400000));
                        SimpleDateFormat mformat = new SimpleDateFormat("yyyy-MM-dd");
                        mDate=mformat.format(fragmentdate);
                    }
                } catch (Exception e) {
                    Log.d(LOG_TAG, "error here!");
                    Log.e(LOG_TAG,e.getMessage());
                }
                Home = match_data.getString(HOME_TEAM_NAME);
                Away = match_data.getString(AWAY_TEAM_NAME);
                Home_goals = match_data.getJSONObject(RESULT).getString(HOME_GOALS);
                Away_goals = match_data.getJSONObject(RESULT).getString(AWAY_GOALS);
                match_day = match_data.getString(MATCH_DAY);

                ContentValues match_values = new ContentValues();
                match_values.put(DatabaseContract.scores_table.MATCH_ID,match_id);
                match_values.put(DatabaseContract.scores_table.DATE_COL,mDate);
                match_values.put(DatabaseContract.scores_table.TIME_COL,mTime);
                match_values.put(DatabaseContract.scores_table.HOME_COL,Home);
                match_values.put(DatabaseContract.scores_table.AWAY_COL,Away);
                match_values.put(DatabaseContract.scores_table.HOME_GOALS_COL,Home_goals);
                match_values.put(DatabaseContract.scores_table.AWAY_GOALS_COL,Away_goals);
                match_values.put(DatabaseContract.scores_table.LEAGUE_COL, League);
                match_values.put(DatabaseContract.scores_table.MATCH_DAY, match_day);
                match_values.put(DatabaseContract.scores_table.HOME_CREST_COL, homeCrestUrl);
                match_values.put(DatabaseContract.scores_table.AWAY_CREST_COL, awayCrestUrl);

                values.add(match_values);

            }
            int inserted_data = 0;
            ContentValues[] insert_data = new ContentValues[values.size()];
            values.toArray(insert_data);
            inserted_data = mContext.getContentResolver().bulkInsert(
                    DatabaseContract.BASE_CONTENT_URI,insert_data);

            Log.v(LOG_TAG,"Succesfully Inserted : " + String.valueOf(inserted_data));
        }
        catch (JSONException e)
        {
            Log.e(LOG_TAG,e.getMessage());
        }

    }

    private String getCrestUrl (String href) {
        final String CREST_URL = "crestUrl";

        if (href != null) {
            HttpURLConnection m_connection = null;
            BufferedReader reader = null;
            String JSON_data = null;
            try {
                URL url = new URL(href);
                m_connection = (HttpURLConnection) url.openConnection();
                m_connection.setRequestMethod("GET");
                m_connection.addRequestProperty("X-Auth-Token",getString(R.string.api_key));
                m_connection.connect();
                // Read the input stream into a String
                InputStream inputStream = m_connection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return "";
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }
                if (buffer.length() == 0) {
                    return "";
                }
                JSON_data = buffer.toString();
            }
            catch (Exception e)
            {
                Log.e(LOG_TAG,"Exception here" + e.getMessage());
                JSON_data = null;
            }
            finally {
                if(m_connection != null)
                {
                    m_connection.disconnect();
                }
                if (reader != null)
                {
                    try {
                        reader.close();
                    }
                    catch (IOException e)
                    {
                        Log.e(LOG_TAG,"Error Closing Stream");
                    }
                }
            }

            if (JSON_data != null) {
                try {
                    JSONObject jsonObject = new JSONObject(JSON_data);
                    return jsonObject.getString(CREST_URL);
                } catch (JSONException e) {
                    e.printStackTrace();
                    return "";
                }
            } else {
                return "";
            }
        } else {
            return "";
        }
    }

}

