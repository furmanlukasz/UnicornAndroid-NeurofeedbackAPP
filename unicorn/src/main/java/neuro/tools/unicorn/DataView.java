package neuro.tools.unicorn;

import gtec.java.unicorn.Unicorn;
import neuro.tools.unicorn.GenericFunctions;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

public class DataView {
    private float[][] dataV;

    GenericFunctions genFunc = new GenericFunctions();


    public void View(float[][] dataV, GraphView _graph, int electrode_idx){
        this.dataV = dataV;
        int di[] = new int[2];
        di = genFunc.getDim(dataV);

        GraphView graph = (GraphView) _graph;
        DataPoint[] dataPoints = new DataPoint[di[1]];
        for (int i = 0; i < di[1]; i++){
            dataPoints[i] = new DataPoint(i, dataV[electrode_idx][i]);
        }
        //graph.removeAllSeries();
        //LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>(dataPoints);
        //graph.addSeries(series);
    }
}
