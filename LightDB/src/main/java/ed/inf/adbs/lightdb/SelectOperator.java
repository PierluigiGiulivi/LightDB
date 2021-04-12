package ed.inf.adbs.lightdb;

import net.sf.jsqlparser.expression.Expression;

import java.io.IOException;

/**
 * Select Operator, used for analysing the WHERE clause in a query
 */
public class SelectOperator extends Operator {

    ExpressionTester tester = new ExpressionTester(); // used for deciding if specific tuple satisfies expression
    Operator operator;
    Expression expression;

    /**
     * Constructor
     * @param tableName string of the name of the table as it appears in the CSV file
     * @param expression expression that we want to evaluate
     */
    public SelectOperator(String tableName, Expression expression) throws IOException {
        operator = new ScanOperator(tableName);
        this.expression = expression;
    }

    /**
     * Gets the next tuple that satisfies the Expression and when reaches the end of table returns null
     * @return Tuple
     */
    public Tuple getNextTuple() {

        Tuple tuple = operator.getNextTuple(); // gets tuple from Scan Operator

        while (tuple != null) {
            if (tester.test(expression, tuple, tuple)) { // if tuple satisfies expression returns tuple
                return tuple;
            }
            tuple = operator.getNextTuple();
        }

        return null; // returns null when end of table
    }

    /**
     * Reset so the table scan starts again from the beginning of the table
     */
    public void reset() {

        operator.reset(); // reset Scan Operator
    }
}