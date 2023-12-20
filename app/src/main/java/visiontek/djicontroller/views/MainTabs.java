package visiontek.djicontroller.views;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.WindowManager;

import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.widget.QMUITabSegment;
import com.qmuiteam.qmui.widget.QMUIViewPager;

import java.util.ArrayList;
import java.util.List;

import visiontek.djicontroller.R;
import visiontek.djicontroller.forms.fragment.FPVFragment;
import visiontek.djicontroller.forms.fragment.TaskControlFragment;
import visiontek.djicontroller.forms.fragment.TaskEditFragment;

public class MainTabs extends AppCompatActivity {
    private QMUITabSegment tabSegment;
    private QMUIViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tabs_main);

        tabSegment = findViewById(R.id.tabSegment);
        viewPager = findViewById(R.id.contentViewPager);

        final List<Fragment> fragments= new ArrayList<>();
        TaskEditFragment tab1=new TaskEditFragment();
        TaskControlFragment tab3=new TaskControlFragment();
        FPVFragment tab4=new FPVFragment();
        fragments.add(tab1);
        //fragments.add(tab2);
//        fragments.add(tab3);
//        fragments.add(tab4);

        BaseFragmentPagerAdapter adapter = new BaseFragmentPagerAdapter(getSupportFragmentManager(), fragments);
        viewPager.setAdapter(adapter);
        viewPager.setSwipeable(false);
        viewPager.setOffscreenPageLimit(4);//内容过于复杂包括地图和大疆控件 全部缓存防止卡顿和对象销毁造成闪退
        tabSegment.addTab(new QMUITabSegment.Tab("任务规划 "));
        //tabSegment.addTab(new QMUITabSegment.Tab("飞行参数"));
//        tabSegment.addTab(new QMUITabSegment.Tab("任务执行 "));
//        tabSegment.addTab(new QMUITabSegment.Tab("实时图传 "));
        //tabSegment.setDefaultNormalColor(getResources().getColor(R.color.white,null));
        int space = QMUIDisplayHelper.dp2px(this, 16);
        tabSegment.setHasIndicator(true);

        tabSegment.setMode(QMUITabSegment.MODE_FIXED);  //MODE_SCROLLABLE 自适应宽度+滚动   MODE_FIXED  平均
        tabSegment.setItemSpaceInScrollMode(space);

        tabSegment.setupWithViewPager(viewPager, false);
        tabSegment.setPadding(space, 0, space, 0);
        tabSegment.setOnTabClickListener(new QMUITabSegment.OnTabClickListener() {
            @Override
            public void onTabClick(int index) {
                Fragment tag= fragments.get(index);

            }
        });
        tabSegment.addOnTabSelectedListener(new QMUITabSegment.OnTabSelectedListener() {
            @Override
            public void onTabSelected(int index) {
                
            }

            @Override
            public void onTabUnselected(int index) {

            }

            @Override
            public void onTabReselected(int index) {

            }

            @Override
            public void onDoubleTap(int index) {

            }
        });
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);//屏幕常亮
    }
    @Override
    public boolean onKeyDown(int keyCode,KeyEvent event){
        if(keyCode== KeyEvent.KEYCODE_BACK){
            //moveTaskToBack(false);
            return true;//不执行父类点击事件
        }
        return super.onKeyDown(keyCode, event);//继续执行父类其他点击事件
    }
    private class BaseFragmentPagerAdapter extends FragmentPagerAdapter {
        private List<Fragment> mDataList;

        public BaseFragmentPagerAdapter(FragmentManager fm, List<Fragment> dataList) {
            super(fm);
            mDataList = dataList;
        }
        @Override
        public Fragment getItem(int position) {
            return mDataList.get(position);
        }
        @Override
        public int getCount() {
            return mDataList.size();
        }
    }

}
