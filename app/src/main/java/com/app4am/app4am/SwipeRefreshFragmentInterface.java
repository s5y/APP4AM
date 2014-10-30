package com.app4am.app4am;

/**
 * Created by dino on 2014/10/29.
 */
public interface SwipeRefreshFragmentInterface {
    public static final String FRAGMENT_POSITION = "fragment_position";

    public abstract void onRefreshRequest(SwipeRefreshListFragment fragment);
}
