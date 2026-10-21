package com.app.androidnewsapp.adapter;

import static com.app.androidnewsapp.util.Constant.PAGER_NUMBER_DEFAULT;
import static com.app.androidnewsapp.util.Constant.PAGER_NUMBER_NO_VIDEO;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.app.androidnewsapp.fragment.FragmentCategory;
import com.app.androidnewsapp.fragment.FragmentFavorite;
import com.app.androidnewsapp.fragment.FragmentPost;
import com.app.androidnewsapp.fragment.FragmentVideo;

@SuppressWarnings("ALL")
public class AdapterNavigation {

    public static class Default extends FragmentPagerAdapter {

        public Default(FragmentManager fm) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {

            switch (position) {
                case 0:
                    return new FragmentPost();
                case 1:
                    return new FragmentCategory();
                case 2:
                    return new FragmentVideo();
                case 3:
                    return new FragmentFavorite();
            }
            return null;
        }

        @Override
        public int getCount() {
            return PAGER_NUMBER_DEFAULT;
        }

    }

    public static class DefaultNoVideo extends FragmentPagerAdapter {

        public DefaultNoVideo(FragmentManager fm) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {

            switch (position) {
                case 0:
                    return new FragmentPost();
                case 1:
                    return new FragmentCategory();
                case 2:
                    return new FragmentFavorite();
            }
            return null;
        }

        @Override
        public int getCount() {
            return PAGER_NUMBER_NO_VIDEO;
        }

    }

    public static class Category extends FragmentPagerAdapter {

        public Category(FragmentManager fm) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {

            switch (position) {
                case 0:
                    return new FragmentCategory();
                case 1:
                    return new FragmentPost();
                case 2:
                    return new FragmentVideo();
                case 3:
                    return new FragmentFavorite();
            }
            return null;
        }

        @Override
        public int getCount() {
            return PAGER_NUMBER_DEFAULT;
        }

    }

    public static class CategoryNoVideo extends FragmentPagerAdapter {

        public CategoryNoVideo(FragmentManager fm) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {

            switch (position) {
                case 0:
                    return new FragmentCategory();
                case 1:
                    return new FragmentPost();
                case 2:
                    return new FragmentFavorite();
            }
            return null;
        }

        @Override
        public int getCount() {
            return PAGER_NUMBER_NO_VIDEO;
        }

    }

}
