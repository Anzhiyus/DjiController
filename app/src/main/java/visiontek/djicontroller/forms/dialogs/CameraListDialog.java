package visiontek.djicontroller.forms.dialogs;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;
import com.qmuiteam.qmui.widget.pullRefreshLayout.QMUIPullRefreshLayout;

import visiontek.djicontroller.R;
import visiontek.djicontroller.dataManager.CameraManager;
import visiontek.djicontroller.forms.Adapter.CameraListViewAdapter;

public class CameraListDialog extends DialogFragment {
    private LinearLayout dialog;
    ImageView addBtn;
    ImageView closeBtn;
    QMUIPullRefreshLayout mPullRefreshLayout;
    CameraListViewAdapter mAdapter;
    ListView mListView;
    CameraManager manager=new CameraManager();
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
        dialog= (LinearLayout) inflater.inflate(R.layout.cameralist,null);
        manager=new CameraManager();
        initUI();
        initData();
        return dialog;
    }
    @Override
    public void show(FragmentManager manager, String tag) {
        try {
            //在每个add事务前增加一个remove事务，防止连续的add
            manager.beginTransaction().remove(this).commit();
            super.show(manager, tag);
        } catch (Exception e) {
            //同一实例使用不同的tag会异常,这里捕获一下
            e.printStackTrace();
        }
    }
    private void initUI(){
        addBtn=dialog.findViewById(R.id.addBtn);
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CameraInfoDialog editdlg=new CameraInfoDialog();
                editdlg.show(getFragmentManager(),"camera");
            }
        });
        closeBtn=dialog.findViewById(R.id.closeBtn);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        mListView=dialog.findViewById(R.id.listview);
        mPullRefreshLayout=dialog.findViewById(R.id.pull_to_refresh);
    }
    private CameraListViewAdapter.onClickEvent lisener=new CameraListViewAdapter.onClickEvent() {
        @Override
        public void onRemoveClick(String id) {
            final String cameraid=id;
            new QMUIDialog.MessageDialogBuilder(getActivity())
                    .setTitle("删除相机")
                    .setMessage("确定要删除吗？(使用该相机的任务也会被删除)")
                    .addAction("取消", new QMUIDialogAction.ActionListener() {
                        @Override
                        public void onClick(QMUIDialog dialog, int index) {
                            dialog.dismiss();
                        }
                    })
                    .addAction("确定", new QMUIDialogAction.ActionListener() {
                        @Override
                        public void onClick(QMUIDialog dialog, int index) {
                            dialog.dismiss();
                            manager.RemoveCamera(cameraid);
                            mAdapter.SetData(manager.getCameraList());
                            mAdapter.notifyDataSetChanged();
                        }
                    })
                    .create().show();
        }

        @Override
        public void onItemClick(String id) {
            Bundle bundle = new Bundle();
            bundle.putString("cameraid",id);
            CameraInfoDialog editdlg=new CameraInfoDialog();
            editdlg.setArguments(bundle);
            editdlg.show(getFragmentManager(),"camera");
        }
    };
    private void initData() {//刷新数据
        mAdapter=new CameraListViewAdapter(getContext(),lisener);
        mAdapter.SetData(manager.getCameraList());
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
                        mAdapter.SetData(manager.getCameraList());
                        mAdapter.notifyDataSetChanged();
                        mPullRefreshLayout.finishRefresh();
                    }
                }, 1000);
            }
        });
    }
}
