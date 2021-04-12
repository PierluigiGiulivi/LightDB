package ed.inf.adbs.lightdb;

/**
 *Tuple Class. Each instance of the class is a tuple in a table.
 */
public class Tuple {

    private int[] tuple; // we assume all the inputs in tables are integers
    String row; // string representation of tuple

    /**
     * Creates a tuple
     * @param row a string of the row of the table
     */
    public Tuple (String row) {

        this.row = row; // store string representation of tuple
        tuple = new int[row.split(",").length]; // create array of correct size

        for(int i = 0;i < row.split(",").length;i++) {
            tuple[i] = Integer.parseInt(row.split(",")[i]); // add integers of string to tuple array
        }
    }

    /**
     * @return tuple
     */
    public int[] getTuple() {
        return tuple;
    }

    /**
     * @return string representation of tuple
     */
    public String getString() {

        return row;
    }
}