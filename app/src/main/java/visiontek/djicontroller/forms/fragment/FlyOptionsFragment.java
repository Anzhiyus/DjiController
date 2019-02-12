package visiontek.djicontroller.forms.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import visiontek.djicontroller.R;

//参数设置TAB
public class FlyOptionsFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fly_options_widgets, null);
        return layout;
    }
}
