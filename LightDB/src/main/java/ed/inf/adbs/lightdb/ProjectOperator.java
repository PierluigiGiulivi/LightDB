package ed.inf.adbs.lightdb;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.select.SelectItem;

import java.io.IOException;
import java.util.List;

/**
 * Project Operator, used for selecting the attributes to output
 */
public class ProjectOperator extends Operator {

    List<SelectItem> select;
    Operator operator;
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
     * @param select List of attributes to select in the required order
     * @param operator operator to which we apply the projection
     */
    public ProjectOperator(List<SelectItem> select, Operator operator) throws IOException {

            this.select = select;
            this.operator = operator;
    }

    /**
     * @return next tuple with only the desired attributes in the specified order, when reaches end of table returns null
     */
    public Tuple getNextTuple() {

        Tuple tuple = operator.getNextTuple(); // gets next tuple from operator

        if (tuple == null) { // return null when reach end of table
            return null;
        }

        String data = ""; // a new tuple can be created by creating a string like "2,100,34"

        for (int i = 0; i < select.size(); i++) { // for each attribute in select
            int e = catalog.getAttributeIndex(select.get(i).toString().trim()); // get attribute index

            // append to string the value in the selected index
            if (data == "") {
                data = data + tuple.getTuple()[e];
            }
            else { data = data + "," + tuple.getTuple()[e];}
        }

        Tuple nextTuple = new Tuple(data); // create new tuple from string data
        return nextTuple;
    }

    /**
     * Reset so the table scan starts again from the beginning of the table
     */
    public void reset() {

        operator.reset(); // reset Scan Operator
    }
}