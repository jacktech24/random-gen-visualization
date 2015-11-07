package graphics;

import app.Log;
import app.UltimateSorter;
import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Filip Prochazka (jacktech24)
 */
public class TerminalDrawer {

    private static final ArrayList<HelpItem<Character, String>> tutorial = new ArrayList<>();

    static {
        tutorial.add(new HelpItem<>('n', "next chart"));
        tutorial.add(new HelpItem<>('b', "prev chart"));
        tutorial.add(new HelpItem<>('s', "toggle sorted"));
        tutorial.add(new HelpItem<>('a', "start animation"));
        tutorial.add(new HelpItem<>('p', "pick algorithm"));
    }

    private int page = 0;

    private ArrayList<Chart> charts = new ArrayList<>();

    private Terminal terminal;
    private TerminalScreen screen;
    private TextGraphics graphics;

    private boolean started = false;
    private boolean sorted = false;
    private boolean animating = false;

    private UltimateSorter.SortType sortingAlgorithmType = UltimateSorter.SortType.BUBBLE;
    private UltimateSorter.SortingAlgorithm algorithm;

    private TerminalPosition graphStart;
    private TerminalPosition graphEnd;

    public TerminalDrawer(){
        initTerminal();
    }

    private void initTerminal() {
        try {
            DefaultTerminalFactory fac = new DefaultTerminalFactory()
                    .setSwingTerminalFrameTitle("Series Renderer v0.0.1 - by Filip Prochazka (A15B0549P)");
            fac.setInitialTerminalSize(new TerminalSize(120, 35));
            terminal = fac.createTerminal();
            screen = new TerminalScreen(terminal);
            graphics = screen.newTextGraphics();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start() throws IOException {
        if(!started) {
            screen.startScreen();
            screen.setCursorPosition(null);
            terminal.setCursorVisible(false);
            screen.clear();

            graphStart = new TerminalPosition(2, 4);
            graphEnd = new TerminalPosition(terminal.getTerminalSize().getColumns()-2, terminal.getTerminalSize().getRows()-2);

            started = true;
        } else {
            throw new RuntimeException("You have already started Terminal screen");
        }
    }

    public void init(List<List<Integer>> listSeries) {
        if(!started) {
            throw new RuntimeException("You must start Terminal screen first");
        }

        for(List<Integer> serie: listSeries) {
            charts.add(new Chart(serie, graphStart, graphEnd));
        }

        setupControls();
        render();
    }

    private void render() {
        try {
            graphics.fillRectangle(graphStart, new TerminalSize(graphEnd.getColumn()-graphStart.getColumn(),
                    graphEnd.getRow()-graphStart.getRow()), ' ');

            int[] array;
            if (sorted && !animating) {
                array = UltimateSorter.sortList(charts.get(page).getBars(), sortingAlgorithmType);
            } else if (animating) {
                array = algorithm.getArray();
            } else {
                array = UltimateSorter.listToArray(charts.get(page).getBars());
            }
            charts.get(page).render(graphics, array);

            renderStatusLine();

            screen.refresh();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void renderStatusLine() {
        try {
            graphics.setForegroundColor(new TextColor.RGB(255, 255, 255));
            int rowNumber = terminal.getTerminalSize().getRows()-1;
            int colNumber = terminal.getTerminalSize().getColumns()-1;
            int column = 1;
            int row = 1;
            graphics.setCharacter(0, 0, '┏');
            graphics.setCharacter(0, 1, '┃');
            graphics.setCharacter(0, 2, '┣');
            for (int i = 3;i < rowNumber;i++) {
                graphics.setCharacter(0, i, '┃');
                graphics.setCharacter(colNumber, i, '┃');
            }
            graphics.setCharacter(0, rowNumber, '┗');
            graphics.setCharacter(colNumber, 0, '┓');
            graphics.setCharacter(colNumber, 1, '┃');
            graphics.setCharacter(colNumber, 2, '┫');
            graphics.setCharacter(colNumber, rowNumber, '┛');
            graphics.drawLine(1, rowNumber, colNumber-1, rowNumber, '━');

            for (int i = 0;i < tutorial.size();i++) {
                StringBuilder help = new StringBuilder()
                        .append(" ").append(tutorial.get(i).o1).append(" = ")
                        .append(tutorial.get(i).o2).append(" ").append("┃");
                graphics.putString(column, row, help.toString());
                int targetColumn = column+(help.length()-1);
                graphics.drawLine(column, row+1, targetColumn, row+1, '━');
                graphics.drawLine(column, row-1, targetColumn, row-1, '━');
                column += help.length()-1;
                graphics.setCharacter(column, row+1, '┻');
                graphics.setCharacter(column, row-1, '┳');
                column += 1;
            }
            if(column < colNumber-1) {
                graphics.drawLine(column, 0, colNumber-1, 0, '━');
                graphics.drawLine(column, 2, colNumber-1, 2, '━');
            }
            graphics.putString(1, rowNumber, " chart " + (page+1) + " of " + charts.size() + " ");
            String sortName = sortingAlgorithmType.name()+" SORT";
            graphics.putString(colNumber-sortName.length()-2, rowNumber, " "+sortName+" ");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setupControls() {
        new Thread(controlsThread).start();
    }

    private Runnable controlsThread = new Runnable() {
        @Override
        public void run() {
            boolean running = true;
            while (running) {
                try {
                    KeyStroke stroke = screen.readInput();
                    Log.info(stroke.toString());
                    switch (stroke.getKeyType()) {
                        case Escape:
                            running = false;
                            break;
                        case Character:
                            Character ch = Character.toLowerCase(stroke.getCharacter());
                            switch (ch) {
                                case 'n':
                                    nextPage();
                                    break;
                                case 'b':
                                    prevPage();
                                    break;
                                case 's':
                                    toggleSorted();
                                    break;
                                case 'a':
                                    startSortingAnimation();
                                    break;
                                case 'p':
                                    nextAlgorithm();
                                    break;
                            }
                            break;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            System.exit(0);
        }
    };

    private void nextAlgorithm() {
        if (!animating) {
            UltimateSorter.SortType[] values = UltimateSorter.SortType.values();
            sortingAlgorithmType = values[(sortingAlgorithmType.ordinal()+1)%values.length];
            render();
        }
    }

    private Runnable animationThread = new Runnable() {
        @Override
        public void run() {
            algorithm = UltimateSorter.listSorter(charts.get(page).getBars(), sortingAlgorithmType);
            do {
                algorithm.step();
                render();
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    break;
                }
            } while (!algorithm.sorted());
            animating = false;
        }
    };

    private void toggleSorted() {
        if (!animating) {
            sorted = !sorted;
            render();
        }
    }

    private void startSortingAnimation() {
        animating = true;
        new Thread(animationThread).start();
    }

    private void nextPage() {
        if (!animating) {
            if (++page >= charts.size()) {
                page = 0;
            }
            render();
        }
    }

    private void prevPage() {
        if (!animating) {
            if (--page < 0) {
                page = charts.size() - 1;
            }
            render();
        }
    }

    public void stop() throws IOException {
        screen.stopScreen();
    }

    private static class HelpItem<K,V> {

        public final K o1;
        public final V o2;

        public HelpItem(K o1, V o2) {
            this.o1 = o1;
            this.o2 = o2;
        }

        @Override
        public String toString() {
            return super.toString();
        }
    }

    /*public void test() {
        try {
            graphics.drawRectangle(
                    new TerminalPosition(3, 3), new TerminalSize(10, 10), '█');
            screen.refresh();

            screen.readInput();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/

}