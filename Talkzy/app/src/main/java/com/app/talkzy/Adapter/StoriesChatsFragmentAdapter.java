package com.app.talkzy.Adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.app.talkzy.Fragment.ChatsFragment;
import com.app.talkzy.Fragment.StoriesFragment;

public class StoriesChatsFragmentAdapter extends FragmentStateAdapter {
    public StoriesChatsFragmentAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);

    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {

        switch (position)
        {
            case 1 :
                return new StoriesFragment();

        }

        return new ChatsFragment();
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}