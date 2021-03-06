package graphics;

import app.Utils;
import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;

import java.util.ArrayList;
import java.util.List;

/**
 * A Chart object represents one chart of a series of integers
 * @author Filip Prochazka (jacktech24)
 */
public class Chart {

    private static final int MAX_COLLUMN_WIDTH = 10;
    private static final int MIN_COLLUMN_WIDTH = 1;

    private static final int COLLUMN_SPACE_WIDTH = 1;

    private final TerminalPosition start;
    private final TerminalPosition end;

    private List<Integer> bars = new ArrayList<>();

    private boolean scroolable = false;

    private int barWidth;
    private int maxVal;
    private int barHeight;

    /**
     * @param series The series the chart will represent
     */
    public Chart(List<Integer> series, TerminalPosition start, TerminalPosition end) {
        this.start = start;
        this.end = end;

        bars.addAll(series);
    }

    private Float[] calculate(int[] array) {
        int max = array[0];
        for(int i = 0;i < array.length;i++) {
            if(array[i] > max) {
                max = array[i];
            }
        }
        maxVal = max;

        TerminalSize drawingSpace =
                new TerminalSize(
                        Math.abs(start.getColumn()-end.getColumn()),
                        Math.abs(start.getRow()-end.getRow()));
        barWidth = (int) Math.floor(drawingSpace.getColumns()/(float)array.length);
        barWidth = barWidth < MIN_COLLUMN_WIDTH ? MIN_COLLUMN_WIDTH : barWidth;

        barHeight = drawingSpace.getRows();

        float coeficient = barHeight/(float)maxVal;

        List<Float> cachedBars = new ArrayList<>();
        for (int i = 0;i < array.length;i++) {
            cachedBars.add(recalculateHeight(array[i], coeficient));
        }
        return cachedBars.toArray(new Float[cachedBars.size()]);
    }

    /**
     *
     * @return
     */
    public List<Integer> getBars() {
        return bars;
    }

    private Float recalculateHeight(Integer height, float coeficient) {
        return height * coeficient;
    }

    /**
     *
     * @param graphics
     * @param array
     * @param highlight
     */
    public void render(TextGraphics graphics, int[] array, int[] highlight) {
        int step = (int) Math.ceil((end.getRow()-start.getRow())/10f);
        for (int i = 0;i < 10;i++) {
            graphics.setForegroundColor(new TextColor.RGB(50, 50, 50));
            graphics.drawLine(start.getColumn(), end.getRow()-1-(i*step), end.getColumn(), end.getRow()-1-(i*step), '_');
        }

        Float[] bars = calculate(array);
        for (int i = 0;i < bars.length;i++) {
            int x = start.getColumn()+3+4*i;
            if (highlight != null && Utils.arrayContains(highlight, i)) {
                graphics.setForegroundColor(new TextColor.RGB(255, 0, 0));
            } else {
                graphics.setForegroundColor(new TextColor.RGB(255, 255, 255));
            }
            graphics.fillRectangle(new TerminalPosition(x, end.getRow()-bars[i].intValue()),
                    new TerminalSize(2, bars[i].intValue()), '║');
            graphics.setForegroundColor(new TextColor.RGB(0, 255, 0));
            graphics.putString(x, end.getRow(), String.valueOf(array[i]));
        }
    }

}
