package top.bogey.auto_touch.util;

import android.graphics.Point;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DouglasPeucker {
    private static final float epsilon = 10;

    public static List<Point> compress(List<Point> points){
        if (points.size() <= 2) return points;
        float max = 0;
        int index = 0;
        for (int i = 1; i < points.size() - 1; i++) {
            float d = distance(points.get(0), points.get(points.size() - 1), points.get(i));
            if (d > max){
                index = i;
                max = d;
            }
        }

        if (max > epsilon){
            List<Point> list = compress(points.subList(0, index));
            List<Point> list2 = compress(points.subList(index, points.size() - 1));
            list.remove(list.size() - 1);
            list.addAll(list2);
            return list;
        } else {
            return new ArrayList<>(Arrays.asList(points.get(0), points.get(points.size() - 1)));
        }
    }

    private static float distance(Point start, Point end, Point curr){
        float a = end.y - start.y;
        float b = start.x - end.x;
        if (a == 0 && b == 0) return 0;
        float c = end.x * start.y - start.x * end.y;
        return Math.abs(a * curr.x + b * curr.y + c) / (float) Math.sqrt(a * a + b * b);
    }
}
