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
import visiontek.djicontroller.orm.FlyCamera;

public class CameraListViewAdapter_new extends BaseAdapter {
    private Context context;
    public List<FlyCamera> _data;
    private onClickEvent _event;

    public CameraListViewAdapter_new(Context context,onClickEvent event){
        this.context=context;
        this._event=event;
    }

    public void SetData(List<FlyCamera> list){
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
        final FlyCamera entity=_data.get(position);
        if(convertView==null) {
            viewHolder=new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.item_camera_swipe_new,null);
            viewHolder.tvContent= (TextView) convertView.findViewById(R.id.tvContent);
            viewHolder.btnDelete= (Button) convertView.findViewById(R.id.btnDelete);
        }else{
            viewHolder= (ViewHolder) convertView.getTag();
        }
        viewHolder.tvContent.setText(_data.get(position).getName()+"");
        viewHolder.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _event.onRemoveClick(entity.id);
            }
        });
        convertView.setTag(viewHolder);
        viewHolder.tvContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _event.onItemClick(_data.get(position).id);
            }
        });

        return convertView;
    }
    class ViewHolder{
        TextView tvContent;
        Button btnDelete;
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
        void onRemoveClick(String taskid);
        void onItemClick(String taskid);
    }

}
