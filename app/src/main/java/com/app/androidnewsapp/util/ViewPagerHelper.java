package com.app.androidnewsapp.util;

import static com.app.androidnewsapp.util.Constant.PAGER_NUMBER_DEFAULT;
import static com.app.androidnewsapp.util.Constant.PAGER_NUMBER_NO_VIDEO;

import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.app.androidnewsapp.R;
import com.app.androidnewsapp.adapter.AdapterNavigation;
import com.app.androidnewsapp.config.AppConfig;
import com.app.androidnewsapp.database.prefs.SharedPref;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ViewPagerHelper {

    AppCompatActivity activity;
    MenuItem prevMenuItem;
    SharedPref sharedPref;

    public ViewPagerHelper(AppCompatActivity activity) {
        this.activity = activity;
        this.sharedPref = new SharedPref(activity);
    }

    public void setupViewPager(ViewPager viewPager, BottomNavigationView navigation, TextView title_toolbar) {
        if (AppConfig.SET_CATEGORY_AS_MAIN_PAGE) {
            if (sharedPref.getVideoMenu().equals("yes")) {
                setupViewPagerCategory(viewPager, navigation, title_toolbar);
            } else {
                setupViewPagerCategoryNoVideo(viewPager, navigation, title_toolbar);
            }
        } else {
            if (sharedPref.getVideoMenu().equals("yes")) {
                setupViewPagerDefault(viewPager, navigation, title_toolbar);
            } else {
                setupViewPagerDefaultNoVideo(viewPager, navigation, title_toolbar);
            }
        }
    }

    public void setupViewPagerRTL(RtlViewPager viewPagerRTL, BottomNavigationView navigation, TextView title_toolbar) {
        if (AppConfig.SET_CATEGORY_AS_MAIN_PAGE) {
            if (sharedPref.getVideoMenu().equals("yes")) {
                setupViewPagerRtlCategory(viewPagerRTL, navigation, title_toolbar);
            } else {
                setupViewPagerRtlCategoryNoVideo(viewPagerRTL, navigation, title_toolbar);
            }
        } else {
            if (sharedPref.getVideoMenu().equals("yes")) {
                setupViewPagerRtlDefault(viewPagerRTL, navigation, title_toolbar);
            } else {
                setupViewPagerRtlDefaultNoVideo(viewPagerRTL, navigation, title_toolbar);
            }
        }
    }

    //posts as main page
    public void setupViewPagerDefault(ViewPager viewPager, BottomNavigationView navigation, TextView title_toolbar) {
        viewPager.setVisibility(View.VISIBLE);
        viewPager.setAdapter(new AdapterNavigation.Default(activity.getSupportFragmentManager()));
        viewPager.setOffscreenPageLimit(PAGER_NUMBER_DEFAULT);
        navigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_recent) {
                viewPager.setCurrentItem(0);
                return true;
            } else if (itemId == R.id.navigation_category) {
                viewPager.setCurrentItem(1);
                return true;
            } else if (itemId == R.id.navigation_video) {
                viewPager.setCurrentItem(2);
                return true;
            } else if (itemId == R.id.navigation_favorite) {
                viewPager.setCurrentItem(3);
                return true;
            }
            return false;
        });

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (prevMenuItem != null) {
                    prevMenuItem.setChecked(false);
                } else {
                    navigation.getMenu().getItem(0).setChecked(false);
                }
                navigation.getMenu().getItem(position).setChecked(true);
                prevMenuItem = navigation.getMenu().getItem(position);

                if (viewPager.getCurrentItem() == 0) {
                    title_toolbar.setText(activity.getResources().getString(R.string.app_name));
                } else if (viewPager.getCurrentItem() == 1) {
                    title_toolbar.setText(activity.getResources().getString(R.string.title_nav_category));
                } else if (viewPager.getCurrentItem() == 2) {
                    title_toolbar.setText(activity.getResources().getString(R.string.title_nav_video));
                } else if (viewPager.getCurrentItem() == 3) {
                    title_toolbar.setText(activity.getResources().getString(R.string.title_nav_favorite));
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    public void setupViewPagerDefaultNoVideo(ViewPager viewPager, BottomNavigationView navigation, TextView title_toolbar) {
        viewPager.setVisibility(View.VISIBLE);
        viewPager.setAdapter(new AdapterNavigation.DefaultNoVideo(activity.getSupportFragmentManager()));
        viewPager.setOffscreenPageLimit(PAGER_NUMBER_NO_VIDEO);
        navigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_recent) {
                viewPager.setCurrentItem(0);
                return true;
            } else if (itemId == R.id.navigation_category) {
                viewPager.setCurrentItem(1);
                return true;
            } else if (itemId == R.id.navigation_favorite) {
                viewPager.setCurrentItem(2);
                return true;
            }
            return false;
        });

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (prevMenuItem != null) {
                    prevMenuItem.setChecked(false);
                } else {
                    navigation.getMenu().getItem(0).setChecked(false);
                }
                navigation.getMenu().getItem(position).setChecked(true);
                prevMenuItem = navigation.getMenu().getItem(position);

                if (viewPager.getCurrentItem() == 0) {
                    title_toolbar.setText(activity.getResources().getString(R.string.app_name));
                } else if (viewPager.getCurrentItem() == 1) {
                    title_toolbar.setText(activity.getResources().getString(R.string.title_nav_category));
                } else if (viewPager.getCurrentItem() == 2) {
                    title_toolbar.setText(activity.getResources().getString(R.string.title_nav_favorite));
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    public void setupViewPagerRtlDefault(RtlViewPager viewPagerRTL, BottomNavigationView navigation, TextView title_toolbar) {
        viewPagerRTL.setVisibility(View.VISIBLE);
        viewPagerRTL.setAdapter(new AdapterNavigation.Default(activity.getSupportFragmentManager()));
        viewPagerRTL.setOffscreenPageLimit(PAGER_NUMBER_DEFAULT);
        navigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_recent) {
                viewPagerRTL.setCurrentItem(0);
                return true;
            } else if (itemId == R.id.navigation_category) {
                viewPagerRTL.setCurrentItem(1);
                return true;
            } else if (itemId == R.id.navigation_video) {
                viewPagerRTL.setCurrentItem(2);
                return true;
            } else if (itemId == R.id.navigation_favorite) {
                viewPagerRTL.setCurrentItem(3);
                return true;
            }
            return false;
        });

        viewPagerRTL.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (prevMenuItem != null) {
                    prevMenuItem.setChecked(false);
                } else {
                    navigation.getMenu().getItem(0).setChecked(false);
                }
                navigation.getMenu().getItem(position).setChecked(true);
                prevMenuItem = navigation.getMenu().getItem(position);

                if (viewPagerRTL.getCurrentItem() == 0) {
                    title_toolbar.setText(activity.getResources().getString(R.string.app_name));
                } else if (viewPagerRTL.getCurrentItem() == 1) {
                    title_toolbar.setText(activity.getResources().getString(R.string.title_nav_category));
                } else if (viewPagerRTL.getCurrentItem() == 2) {
                    title_toolbar.setText(activity.getResources().getString(R.string.title_nav_video));
                } else if (viewPagerRTL.getCurrentItem() == 3) {
                    title_toolbar.setText(activity.getResources().getString(R.string.title_nav_favorite));
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    public void setupViewPagerRtlDefaultNoVideo(RtlViewPager viewPagerRTL, BottomNavigationView navigation, TextView title_toolbar) {
        viewPagerRTL.setVisibility(View.VISIBLE);
        viewPagerRTL.setAdapter(new AdapterNavigation.DefaultNoVideo(activity.getSupportFragmentManager()));
        viewPagerRTL.setOffscreenPageLimit(PAGER_NUMBER_NO_VIDEO);
        navigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_recent) {
                viewPagerRTL.setCurrentItem(0);
                return true;
            } else if (itemId == R.id.navigation_category) {
                viewPagerRTL.setCurrentItem(1);
                return true;
            } else if (itemId == R.id.navigation_favorite) {
                viewPagerRTL.setCurrentItem(2);
                return true;
            }
            return false;
        });

        viewPagerRTL.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (prevMenuItem != null) {
                    prevMenuItem.setChecked(false);
                } else {
                    navigation.getMenu().getItem(0).setChecked(false);
                }
                navigation.getMenu().getItem(position).setChecked(true);
                prevMenuItem = navigation.getMenu().getItem(position);

                if (viewPagerRTL.getCurrentItem() == 0) {
                    title_toolbar.setText(activity.getResources().getString(R.string.app_name));
                } else if (viewPagerRTL.getCurrentItem() == 1) {
                    title_toolbar.setText(activity.getResources().getString(R.string.title_nav_category));
                } else if (viewPagerRTL.getCurrentItem() == 2) {
                    title_toolbar.setText(activity.getResources().getString(R.string.title_nav_favorite));
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    //category as main page
    public void setupViewPagerCategory(ViewPager viewPager, BottomNavigationView navigation, TextView title_toolbar) {
        viewPager.setVisibility(View.VISIBLE);
        viewPager.setAdapter(new AdapterNavigation.Category(activity.getSupportFragmentManager()));
        viewPager.setOffscreenPageLimit(PAGER_NUMBER_DEFAULT);
        navigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_category) {
                viewPager.setCurrentItem(0);
                return true;
            } else if (itemId == R.id.navigation_recent) {
                viewPager.setCurrentItem(1);
                return true;
            } else if (itemId == R.id.navigation_video) {
                viewPager.setCurrentItem(2);
                return true;
            } else if (itemId == R.id.navigation_favorite) {
                viewPager.setCurrentItem(3);
                return true;
            }
            return false;
        });

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (prevMenuItem != null) {
                    prevMenuItem.setChecked(false);
                } else {
                    navigation.getMenu().getItem(0).setChecked(false);
                }
                navigation.getMenu().getItem(position).setChecked(true);
                prevMenuItem = navigation.getMenu().getItem(position);

                if (viewPager.getCurrentItem() == 0) {
                    title_toolbar.setText(activity.getResources().getString(R.string.app_name));
                } else if (viewPager.getCurrentItem() == 1) {
                    title_toolbar.setText(activity.getResources().getString(R.string.title_nav_recent));
                } else if (viewPager.getCurrentItem() == 2) {
                    title_toolbar.setText(activity.getResources().getString(R.string.title_nav_video));
                } else if (viewPager.getCurrentItem() == 3) {
                    title_toolbar.setText(activity.getResources().getString(R.string.title_nav_favorite));
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    public void setupViewPagerCategoryNoVideo(ViewPager viewPager, BottomNavigationView navigation, TextView title_toolbar) {
        viewPager.setVisibility(View.VISIBLE);
        viewPager.setAdapter(new AdapterNavigation.CategoryNoVideo(activity.getSupportFragmentManager()));
        viewPager.setOffscreenPageLimit(PAGER_NUMBER_NO_VIDEO);
        navigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_category) {
                viewPager.setCurrentItem(0);
                return true;
            } else if (itemId == R.id.navigation_recent) {
                viewPager.setCurrentItem(1);
                return true;
            } else if (itemId == R.id.navigation_favorite) {
                viewPager.setCurrentItem(2);
                return true;
            }
            return false;
        });

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (prevMenuItem != null) {
                    prevMenuItem.setChecked(false);
                } else {
                    navigation.getMenu().getItem(0).setChecked(false);
                }
                navigation.getMenu().getItem(position).setChecked(true);
                prevMenuItem = navigation.getMenu().getItem(position);

                if (viewPager.getCurrentItem() == 0) {
                    title_toolbar.setText(activity.getResources().getString(R.string.app_name));
                } else if (viewPager.getCurrentItem() == 1) {
                    title_toolbar.setText(activity.getResources().getString(R.string.title_nav_recent));
                } else if (viewPager.getCurrentItem() == 2) {
                    title_toolbar.setText(activity.getResources().getString(R.string.title_nav_favorite));
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    public void setupViewPagerRtlCategory(RtlViewPager viewPagerRTL, BottomNavigationView navigation, TextView title_toolbar) {
        viewPagerRTL.setVisibility(View.VISIBLE);
        viewPagerRTL.setAdapter(new AdapterNavigation.Category(activity.getSupportFragmentManager()));
        viewPagerRTL.setOffscreenPageLimit(PAGER_NUMBER_DEFAULT);
        navigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_category) {
                viewPagerRTL.setCurrentItem(0);
                return true;
            } else if (itemId == R.id.navigation_recent) {
                viewPagerRTL.setCurrentItem(1);
                return true;
            } else if (itemId == R.id.navigation_video) {
                viewPagerRTL.setCurrentItem(2);
                return true;
            } else if (itemId == R.id.navigation_favorite) {
                viewPagerRTL.setCurrentItem(3);
                return true;
            }
            return false;
        });

        viewPagerRTL.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (prevMenuItem != null) {
                    prevMenuItem.setChecked(false);
                } else {
                    navigation.getMenu().getItem(0).setChecked(false);
                }
                navigation.getMenu().getItem(position).setChecked(true);
                prevMenuItem = navigation.getMenu().getItem(position);

                if (viewPagerRTL.getCurrentItem() == 0) {
                    title_toolbar.setText(activity.getResources().getString(R.string.app_name));
                } else if (viewPagerRTL.getCurrentItem() == 1) {
                    title_toolbar.setText(activity.getResources().getString(R.string.title_nav_recent));
                } else if (viewPagerRTL.getCurrentItem() == 2) {
                    title_toolbar.setText(activity.getResources().getString(R.string.title_nav_video));
                } else if (viewPagerRTL.getCurrentItem() == 3) {
                    title_toolbar.setText(activity.getResources().getString(R.string.title_nav_favorite));
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    public void setupViewPagerRtlCategoryNoVideo(RtlViewPager viewPagerRTL, BottomNavigationView navigation, TextView title_toolbar) {
        viewPagerRTL.setVisibility(View.VISIBLE);
        viewPagerRTL.setAdapter(new AdapterNavigation.CategoryNoVideo(activity.getSupportFragmentManager()));
        viewPagerRTL.setOffscreenPageLimit(PAGER_NUMBER_NO_VIDEO);
        navigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_category) {
                viewPagerRTL.setCurrentItem(0);
                return true;
            } else if (itemId == R.id.navigation_recent) {
                viewPagerRTL.setCurrentItem(1);
                return true;
            } else if (itemId == R.id.navigation_favorite) {
                viewPagerRTL.setCurrentItem(2);
                return true;
            }
            return false;
        });

        viewPagerRTL.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (prevMenuItem != null) {
                    prevMenuItem.setChecked(false);
                } else {
                    navigation.getMenu().getItem(0).setChecked(false);
                }
                navigation.getMenu().getItem(position).setChecked(true);
                prevMenuItem = navigation.getMenu().getItem(position);

                if (viewPagerRTL.getCurrentItem() == 0) {
                    title_toolbar.setText(activity.getResources().getString(R.string.app_name));
                } else if (viewPagerRTL.getCurrentItem() == 1) {
                    title_toolbar.setText(activity.getResources().getString(R.string.title_nav_recent));
                } else if (viewPagerRTL.getCurrentItem() == 2) {
                    title_toolbar.setText(activity.getResources().getString(R.string.title_nav_favorite));
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

}
