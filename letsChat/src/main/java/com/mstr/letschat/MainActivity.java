package com.mstr.letschat;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.mstr.letschat.bitmapcache.ImageFetcher;
import com.mstr.letschat.fragments.ContactListFragment;
import com.mstr.letschat.fragments.ConversationFragment;
import com.mstr.letschat.service.MessageService;

/**
 * Created by dilli on 12/24/2015.
 */
public class MainActivity extends AppCompatActivity {
    private ImageFetcher imageFetcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // start service to login
        startService(new Intent(MessageService.ACTION_CONNECT, null, this, MessageService.class));

        imageFetcher = ImageFetcher.getAvatarImageFetcher(this);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ViewPager pager = (ViewPager)findViewById(R.id.pager);
        MainFragmentPagerAdapter adapter = new MainFragmentPagerAdapter(this);
        pager.setAdapter(adapter);

        TabLayout tabLayout = (TabLayout)findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(pager);
    }

    public ImageFetcher getImageFetcher() {
        return imageFetcher;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add:
                startActivity(new Intent(this, SearchUserActivity.class));
                return true;

            case R.id.action_set_status:
                startActivity(new Intent(this, SetStatusActivity.class));
                return true;

            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();

        imageFetcher.flushCache();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        imageFetcher.closeCache();
    }

    static class MainFragmentPagerAdapter extends FragmentPagerAdapter {
        private static final int COUNT = 2;
        private Activity activity;

        public MainFragmentPagerAdapter(Activity activity) {
            super(activity.getFragmentManager());
            this.activity = activity;
        }

        public Fragment getItem(int position) {
            if (position == 0) {
                return new ConversationFragment();
            }

            if (position == 1) {
                return new ContactListFragment();
            }

            throw new IllegalArgumentException("invalid position");
        }

        public int getCount() {
            return COUNT;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (position == 0) {
                return activity.getString(R.string.chats);
            }

            if (position == 1) {
                return activity.getString(R.string.contacts);
            }

            throw new IllegalArgumentException("invalid position");
        }
    }
}