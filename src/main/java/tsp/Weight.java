package TSP;

public class Weight{
    private Integer[][] values;

    public Weight(int size) {
        this.values = new Integer[size][size];
    }

    public Weight(Integer[][] values) {
        this.values = values;
    }

    public int getSize() {
        return values.length;
    }

    public int getWeight(int i, int j) {
        return values[i][j];
    }

    public void setWeight(int i, int j, int value) {
        values[i][j] = value;
    }

    public void multiplyByM(int m){
        for (int i = 0; i < values.length; i++) {
            for (int j = 0; j < values[i].length; j++) {
                values[i][j] *= m;
            }
        }
    }
    public void addExtraToPos(int extra, int[] pos){
        values[pos[0]][pos[1]] += extra;
    }

    public void subtractExtraFromPos(int extra, int[] pos){
        values[pos[0]][pos[1]] -= extra;
    }

    public void addExtraToAll(int extra){
        for (int i = 0; i < values.length; i++) {
            for (int j = 0; j < values[i].length; j++) {
                if (i != j){
                    values[i][j] += extra;
                }
            }
        }
    }

    public void subtractExtraFromAll(int extra){
        for (int i = 0; i < values.length; i++) {
            for (int j = 0; j < values[i].length; j++) {
                if (i != j){
                    values[i][j] -= extra;
                }
            }
        }
    }
}
