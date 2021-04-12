package ed.inf.adbs.lightdb;

import net.sf.jsqlparser.expression.Expression;

import java.io.IOException;

/**
 * Join Operator, used for Joining two tables according to an expression
 */
public class JoinOperator extends Operator {

    Expression expressionJoin; // the expression that defines the join
    Operator operatorLeft; // operator that will give tuples for the table on the left of the join expression
    Operator operatorRight; // operator that will give tuples for the table on the right of the join expression
    ExpressionTester tester = new ExpressionTester(); // used for deciding if tuples satisfy expressionJoin
    Tuple tupleLeft;
    Tuple tupleRight;

    /**
     * Constructor
     * @param operatorLeft operator of left side of the join
     * @param operatorRight operator of right side of the join
     * @param expressionJoin expression that defines the join condition
     */
    public JoinOperator(Operator operatorLeft, Operator operatorRight, Expression expressionJoin) throws IOException {

        this.expressionJoin = expressionJoin;

        this.operatorLeft = operatorLeft;
        tupleLeft = operatorLeft.getNextTuple(); // gets first tuple from Operator of left table

        this.operatorRight = operatorRight;
        tupleRight = operatorRight.getNextTuple(); // gets first tuple from Operator of right table
    }

    /**
     * @return next tuple with only the desired attributes in the specified order, when reaches end of table returns null
     */
    public Tuple getNextTuple() {

        while (tupleLeft != null) { // while not end of table

            while (tupleRight != null) { // while not end of table

                if (tester.test(expressionJoin, tupleLeft, tupleRight)) { // if tuple satisfies expression returns tuple

                    Tuple tuple = new Tuple(tupleLeft.getString() + "," + tupleRight.getString()); // create new combine tuple
                    tupleRight = operatorRight.getNextTuple(); // update right tuple
                    return tuple;
                }

                tupleRight = operatorRight.getNextTuple(); // update right tuple
            }

            operatorRight.reset();
            tupleRight = operatorRight.getNextTuple(); // gets first tuple from Operator of right table
            tupleLeft = operatorLeft.getNextTuple(); // update left tuple
        }

        return null; // returns null when end of left table
    }

    /**
     * Reset so the table scan starts again from the beginning of the table
     */
    public void reset() {

        operatorLeft.reset();
        tupleLeft = operatorLeft.getNextTuple(); // gets first tuple from Operator of left table

        operatorRight.reset();
        tupleRight = operatorRight.getNextTuple(); // gets first tuple from Operator of right table
    }
}