package com.xda.one.ui;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ShareCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.xda.one.R;

import com.xda.one.ui.widget.ObservableScrollView;
import com.xda.one.util.CompatUtils;

public class NewsReaderActivity extends AppCompatActivity implements ObservableScrollView.Callbacks {

    private ObservableScrollView mScrollView;

    int vibrantColor;
    private ImageView headerImageView;

    private TextView titleView;

    private TextView contentView;

    private FrameLayout mImageFrameLayout;

    private Toolbar mToolbar;

    private LinearLayout mToolbarLinearLayout;

    private LinearLayout mContentLinearLayout;

    private String news_title;

    private String news_content;

    private String news_url;

    private String news_image_url;

    public NewsReaderActivity() {

    }

    //@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.news_reader_activity);

        mToolbar = (Toolbar) findViewById(R.id.news_toolbar);
        setSupportActionBar(mToolbar);

        mScrollView = (ObservableScrollView) findViewById(R.id.notify_scroll_view);

        mImageFrameLayout = (FrameLayout) findViewById(R.id.image_frame_layout);
        mToolbarLinearLayout = (LinearLayout) findViewById(R.id.toolbar_linear_layout);

        mContentLinearLayout = (LinearLayout) findViewById(R.id.content_linear_layout);

        titleView = (TextView) findViewById(R.id.news_title);
        headerImageView = (ImageView) findViewById(R.id.news_image_view);
        contentView = (TextView) findViewById(R.id.news_contentView);

        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setTitle(null);
            ab.setDisplayHomeAsUpEnabled(true);
        }

        Bundle extras = getIntent().getExtras();
        if (extras != null) {

            news_title = extras.getString("NEWS_TITLE");
            news_content = extras.getString("NEWS_CONTENT");
            news_url = extras.getString("NEWS_URL");
            news_image_url = extras.getString("NEWS_IMAGE_URL");

            setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));

            Picasso.with(this)
                    .load(news_image_url).fit().centerCrop()
                    .placeholder(R.drawable.ic_image_placeholder)
                    .error(R.drawable.ic_image_placeholder)
                    .into(headerImageView, new com.squareup.picasso.Callback() {
                        @Override
                        public void onSuccess() {
                            Palette.from(((BitmapDrawable) headerImageView.getDrawable()).getBitmap()).generate(new Palette.PaletteAsyncListener() {
                                public void onGenerated(Palette p) {
                                    // Use generated palette
                                    vibrantColor = p.getVibrantColor(R.attr.colorPrimary);
                                    mToolbarLinearLayout.setBackgroundColor(vibrantColor);
                                    setStatusBarColor(vibrantColor);
                                }
                            });
                        }
                        @Override
                        public void onError() {

                        }
                    });

            titleView.setText(news_title);
            contentView.setText(Html.fromHtml(news_content));

            setupScrollView();
        }
    }

    public void setStatusBarColor(int color) {
        if (!CompatUtils.hasLollipop()) {
            return;
        }
        this.getWindow().setStatusBarColor(scaleColor(color, 0.8f, false));
    }

    public static int scaleColor(int color, float factor, boolean scaleAlpha) {
        return Color.argb(scaleAlpha ? (Math.round(Color.alpha(color) * factor)) : Color.alpha(color),
                Math.round(Color.red(color) * factor), Math.round(Color.green(color) * factor),
                Math.round(Color.blue(color) * factor));
    }

    private void setupScrollView() {
        //mScrollView.setCallback(this);
        mScrollView.addCallbacks(this);

        ViewTreeObserver viewTreeObserver = mScrollView.getViewTreeObserver();
        if (viewTreeObserver.isAlive()) {
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    // get size
                    int toolbarLinearLayoutHeight = mToolbarLinearLayout.getHeight();
                    int imageHeight = headerImageView.getHeight();

                    // adjust image frame layout height
                    ViewGroup.LayoutParams layoutParams = mImageFrameLayout.getLayoutParams();
                    if (layoutParams.height != imageHeight) {
                        layoutParams.height = imageHeight;
                        mImageFrameLayout.setLayoutParams(layoutParams);
                    }

                    // adjust top margin of content linear layout
                    ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mContentLinearLayout.getLayoutParams();
                    if (marginLayoutParams.topMargin != toolbarLinearLayoutHeight + imageHeight) {
                        marginLayoutParams.topMargin = toolbarLinearLayoutHeight + imageHeight;
                        mContentLinearLayout.setLayoutParams(marginLayoutParams);
                    }

                    // call onScrollChange to update initial properties.
                    onScrollChanged(0, 0);
                }
            });
        }
    }

    @Override
    public void onScrollChanged(int deltaX, int deltaY)  {
        // get scroll y
        int scrollY = mScrollView.getScrollY();

        // choose appropriate y
        float newY = Math.max(headerImageView.getHeight(), scrollY);

        // translate image and toolbar
        ViewCompat.setTranslationY(mToolbarLinearLayout, newY);
        ViewCompat.setTranslationY(mImageFrameLayout, scrollY * 0.5f);
    }

    //@Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.news_reader_activity, container, false);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.news_reader_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        // Handle the other action bar items here
        //Toast.makeText(MainActivity.this, item.getItemId() + "  " + item.getTitle(), Toast.LENGTH_LONG).show();
        switch (item.getItemId()) {
            /*
            case android.R.id.home:
                Toast.makeText(MainActivity.this, "Deal link is currently unavailable", Toast.LENGTH_LONG).show();
                onBackPressed();
                return true;
                */
            case R.id.news_share:
                Intent shareIntent = ShareCompat.IntentBuilder.from(this)
                        .setSubject(news_title)
                        .setText(news_url + "\n\n" + getResources().getText(R.string.share_subtext))
                        .setType("text/plain")
                        .getIntent();
                startActivity(shareIntent);
                return true;
            case R.id.news_open_browser:
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(news_url));
                startActivity(i);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}