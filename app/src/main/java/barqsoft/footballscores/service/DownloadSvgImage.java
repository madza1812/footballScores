package barqsoft.footballscores.service;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Picture;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PictureDrawable;
import android.os.AsyncTask;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.larvalabs.svgandroid.SVG;
import com.larvalabs.svgandroid.SVGParseException;
import com.larvalabs.svgandroid.SVGParser;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import barqsoft.footballscores.R;

/**
 * Created by An on 10/12/2015.
 */
public class DownloadSvgImage extends AsyncTask<Void, Void, Drawable> {

    public final static String TAG = DownloadSvgImage.class.getSimpleName();

    ImageView imageView;
    String href;
    Context context;

    public DownloadSvgImage (Context context, ImageView imageView, String href) {
        this.imageView = imageView;
        this.href = href;
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        imageView.setImageResource(R.drawable.ic_loading_icon);
    }

    @Override
    protected Drawable doInBackground(Void... params) {
        HttpURLConnection urlConn = null;
        InputStream is = null;
        try {
            Log.v(TAG, "onDoInBackGround");
            final URL url = new URL(href);
            urlConn = (HttpURLConnection) url.openConnection();
            is = urlConn.getInputStream();
            SVG svg = null;
            try {
                svg = SVGParser.getSVGFromInputStream(is);
            } catch (SVGParseException e) {
                e.printStackTrace();
            }
            if (svg != null) {
                final int WIDTH_DP = 48, HEIGHT_DP = 48;
                Resources resources = context.getResources();
                DisplayMetrics metrics = resources.getDisplayMetrics();
                int widthPx = (int) (WIDTH_DP * (metrics.densityDpi / 160f));
                int heightPx = (int) (HEIGHT_DP * (metrics.densityDpi / 160f));

                //Drawable drawable= svg.createPictureDrawable();
                Picture picture = svg.getPicture();

                //Bitmap b = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.RGB_565);
                Canvas canvas;// = new Canvas(b);
                Picture resizedPicture = new Picture();
                canvas = resizedPicture.beginRecording(widthPx, heightPx);
                canvas.drawPicture(picture, new Rect(0, 0, widthPx, heightPx));
                resizedPicture.endRecording();
                return new PictureDrawable(resizedPicture);

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if (urlConn != null)
                urlConn.disconnect();
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(Drawable drawable) {
        if (drawable != null) {
            Log.v(TAG, "onPostExcecute");
            imageView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            imageView.setImageDrawable(drawable);

        } else {
            imageView.setImageResource(R.drawable.no_icon);
        }
    }

}
