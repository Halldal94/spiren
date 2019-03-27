package com.gardeners.spiren;

import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.ux.ArFragment;

public class SpirenArFragment extends ArFragment {
    private MainActivity activity;

    @Override
    public void onUpdate(FrameTime frameTime) {
        super.onUpdate(frameTime);
        if (activity != null) {
            activity.onUpdate(frameTime);
        }
    }

    public void setActivity(MainActivity activity) {
        this.activity = activity;
    }
}
