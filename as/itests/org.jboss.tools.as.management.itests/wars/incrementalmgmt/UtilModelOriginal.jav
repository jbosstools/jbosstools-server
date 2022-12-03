package util.pak;

public class UtilModel {
    private static int count = 0;
    private int i = count++;

    public String toString() {
        return "Util:" + this.i;
    }
}

