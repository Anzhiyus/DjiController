package visiontek.djicontroller.forms.Adapter;
import android.content.Context;
import android.provider.ContactsContract;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import visiontek.djicontroller.R;
import visiontek.djicontroller.models.DataEntity;
import visiontek.djicontroller.models.TaskViewModel;
import visiontek.djicontroller.orm.FlyTask;

public class taskListViewAdapter extends BaseAdapter {

    private Context _context;
    //private List<Object> data;
    private onClickEvent _event;

    public List<FlyTask> data;
    public taskListViewAdapter(Context context,onClickEvent event) {
        super();
        _context=context;
        _event=event;
    }
    public void SetData(List<FlyTask> list){
        if(list==null){
            data.clear();
        }
        else{
            data=list;
        }
    }
    @Override
    public int getCount() {
        return data.size();
    }
    @Override
    public Object getItem(int position) {
        return data.get(position);
    }
    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final TaskItemHolder viewHolder;
        if(convertView==null){
            convertView=View.inflate(_context, R.layout.taskitem, null);
            viewHolder=new TaskItemHolder();
            viewHolder.taskNameTextView=(TextView) convertView.findViewById(R.id.taskname);
            viewHolder.loadBtn=(TextView) convertView.findViewById(R.id.loadtask);
            viewHolder.removeBtn=(TextView) convertView.findViewById(R.id.remove);
        }else{
            viewHolder=(TaskItemHolder) convertView.getTag();
        }
        viewHolder.taskNameTextView.setText(data.get(position).taskname);
        viewHolder.Entity=data.get(position);
        viewHolder.loadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _event.onLoadClick(viewHolder.Entity.id);
            }
        });
        viewHolder.removeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _event.onRemoveClick(viewHolder.Entity.id);
            }
        });
        convertView.setTag(viewHolder);
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _event.onItemClick(viewHolder.Entity.id);
            }
        });
        return convertView;
    }
    private class TaskItemHolder{
        public TextView taskNameTextView;
        public TextView loadBtn;
        public TextView removeBtn;
        public FlyTask Entity;
    }

    public interface onClickEvent{
        void onLoadClick(String taskid);
        void onRemoveClick(String taskid);
        void onItemClick(String taskid);
    }
}