package visiontek.djicontroller.util;
import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import visiontek.djicontroller.models.kml.Coordinate;
import visiontek.djicontroller.models.kml.Placemark;


public class KMLReader {

        public static interface Callback {
            public void onDocumentParsed(List<Placemark> placemarks);//只读取范围线
        }

        private static final String LOGTAG = "KMLReader";
        private Callback callback;

        public KMLReader(Callback callback) {
            this.callback = callback;
        }

        public void read(InputStream inputStream) throws IOException, XmlPullParserException {
            ReaderTask task = ReaderTask.getReaderTask(inputStream, callback);
            task.execute();
        }

        public static class ReaderTask extends AsyncTask<Void, Void, List<Placemark>> {

            private static final String MULTIGEOMETRY="MultiGeometry";//
            private static final String PLACEMARK_TAG = "Placemark";//
            private static final String PLACEMARK_POINT_TAG = "Point";//
            private static final String POLYGON_TAG = "Polygon";//
            public static final String LINE_STRING_TAG = "LineString";//

            private static final String DOCUMENT_TAG = "Document";
            private static final String PLACEMARK_NAME_TAG = "name";
            private static final String COORDINATES_TAG = "coordinates";
            private static final String STYLE_TAG = "Style";
            private static final String STYLE_MAP_TAG = "StyleMap";
            private static final String STYLE_ICON_SCALE_TAG = "scale";
            private static final String STYLE_ICON_HREF_TAG = "href";
            private static final String PLACEMARK_DESCRIPTION_TAG = "description";
            private static final String PLACEMARK_ADDRESS_TAG = "address";
            private static final String PLACEMARK_SNIPPET_TAG = "Snippet";
            private static final String PLACEMARK_XDATA_TAG = "ExtendedData";
            private static final String PLACEMARK_STYLE_URL_TAG = "styleUrl";
            private static final String STYLE_MAP_PAIR_TAG = "Pair";
            private static final String KEY_TAG = "key";
            private static final String LINE_STYLE_TAG = "LineStyle";
            private static final String POLY_STYLE_TAG = "PolyStyle";
            private static final String COLOR_TAG = "color";
            private static final String WIDTH_TAG = "width";
            private static final String DATA_TAG = "Data";
            private static final String VALUE_TAG = "value";
            private static final String KML_TAG = "kml";
            private static String CURRENT_TAG="";
            private static String PARENT_TAG="";
            public static List<String> colorList;
            public static Map colorMap;

            //public static  int  TYPE_TAG=0;//记录图形类型，是点线或者面，点为1，线为2，面为3
            private Placemark placemark;
            private List<Placemark> placemarkList;
            private Callback callback;
            InputStream inputStream;

            public ReaderTask() {
            }

            public static ReaderTask getReaderTask(InputStream inputStream, Callback callback) {
                ReaderTask task = new ReaderTask();
                task.inputStream = inputStream;
                task.callback = callback;
                return task;
            }

            @Override
            protected List<Placemark> doInBackground(Void... params) {
                try {
                    read(inputStream);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (XmlPullParserException e) {
                    e.printStackTrace();
                }
                return placemarkList;
            }

            @Override
            protected void onPostExecute(List<Placemark> placemarks) {
                super.onPostExecute(placemarks);
                if(placemarks!=null)
                callback.onDocumentParsed(placemarks);
            }

            public void read(InputStream inputStream) throws IOException, XmlPullParserException {
                XmlPullParserFactory factory = XmlPullParserFactory
                        .newInstance();
                factory.setValidating(false);
                XmlPullParser myxml = factory.newPullParser();
                myxml.setInput(inputStream, null);
                processDocument(myxml);
            }

            protected void processDocument(XmlPullParser xpp)
                    throws XmlPullParserException, IOException {
                placemarkList = new ArrayList<>();
                colorList=new ArrayList<>();
                int eventType=xpp.getEventType();
                do {
                    if (eventType == XmlPullParser.START_DOCUMENT) {
                    } else if (eventType == XmlPullParser.END_DOCUMENT) {
                    } else if (eventType == XmlPullParser.START_TAG) {

                        CURRENT_TAG=xpp.getName();
                        Log.d("KKK","当前节点为："+CURRENT_TAG);
                        if (CURRENT_TAG.equalsIgnoreCase("point")){
                            placemark.type=1;
                        } else if(CURRENT_TAG.equalsIgnoreCase("lineString")){
                            placemark.type=2;
                        }else if (CURRENT_TAG.equalsIgnoreCase("polygon")){
                            placemark.type=3;
                        }
                        processName(xpp.getName(),true);
                    } else if (eventType == XmlPullParser.END_TAG) {
                        CURRENT_TAG="";
                        processName(xpp.getName(), false);
                    } else if (eventType == XmlPullParser.TEXT) {
                        processText(xpp);
                        processColor(xpp);
                    }
                    eventType = xpp.next();
                } while (eventType != XmlPullParser.END_DOCUMENT);
            }

            private void processName(String name, boolean isStart) {
                if(PLACEMARK_TAG.equals(name)){
                    if(isStart) {
                        placemark = new Placemark();
                        placemark.coordinates=new ArrayList<>();
                    } else {
                        placemarkList.add(placemark);
                        Log.d("SSS",String.valueOf(placemark.coordinates.size()));
                        for (int i=0;i<placemark.coordinates.size();i++){
                            Log.d("SSS",String.valueOf(placemark.coordinates.get(i).lat));
                            Log.d("SSS",String.valueOf(placemark.coordinates.get(i).lon));
                            Log.d("SSS",String.valueOf(placemark.coordinates.get(i).height));
                        }
                    }
                }
            }

            //将坐标中的换行符，空格之类的符号都清掉,并将坐标提取出来,提取颜色
            protected void processText(XmlPullParser xpp) throws XmlPullParserException {
                String text = xpp.getText();
                text = text.replaceAll("\t|\f|\\r|\\n", "");
                text=text.trim();
                text=text.replaceAll(" +"," ");
                if(CURRENT_TAG.equals(PLACEMARK_NAME_TAG)&&placemark!=null){
                    placemark.name=text;
                }
                if(CURRENT_TAG.equals(COORDINATES_TAG)){
                    String[] coordinates = text.split(" ");
                    Log.d("mmm","数组的大小为："+String.valueOf(coordinates.length));
                    for(int i=0;i<coordinates.length;i++){
                        Log.d("hhh",coordinates[i]);
                        if (coordinates[i]!=""){
                            Coordinate coordinate =new Coordinate();
                            String[] point = coordinates[i].split(",");
                            coordinate.lon=Double.valueOf(point[0]);
                            Log.d("mmm",String.valueOf(coordinate.lon));
                            coordinate.lat=Double.valueOf(point[1]);
                            Log.d("mmm",String.valueOf(coordinate.lat));
                            coordinate.height=Double.valueOf(point[2]);
                            placemark.coordinates.add(coordinate);
                        }
                    }
                }
            }
            protected void processColor(XmlPullParser xpp){

                if(CURRENT_TAG.equals(COLOR_TAG)&&placemark!=null){
                    placemark.color=xpp.getText();
                    if(!placemark.color.equals(null)&&placemark.color.length()==8){
                        placemark.color="#"+placemark.color.substring(0,2)+placemark.color.substring(6,8)+placemark.color.substring(4,6)+
                        placemark.color.substring(2,4);
                    }
                    colorList.add(placemark.color);
                    Log.d("mmm",placemark.color);
                }
                }
    }

}
