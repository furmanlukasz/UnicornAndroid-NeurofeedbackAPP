package neuro.tools.unicorn;

import com.jjoe64.graphview.series.DataPoint;

public class GenericFunctions {

    private int dimX, dimY, dimZ;
    private float[] arr_1;
    private float[][] arr_2;
    private float[][][] arr_3;

    public int[] getDim(float[][] arr) {
        int[] re = new int[2];
        re[0] = arr.length;
        re[1] = arr[0].length;
        return re;
    }
    /*
    S_cnT = 10
    cnT = 10 -> ostatnia aktualizacja: dataA[9]
    cnT = 11 -> ostatnia aktualizacja: dataA[0] ->
    zaczynamy wczytywanie macierzy od [1]
    */
    public DataPoint[] ToPoints(float[][] arr, int idx, int di1){
        DataPoint[] re = new DataPoint[di1];
        for(int j = 0; j < di1; j++){
            re[j] = new DataPoint((float) j, arr[idx][j]);
        }
        return re;
    }
    public float[][] TransPose(float[][] arr, int ofs, int di0, int di1){
        int ofS = (ofs <= di0) ? 0 : ofs % di0, k;
        float[][] re = new float[di1][di0];
        for(int i = 0; i < di0; i++){
            k = (i + ofS) % di0;
            for(int j = 0; j < di1; j++){
                re[j][i] = arr[k][j];
            }
        }
        return re;
    }
    public void SetZeros(float[][] arr){
        int[] di = new int[2];
        di = getDim(arr);
        for(int i = 0; i < di[0]; i++){
            for(int j = 0; j < di[1]; j++){
                arr[i][j] = 0.0f;
            }
        }
    }
}
