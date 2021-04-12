package ed.inf.adbs.lightdb;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class DuplicateEliminationOperator extends Operator {

    Operator operator;
    ArrayList<Tuple> table; //store table as ArrayList of Tuples
    int counter; // keeps count of which tuple from the ArrayList is the scan process at

    /**
     * Constructor
     * @param operator we eliminate duplicated tuples from given operator
     */
    public DuplicateEliminationOperator(Operator operator) throws IOException {

        this.operator = operator;

        table = new ArrayList<Tuple>(); // Create an ArrayList object

        Tuple tuple = operator.getNextTuple();

        while (tuple != null) { // store all the tuples in table
            table.add(tuple);
            tuple = operator.getNextTuple();
        }

        for (int i=0; i < table.size()-1; i++) {

            if (table.get(i) == table.get(i+1)) {
                table.remove(i);
            }
        }
        counter = 0;
    }

    /**
     * @return next tuple with only the desired attributes in the specified order, when reaches end of table returns null
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
     * Reset so the table scan starts again from the beginning of the table
     */
    public void reset() {

        operator.reset();
        counter = 0;
    }
}
