package ed.inf.adbs.lightdb;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Scan Operator, used for doing full table scans
 */
public class ScanOperator extends Operator {

    ArrayList<Tuple> table; //store table as ArrayList of Tuples
    int counter; // keeps count of which tuple from the ArrayList is the scan process at

    /**
     * Constructor
     * @param tableName string of the name of the table as it appears in the CSV file
     */
    public ScanOperator(String tableName) throws IOException {

        table = new ArrayList<Tuple>(); // Create an ArrayList object

        BufferedReader csvReader = new BufferedReader(new FileReader(LightDB.databaseDir + "/data/" + tableName+ ".csv"));
        String row;
        while ((row = csvReader.readLine()) != null) {
            table.add(new Tuple(row));
        }
        csvReader.close();

        counter = 0;
    }

    /**
     * Gets the next tuple in the table
     * @return Tuple
     */
    public Tuple getNextTuple() {

        if (counter == table.size()) { // if we reach end of table return null
            reset();
            return null;
        }

        Tuple nextTuple = table.get(counter);
        counter = counter + 1;
        return nextTuple;
    }

    /**
     * Reset counter, so the table scan starts again from the beginning of the table
     */
    public void reset() {
        counter = 0;
    }
}