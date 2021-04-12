package ed.inf.adbs.lightdb;

import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.ComparisonOperator;
import net.sf.jsqlparser.util.TablesNamesFinder;

import java.util.HashMap;
import java.util.List;

/**
 * Used for decomposing and analysing an expression to identify join operations or selection operations
 */
public class ExpressionAnalyzer extends ExpressionVisitorAdapter{

    List<String> joinTableList; // list of tables to be joined
    Expression expression; // expression containing the joining conditions and select conditions
    ExpressionTester tester = new ExpressionTester(); // used for deciding if tuples satisfy expressionJoin
    Boolean falseWhere = false; // if we can tell the where clause is false then no need to do full analysis
    TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
    HashMap<String, Expression> selectExpressions = new HashMap<String, Expression>();

    /**
     * In the hashmap Analyzer decomposes the WHERE expression to indicate which conjunctions are for joins or select
     * @param joinTableList list of tables
     * @param expression WHERE clause
     * @return HashMap<String, Expression>
     */
    public HashMap<String, Expression> analyzer(List<String> joinTableList, Expression expression) {

        this.joinTableList = joinTableList; // save list of tables to be joined
        this.expression = expression; // save expression to be split

        selectExpressions.clear();

        for (int i = 0; i < joinTableList.size(); i++) {
            selectExpressions.put(joinTableList.get(i), null); // add each table name with null expression
        }
        for (Integer i = 0; i < joinTableList.size()-1; i++) {
            // add each join with null expression. If we have 5 tables we have 4 joins so we add 0,1,2,3
            selectExpressions.put(i.toString(), null);
        }

        expression.accept(this); // parse the expression decomposing it for analysis

        if (falseWhere) {
           return null; // this is for the case when we have (int op int) and it is false so we can skip the rest
        }

        return selectExpressions;
    }

    /**
     * Only receives expressions of the from (table/int op table/int) for any operation symbol
     * @param expression the expression that we are going to decompose by cases
     */
    @Override
    protected void visitBinaryExpression(BinaryExpression expression) {

        if (expression instanceof ComparisonOperator) {

            List<String> tableList = tablesNamesFinder.getTableList(expression); // get table names in expression, we can have up tp two

            // Case (int op int)
            if (tableList.isEmpty()) { // No tables so it must be (int op int)

                if (!(tester.test(expression, null, null))) { // test if (int op int) is true or false
                    // as we assume only conjunctions, if it is false then we can stop, all WHERE is false hence we return an empty table
                    falseWhere = true;
                }
            }

            // Case (tableA op int) or (int op tableA) or (tableA op tableA)
            if (tableList.size() == 1 || (tableList.size() == 2 && tableList.get(0) == tableList.get(1))) {

                if (selectExpressions.get(tableList.get(0)) == null) { // we do not have any other expression for this table

                    selectExpressions.remove(tableList.get(0));
                    selectExpressions.put(tableList.get(0), expression);
                }

                // we have expressions for the table so we add new one by joining with AND
                AndExpression andExpression  = new  AndExpression(selectExpressions.get(tableList.get(0)), expression);
                selectExpressions.remove(tableList.get(0));
                selectExpressions.put(tableList.get(0), andExpression);
            }

            // Case (tableA op tableB) or (tableB op tableA)
            if (tableList.size() == 2 && (tableList.get(0) != tableList.get(1))) {

                for (Integer i = 0; i < joinTableList.size()-1; i++) { // check each table tha could be joined

                    if (joinTableList.get(i).equals(tableList.get(0)) || joinTableList.get(i).equals(tableList.get(1))) {
                        if (joinTableList.get(i+1).equals(tableList.get(0)) || joinTableList.get(i+1).equals(tableList.get(1))) {

                            if (selectExpressions.get(i.toString()) == null) { // we do not have any other expression for this join

                                selectExpressions.remove(i.toString());
                                selectExpressions.put(i.toString(), expression);
                            }

                            // we have expressions for the join so we add new one by joining with AND
                            AndExpression andExpression = new AndExpression(selectExpressions.get(i.toString()), expression);
                            selectExpressions.remove(i.toString());
                            selectExpressions.put(i.toString(), andExpression);
                        }
                    }
                }
            }
        }
        super.visitBinaryExpression(expression);
    }

    /**
     * Only receives expressions of the from (exp AND exp)
     * @param expression the expression that we are going to decompose by cases
     */
    @Override
    public void visit(AndExpression expression) {

        expression.getLeftExpression().accept(this); // take left side of AND and analyze it
        expression.getRightExpression().accept(this); // take right side of AND and analyze it
        }
}