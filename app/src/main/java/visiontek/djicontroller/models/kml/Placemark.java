package visiontek.djicontroller.models.kml;

import java.util.List;

public class Placemark {
/*<Placemark>
		<name>hlj</name>
				<visibility>0</visibility>
				<styleUrl>#khStyle5641131140</styleUrl>
				<LineString>
					<tessellate>1</tessellate>
					<coordinates>
130.76105,48.37353100000001,0 130.76312,48.369892,0 130.76572,48.366257,0 130.76988,48.36105700000002,0 130.7733,48.35906200000001,0 130.7733,48.35906200000001,0 130.77612,48.357418,0  </coordinates>
				</LineString>
    </Placemark>*/
    public String name;
    public String color;//记录一个图形颜色
    public int type;//记录图形类型，是点线或者面，点为1，线为2，面为3
    public List<Coordinate> coordinates;
}