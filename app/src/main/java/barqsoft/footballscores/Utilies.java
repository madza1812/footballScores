package barqsoft.footballscores;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import barqsoft.footballscores.service.DownloadSvgImage;

/**
 * Created by yehya khaled on 3/3/2015.
 */
public class Utilies
{
    public static final int BUNDESLIGA1 = 394;
    public static final int BUNDESLIGA2 = 395;
    public static final int LIGUE1 = 396;
    public static final int LIGUE2 = 397;
    public static final int PREMIER_LEAGUE = 398;
    public static final int PRIMERA_DIVISION = 399;
    public static final int SEGUNDA_DIVISION = 400;
    public static final int SERIE_A = 401;
    public static final int PRIMERA_LIGA = 402;
    public static final int BUNDESLIGA3 = 403;
    public static final int EREDIVISIE = 404;
    public static final int CHAMPIONS_LEAGUE = 405;
    public static final int BUNDESLIGA = 351;


    public static String getLeague(Context context, int league_num)
    {
        switch (league_num)
        {
            case SERIE_A : return context.getString(R.string.serie_a);
            case PREMIER_LEAGUE : return context.getString(R.string.premier_league);
            case CHAMPIONS_LEAGUE : return context.getString(R.string.champions_league);
            case PRIMERA_DIVISION : return context.getString(R.string.primera_division);
            case BUNDESLIGA : return context.getString(R.string.bundesliga);
            case BUNDESLIGA1 : return context.getString(R.string.bundesliga_1);
            case BUNDESLIGA2 : return context.getString(R.string.bundesliga_2);
            case LIGUE1 : return context.getString(R.string.ligue_1);
            case LIGUE2 : return context.getString(R.string.ligue_2);
            case SEGUNDA_DIVISION : return context.getString(R.string.segunda_division);
            case PRIMERA_LIGA : return context.getString(R.string.primera_liga);
            case BUNDESLIGA3 : return context.getString(R.string.bundesliga_3);
            case EREDIVISIE : return context.getString(R.string.eredivisie);
            default: return context.getString(R.string.unknown_league);
        }
    }
    public static String getMatchDay(Context context, int match_day,int league_num)
    {
        if(league_num == CHAMPIONS_LEAGUE)
        {
            if (match_day <= 6)
            {
                return context.getString(R.string.match_day_CL_group_stages) + String.valueOf(match_day);
            }
            else if(match_day == 7 || match_day == 8)
            {
                return context.getString(R.string.match_day_CL_first_knockout);
            }
            else if(match_day == 9 || match_day == 10)
            {
                return context.getString(R.string.match_day_CL_quarter_final);
            }
            else if(match_day == 11 || match_day == 12)
            {
                return context.getString(R.string.match_day_CL_semi_final);
            }
            else
            {
                return context.getString(R.string.match_day_CL_final);
            }
        }
        else
        {
            return context.getString(R.string.match_day) + String.valueOf(match_day);
        }
    }

    public static String getScores(int home_goals,int awaygoals)
    {
        if(home_goals < 0 || awaygoals < 0)
        {
            return " - ";
        }
        else
        {
            return String.valueOf(home_goals) + " - " + String.valueOf(awaygoals);
        }
    }

    public static String getScoreDescription(Context context, int homeGoals, int awayGoals) {
        if(homeGoals < 0 || awayGoals < 0)
        {
            return context.getString(R.string.a11y_no_score);
        }
        else
        {
            return context.getString(R.string.a11y_scores) +
                    String.valueOf(homeGoals) +
                    " to " +
                    String.valueOf(awayGoals);
        }
    }

    public static int getTeamCrestByTeamName (String teamname)
    {
        
        if (teamname==null){return R.drawable.no_icon;}
        switch (teamname)
        { //This is the set of icons that are currently in the app. Feel free to find and add more
            //as you go.
            case "Arsenal London FC" : return R.drawable.arsenal;
            case "Manchester United FC" : return R.drawable.manchester_united;
            case "Swansea City" : return R.drawable.swansea_city_afc;
            case "Leicester City" : return R.drawable.leicester_city_fc_hd_logo;
            case "Everton FC" : return R.drawable.everton_fc_logo1;
            case "West Ham United FC" : return R.drawable.west_ham;
            case "Tottenham Hotspur FC" : return R.drawable.tottenham_hotspur;
            case "West Bromwich Albion" : return R.drawable.west_bromwich_albion_hd_logo;
            case "Sunderland AFC" : return R.drawable.sunderland;
            case "Stoke City FC" : return R.drawable.stoke_city;

            default: return R.drawable.no_icon;
        }
    }

    public static void setTeamCrestWithPngImage(Context context, String url, ImageView target) {
        if (url.trim().length() == 0) {
            Picasso.with(context)
                    .load(R.drawable.no_icon)
                    .placeholder(R.drawable.ic_loading_icon)
                    .into(target);
        } else {
            Picasso.with(context)
                    .load(url)
                    .placeholder(R.drawable.ic_loading_icon)
                    .error(R.drawable.no_icon)
                    .into(target);
        }
    }

    public static void setTeamCrestWithSVGImage(Context context, String href, ImageView target) {
        Log.v("Utilities", "setTeamCrestWithSVG");
        String correctedHref = href;
        if (!href.startsWith("https:")) {
            correctedHref = href.replaceFirst("http", "https");
        }
        DownloadSvgImage downloadSVG = new DownloadSvgImage(context, target, correctedHref);
        downloadSVG.execute();
    }

    public static String convertFragPosToDate (int fragPosition) {
        Date fragmentdate = new Date(System.currentTimeMillis()+((fragPosition-2)*86400000));
        SimpleDateFormat mformat = new SimpleDateFormat("yyyy-MM-dd");
        return mformat.format(fragmentdate);
    }

    public static void displayToast (Context context, CharSequence msg) {
        Toast noConToast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
        noConToast.setGravity(Gravity.TOP | Gravity.CENTER, 0, 0);
        noConToast.show();
    }

    public static void displayNeutralAlert(Context context, String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        // Title and message
        builder.setMessage(message)
                .setTitle(title);
        // OK button
        builder.setNeutralButton(R.string.ok_alert, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public static String getWidgetFragmentTitle(Context context, int fragPosition) {
        return fragPosition > 2 ?
                context.getString(R.string.widget_title_tomorrow) : fragPosition < 2 ?
                context.getString(R.string.widget_title_yesterday) : context.getString(R.string.widget_title_today);
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return (activeNetwork != null && activeNetwork.isConnectedOrConnecting());
    }

    public static String convert24to12 (String hour24) {
        SimpleDateFormat sdf12 = new SimpleDateFormat("hh:mm aa");
        SimpleDateFormat sdf24 = new SimpleDateFormat("HH:mm");
        try {
            Date date = sdf24.parse(hour24);
            return sdf12.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
    }

}
