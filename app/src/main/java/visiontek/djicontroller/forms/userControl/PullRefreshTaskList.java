package visiontek.djicontroller.forms.userControl;

import android.content.Context;
import android.graphics.Canvas;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import com.qmuiteam.qmui.util.QMUIDirection;
import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.util.QMUIViewHelper;
import com.qmuiteam.qmui.widget.pullRefreshLayout.QMUIPullRefreshLayout;
import visiontek.djicontroller.R;
import visiontek.djicontroller.dataManager.TaskManager;
import visiontek.djicontroller.forms.Adapter.TaskListViewAdapter_new;
import visiontek.djicontroller.forms.dialogs.TaskInfoDialog;

public class PullRefreshTaskList extends LinearLayout{
    ImageView addBtn;
    ImageView closeBtn;
    int visible= View.GONE;//默认显示
    QMUIPullRefreshLayout mPullRefreshLayout;
    TaskListViewAdapter_new mAdapter;
    ListView mListView;
    private TaskManager taskManager=new TaskManager();
    private Context _context;
    public PullRefreshTaskList(final Context context, AttributeSet attrs) {
        super(context, attrs);
        _context=context;
        // 加载布局
        View view= LayoutInflater.from(context).inflate(R.layout.tasklist,this,true);//最后参数必须为true
        // 获取控件
        addBtn = view.findViewById(R.id.addBtn);
        closeBtn = view.findViewById(R.id.close_Edit);
        mPullRefreshLayout=view.findViewById(R.id.pull_to_refresh);
        mListView=view.findViewById(R.id.listview);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                visible=View.GONE;
                QMUIViewHelper.slideOut(PullRefreshTaskList.this, 300, null, true, QMUIDirection.LEFT_TO_RIGHT);
            }
        });
        addBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager mnger= ((FragmentActivity)_context).getSupportFragmentManager();
                TaskInfoDialog dlg=new TaskInfoDialog();
                dlg.show(mnger,"task");
            }
        });

    }

    public void Toggle(){
        if(visible==View.GONE){
            visible=View.VISIBLE;
            QMUIViewHelper.slideIn(this, 300, null, true, QMUIDirection.RIGHT_TO_LEFT);
        }
        else{
            visible=View.GONE;
            QMUIViewHelper.slideOut(this, 300, null, true, QMUIDirection.LEFT_TO_RIGHT);
        }
    }
    private TaskListViewAdapter_new.onClickEvent _lisener;
    public void SetListViewEventLisener(TaskListViewAdapter_new.onClickEvent lisener){
        _lisener=lisener;
        initData();
    }
    public void RefreshData(){
        mAdapter.SetData(taskManager.getTaskList());
        mAdapter.notifyDataSetChanged();
    }
    private void initData() {//刷新数据
        mAdapter=new TaskListViewAdapter_new(_context,_lisener);
        mAdapter.SetData(taskManager.getTaskList());
        mListView.setAdapter(mAdapter);
        mPullRefreshLayout.setOnPullListener(new QMUIPullRefreshLayout.OnPullListener() {
            @Override
            public void onMoveTarget(int offset) {
            }

            @Override
            public void onMoveRefreshView(int offset) {
            }

            @Override
            public void onRefresh() {
                mPullRefreshLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.SetData(taskManager.getTaskList());
                        mAdapter.notifyDataSetChanged();
                        mPullRefreshLayout.finishRefresh();
                    }
                }, 1000);
            }
        });
    }
}
