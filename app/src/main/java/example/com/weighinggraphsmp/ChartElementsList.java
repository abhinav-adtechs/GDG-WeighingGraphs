package example.com.weighinggraphsmp;


import android.util.Log;

import com.google.gson.annotations.SerializedName;

import java.util.Comparator;
import java.util.List;

public class ChartElementsList{

    private final int TYPE_HOURS = 75676 ;
    private final int TYPE_DAYS = 75677 ;

    private int sortType = TYPE_DAYS ;

    @SerializedName("data")
    private List<ChartElement> chartElements;

    public List<ChartElement> getChartElements() {
        return chartElements;
    }

    public void setChartElements(List<ChartElement> chartElements) {
        this.chartElements = chartElements;
    }


    public class ChartElement implements Comparable<ChartElement>{


        private static final String TAG = "List Test" ;
        public String temperature;
        public String airQuality;
        public String weight;
        public String date;
        public String time;


        public ChartElement() {
        }


        public String getTemperature() {
            return temperature;
        }

        public void setTemperature(String temperature) {
            this.temperature = temperature;
        }

        public String getAirQuality() {
            return airQuality;
        }

        public void setAirQuality(String airQuality) {
            this.airQuality = airQuality;
        }

        public String getWeight() {
            return weight;
        }

        public void setWeight(String weight) {
            this.weight = weight;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }

        @Override
        public int compareTo(ChartElement another) {
            return getDate().compareTo(another.getDate()) ;
        }

        public void printData() {
            Log.d(TAG, "printData() called with: " + " " +
                    getAirQuality() + " " +
                            getDate() + " " +
                            getWeight() + " " +
                            getTime() );
        }

    }
}