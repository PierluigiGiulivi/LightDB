package ed.inf.adbs.lightdb;

import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.ComparisonOperator;

import java.io.IOException;

/**
 * Used for testing if two tuple satisfy an Expression such as a WHERE clause i.e. Sailors.A = Boats.D
 * If we have Sailors.A < Sailors.B use the same tuple twice
 */
public class ExpressionTester extends ExpressionVisitorAdapter{

    Boolean bool = false;
    Integer intLeft;
    Integer intRight;
    String left;
    String right;
    String operation;
    Tuple tupleLeft;
    Tuple tupleRight;
    DatabaseCatalog catalog;
    {
        try {
            catalog = new DatabaseCatalog();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Check if tuples satisfy an expression
     * @param expression Expression from the WHERE clause
     * @param tupleLeft the tuple from the left side table
     * @param tupleRight the tuple from the right side table
     * @return Boolean
     */
    public Boolean test(Expression expression, Tuple tupleLeft, Tuple tupleRight) {

        if (expression == null) { // this is used for cross product, if no expression we evaluate to true
            return true;
        }

        reset(); // reset stored elements of expression
        this.tupleLeft = tupleLeft;
        this.tupleRight = tupleRight;
        expression.accept(this); // parse the expression decomposing it for analysis
        return bool; // final boolean value of expression
    }

    /**
     * Only receives expressions of the from (table/int op table/int) for any operation symbol
     * @param expression the expression to evaluate according to tupleLeft and tupleRight
     */
    @Override
    protected void visitBinaryExpression(BinaryExpression expression) {

        reset(); // reset stored elements of expression

        if (expression instanceof ComparisonOperator) {
            left = expression.getLeftExpression().toString(); // get left side
            operation = expression.getStringExpression(); // get operation symbol
            right = expression.getRightExpression().toString(); // get right side

            try {
                intLeft = Integer.parseInt(left); // if left side is integer then save value
            }
            catch(Exception e) { }

            try {
                intRight = Integer.parseInt(right); // if right side is integer then save value
            }
            catch(Exception e) { }

            testing(); // analyse and change bool value accordingly
        }
        super.visitBinaryExpression(expression);
    }

    /**
     * Only receives expressions of the from (exp AND exp)
     * @param expression the expression to evaluate according to tupleLeft and tupleRight
     */
    @Override
    public void visit(AndExpression expression) {

        expression.getLeftExpression().accept(this); // take left side of AND and analyze it
        if (bool) { // if left side is TRUE then take right side and analyze it. This is repeated for many AND
            expression.getRightExpression().accept(this);
        }
    }

    /**
     * Tests expression, decides if tuple satisfy it
     */
    public void testing() {

        if (intLeft != null && intRight != null) { // we have: (int op int)
            switch (operation) {
                case "=": bool = intLeft == intRight;
                    break;
                case "!=": bool = intLeft != intRight;
                    break;
                case "<": bool = intLeft < intRight;
                    break;
                case ">": bool = intLeft > intRight;
                    break;
                case "<=": bool = intLeft <= intRight;
                    break;
                case ">=": bool = intLeft >= intRight;
                    break;
            }
        }

        if (intLeft == null && intRight == null) { // we have: (attribute op attribute)
            int indexLeft = catalog.getAttributeIndex(left); // get left attribute index in tupleLeft
            int indexRight = catalog.getAttributeIndex(right); // get right attribute index in tupleRight
            switch (operation) {
                case "=": bool = tupleLeft.getTuple()[indexLeft] == tupleRight.getTuple()[indexRight];
                    break;
                case "!=": bool = tupleLeft.getTuple()[indexLeft] != tupleRight.getTuple()[indexRight];
                    break;
                case "<": bool = tupleLeft.getTuple()[indexLeft] < tupleRight.getTuple()[indexRight];
                    break;
                case ">": bool = tupleLeft.getTuple()[indexLeft] > tupleRight.getTuple()[indexRight];
                    break;
                case "<=": bool = tupleLeft.getTuple()[indexLeft] <= tupleRight.getTuple()[indexRight];
                    break;
                case ">=": bool = tupleLeft.getTuple()[indexLeft] >= tupleRight.getTuple()[indexRight];
                    break;
            }
        }

        if (intLeft != null && intRight == null) { // we have: (int op attribute)
            int indexRight = catalog.getAttributeIndex(right); // get right attribute index in tupleRight
            switch (operation) {
                case "=": bool = intLeft == tupleRight.getTuple()[indexRight];
                    break;
                case "!=": bool = intLeft != tupleRight.getTuple()[indexRight];
                    break;
                case "<": bool = intLeft < tupleRight.getTuple()[indexRight];
                    break;
                case ">": bool = intLeft > tupleRight.getTuple()[indexRight];
                    break;
                case "<=": bool = intLeft <= tupleRight.getTuple()[indexRight];
                    break;
                case ">=": bool = intLeft >= tupleRight.getTuple()[indexRight];
                    break;
            }
        }

        if (intLeft == null && intRight != null) { // we have: (attribute op int)
            int indexLeft = catalog.getAttributeIndex(left); // get left attribute index in tupleLeft
            switch (operation) {
                case "=": bool = tupleLeft.getTuple()[indexLeft] == intRight;
                    break;
                case "!=": bool = tupleLeft.getTuple()[indexLeft] != intRight;
                    break;
                case "<": bool = tupleLeft.getTuple()[indexLeft] < intRight;
                    break;
                case ">": bool = tupleLeft.getTuple()[indexLeft] > intRight;
                    break;
                case "<=": bool = tupleLeft.getTuple()[indexLeft] <= intRight;
                    break;
                case ">=": bool = tupleLeft.getTuple()[indexLeft] >= intRight;
                    break;
            }
        }
    }

    /**
     * resets stored elements of expression
     */
    public void reset() {
        bool = false;
        intLeft = null;
        intRight = null;
        left = "";
        right = "";
        operation = "";
    }
}