package net.dahanne.banq.notifications;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends FragmentActivity {

    private ViewPager viewPager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Account[] accounts = AccountManager.get(MainActivity.this).getAccountsByType(getString(R.string.accountType));
        if (accounts.length == 0) {
            startActivity(LoginActivity.newIntent(this, true));
            finish();
        } else {
            setContentView(R.layout.activity_main);
            viewPager = (ViewPager) findViewById(R.id.pager);
            viewPager.setAdapter(new AccountFragmentPagerAdapter(getSupportFragmentManager(), accounts));
            viewPager.setPageTransformer(true, new DepthPageTransformer());
            ((PagerTabStrip) findViewById(R.id.tabStrip)).setTabIndicatorColor(Color.BLUE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivity(PreferencesActivity.newIntent(this));
//            case R.id.action_test_notification:
//                NotificationHelper.launchNotification(this, new BorrowedItem("Book title", "BTU", new Date(), new Date(), "", ""));
        }
        return super.onMenuItemSelected(featureId, item);
    }

    public static Intent newIntent(Context context) {
        return new Intent(context, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
    }

    public class AccountFragmentPagerAdapter extends FragmentPagerAdapter {
        List<AccountFragment> fragments = new ArrayList<AccountFragment>();

        public AccountFragmentPagerAdapter(FragmentManager fm, Account[] accounts) {
            super(fm);

            for (Account account : accounts) {
                fragments.add(AccountFragment.newInstance(account));
            }
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return ((AccountFragment) getItem(position)).getTitle();
        }

        public void refreshFragments() {
            for (AccountFragment fragment : fragments) {
                fragment.refresh();
            }
        }
    }

}
