package ed.inf.adbs.lightdb;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Used to sort the output of query
 */
public class SortOperator extends Operator {

    String expressionOrder;
    Operator operator;
    ArrayList<Tuple> table; //store table as ArrayList of Tuples
    int counter; // keeps count of which tuple from the ArrayList is the scan process at
    DatabaseCatalog catalog;
    {
        try {
            catalog = new DatabaseCatalog();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Constructor
     * @param operator we get all the tuples from operator in table and sort table
     * @param expressionOrder expression that dictates the order
     */
    public SortOperator(Operator operator, String expressionOrder) throws IOException {

        this.expressionOrder = expressionOrder;
        this.operator = operator;
        table = new ArrayList<Tuple>(); // Create an ArrayList object
        int index = catalog.getAttributeIndex(expressionOrder); // get attribute index in order expression

        Tuple tuple = operator.getNextTuple();

        while (tuple != null) { // store all the tuples in table
            table.add(tuple);
            tuple = operator.getNextTuple();
        }

        Collections.sort(table,new Comparator<Tuple>(){ // sort table

            public int compare(Tuple tuple1, Tuple tuple2){
                return tuple1.getTuple()[index] - tuple2.getTuple()[index];
            }});
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