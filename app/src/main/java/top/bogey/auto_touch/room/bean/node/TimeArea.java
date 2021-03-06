package top.bogey.auto_touch.room.bean.node;

public class TimeArea {
    private int min;
    private int max;

    public TimeArea(int time) {
        this(time, time);
    }

    public TimeArea(int min, int max) {
        this.min = min;
        this.max = max;
    }

    public int getRandomTime(){
        return (int) Math.round(Math.random() * Math.abs(max - min) + Math.min(min, max));
    }

    public void setTime(int time){
        min = time;
        max = time;
    }

    public String getTitle(){
        if (Math.min(min, max) == min){
            return min + "-" + max;
        } else {
            return max + "-" + min;
        }
    }

    public int getRealMax(){
        return Math.max(min, max);
    }

    public int getRealMin(){
        return Math.min(min, max);
    }

    public int getMin() {
        return min;
    }

    public void setMin(int min) {
        this.min = min;
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }
}
