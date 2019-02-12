package visiontek.djicontroller.forms.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import java.util.List;

import visiontek.djicontroller.R;

import visiontek.djicontroller.orm.FlyTask;

public class TaskListViewAdapter_new extends BaseAdapter {
    //private List<String> list;
    private Context context;
    public List<FlyTask> _data;
    private onClickEvent _event;

    public TaskListViewAdapter_new(Context context,onClickEvent event){
        this.context=context;
        this._event=event;
    }

    public void SetData(List<FlyTask> list){
        if(list==null){
            _data.clear();
        }
        else{
            _data=list;
        }
    }

    @Override
    public int getCount() {
        return _data.size();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;
        if(convertView==null) {
            viewHolder=new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.taskitem_new,null);
            viewHolder.tvContent=  convertView.findViewById(R.id.tvContent);
            viewHolder.btnDelete=  convertView.findViewById(R.id.btnDelete);
            viewHolder.btnLoad=convertView.findViewById(R.id.btnLoad);
        }else{
            viewHolder= (ViewHolder) convertView.getTag();
        }
        viewHolder.Entity=_data.get(position);
        viewHolder.tvContent.setText(viewHolder.Entity.taskname);
        viewHolder.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _event.onRemoveClick(viewHolder.Entity.id);
            }
        });
        viewHolder.btnLoad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {//点击加载
                _event.onItemClick(viewHolder.Entity.id);
            }
        });
        convertView.setTag(viewHolder);
        viewHolder.tvContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _event.onLoadClick(viewHolder.Entity.id);
            }
        });
        return convertView;
    }

    class ViewHolder{
        TextView tvContent;
        Button btnDelete;
        Button btnLoad;
        FlyTask Entity;
    }

    @Override
    public Object getItem(int position) {
        return _data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public interface onClickEvent{
        void onLoadClick(String taskid);
        void onRemoveClick(String taskid);
        void onItemClick(String taskid);
    }
}
