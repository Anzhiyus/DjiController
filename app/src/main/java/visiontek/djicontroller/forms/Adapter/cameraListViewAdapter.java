package visiontek.djicontroller.forms.Adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import java.util.List;
import visiontek.djicontroller.R;
import visiontek.djicontroller.orm.FlyCamera;

public class cameraListViewAdapter extends BaseAdapter {

    private Context _context;
    private onClickEvent _event;

    public List<FlyCamera> data;
    public cameraListViewAdapter(Context context,onClickEvent event) {
        super();
        _context=context;
        _event=event;
    }
    public void SetData(List<FlyCamera> list){
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
        final cameraListViewAdapter.CameraItemHolder viewHolder;
        if(convertView==null){
            convertView=View.inflate(_context, R.layout.cameraitem, null);
            viewHolder=new cameraListViewAdapter.CameraItemHolder();
            viewHolder.NameTextView=convertView.findViewById(R.id.cameraname);
            viewHolder.removeBtn= convertView.findViewById(R.id.remove);
        }else{
            viewHolder=(cameraListViewAdapter.CameraItemHolder) convertView.getTag();
        }
        viewHolder.NameTextView.setText(data.get(position).name);
        viewHolder.Entity=data.get(position);
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
    private class CameraItemHolder{
        public TextView NameTextView;
        public TextView removeBtn;
        public FlyCamera Entity;
    }

    public interface onClickEvent{
        void onRemoveClick(String taskid);
        void onItemClick(String taskid);
    }
}
