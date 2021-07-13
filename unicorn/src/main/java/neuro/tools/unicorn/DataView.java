package neuro.tools.unicorn;

import gtec.java.unicorn.Unicorn;
import neuro.tools.unicorn.GenericFunctions;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

public class DataView {
    private float[][] dataV;
    GenericFunctions genFunc = new GenericFunctions();
    public void View(DataPoint[] dataP, GraphView graph) {
        graph.removeAllSeries();
        LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>(dataP);
        graph.addSeries(series);
    }
}
