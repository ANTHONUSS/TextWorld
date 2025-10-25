package fr.anthonus.dataBase;

import java.util.ArrayList;
import java.util.List;

public class Cell {
    public static final List<Cell> cells = new ArrayList<>();

    public int x;
    public int y;
    public String c;

    public Cell(int x, int y, String c) {
        for(var cell : cells){
            if(cell.x == x && cell.y == y) {
                cell.c = c;
                return;
            }
        }

        this.x = x;
        this.y = y;
        this.c = c;
    }

    public static Cell getCell(int x, int y){
        for(var cell : cells){
            if(cell.x == x && cell.y == y) return cell;
        }
        return null;
    }

    public static List<Cell> getCellsInArea(int x, int y, int w, int h) {
        List<Cell> result = new ArrayList<>();
        for (var cell : cells) {
            if (cell.x >= x && cell.x < x + w && cell.y >= y && cell.y < y + h) {
                result.add(cell);
            }
        }
        return result;
    }
}
