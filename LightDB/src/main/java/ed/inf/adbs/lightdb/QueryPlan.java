package ed.inf.adbs.lightdb;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.util.TablesNamesFinder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * From a PlainSelect get a query plan and execute it getting the answer to the query
 */
public class QueryPlan {

    PlainSelect plain;
    List<SelectItem> select; // SELECT clause items
    String from; // first table in FROM clause
    Expression where; // WHERE clause
    String orderby = null;
    Distinct distinct;
    List<String> join = new ArrayList<>();; // 2nd, 3rd, etc tables in the FROM clause
    List<String> joinTableList; // list with all the tables to be joined in the order as they appear in the FROM clause
    ExpressionAnalyzer expressionAnalyzer = new ExpressionAnalyzer(); // used to break down the WHERE clause when we have joins
    HashMap<String, Expression> selectExpressions; // used to link a table to the conjunctions where the table appears
    TablesNamesFinder tablesNamesFinder = new TablesNamesFinder(); // used to find the tables in an Expression

    /**
     * Constructor: stores each part of the query
     * @param plain PlainSelect of the query
     */
    public QueryPlan(PlainSelect plain, List<String> joinTableList) throws IOException {

        this.plain = plain;
        this.joinTableList = joinTableList;
        select = plain.getSelectItems();
        where = plain.getWhere();
        LightDB.aliases.clear();
        from = plain.getFromItem().toString();
        this.distinct = plain.getDistinct();
        if ( plain.getOrderByElements() != null) {
            orderby = plain.getOrderByElements().get(0).toString();
        }

        if (plain.getJoins() != null) {
            for (int i = 0; i < plain.getJoins().size(); i++) {
                join.add(plain.getJoins().get(i).toString()); // make the join tables list into strings
            }
        }

        // Identify aliases and store them in LightDB.aliases<String alias, Sting table>
        for (int i = 0; i < joinTableList.size(); i++) {

            if (i == 0) {
                try { // first table is in getFromItem
                    from = plain.getFromItem().toString().split(" ")[0]; // get table name as string
                    LightDB.aliases.put(plain.getFromItem().toString().split(" ")[1], from); // store it
                }
                catch(Exception e) { }
            }
            else {
                try { // the rest of the tables are in getJoins
                    join.remove(plain.getJoins().get(i-1).toString());
                    join.add(plain.getJoins().get(i-1).toString().split(" ")[0]); // get table name as string
                    LightDB.aliases.put(plain.getJoins().get(i-1).toString().split(" ")[1], plain.getJoins().get(i-1).toString().split(" ")[0]); // store it
                }
                catch(Exception e) { }
            }
        }
    }

    /**
     * Depending on the structure of the query it creates a query plan and executes it
     */
    public void plan() throws IOException {

        if (select.get(0).toString() == "*") { // select all attributes

            if (join.isEmpty()) { // No Joins to be done

                if (orderby != null) { // no order by

                    if (distinct != null) { // we have disitnc

                        // if the WHERE expression is null then this will act the same way as a scan operator
                        SelectOperator selectOperator = new SelectOperator(from.toString(), where);
                        SortOperator sortOperator = new SortOperator(selectOperator, orderby);
                        DuplicateEliminationOperator dis = new DuplicateEliminationOperator(sortOperator);
                        dis.dump();
                    }
                    else { //  no distinct
                        // if the WHERE expression is null then this will act the same way as a scan operator
                        SelectOperator selectOperator = new SelectOperator(from.toString(), where);
                        SortOperator sortOperator = new SortOperator(selectOperator, orderby);
                        sortOperator.dump();
                    }
                }
                else { // we have order by
                    // if the WHERE expression is null then this will act the same way as a scan operator
                    SelectOperator selectOperator = new SelectOperator(from.toString(), where);
                    selectOperator.dump();
                }
            }

            else { // We have Joins

                selectExpressions = expressionAnalyzer.analyzer(joinTableList, where); // analyze the WHERE clause
                if (selectExpressions == null) {
                    // if one conjunction is of type (int op int) and evaluates to false
                    // no need to do anything else we return the empty table
                }

                for (Integer i = 0; i < joinTableList.size()-1; i++) { // join expressions stored with keys going from 0 to #ofTables -1

                    Expression joinExpression = selectExpressions.get(i.toString()); // we get the first join expression
                    List<String> tableList = tablesNamesFinder.getTableList(joinExpression); // we get the tables involved in join

                    // create select operator for left table of join and apply the conditions that are relevant
                    SelectOperator operatorLeft = new SelectOperator(tableList.get(0), selectExpressions.get(tableList.get(0)));
                    // create select operator for right table of join and apply the conditions that are relevant
                    SelectOperator operatorRight = new SelectOperator(tableList.get(1), selectExpressions.get(tableList.get(1)));
                    // create the join with the join condition
                    JoinOperator joinOperator = new JoinOperator(operatorLeft, operatorRight, joinExpression);
                    joinOperator.dump();
                }
            }
        }

        if (select.get(0).toString() != "*") { // select some attributes

            if (join.isEmpty()) { // No Joins to be done

                if (orderby != null) { // no order by

                    if (distinct != null) {

                        // if the WHERE expression is null then the condition will be ignore and this will act the same way
                        // as a ProjectOperator with a ScanOperator as a child
                        SelectOperator selectOperator = new SelectOperator(from.toString(), where);
                        SortOperator sortOperator = new SortOperator(selectOperator, orderby);
                        DuplicateEliminationOperator dis = new DuplicateEliminationOperator(sortOperator);
                        ProjectOperator projectOperator = new ProjectOperator(select, dis);
                        projectOperator.dump();
                    }
                    else {
                        // if the WHERE expression is null then the condition will be ignore and this will act the same way
                        // as a ProjectOperator with a ScanOperator as a child
                        SelectOperator selectOperator = new SelectOperator(from.toString(), where);
                        SortOperator sortOperator = new SortOperator(selectOperator, orderby);
                        ProjectOperator projectOperator = new ProjectOperator(select, sortOperator);
                        projectOperator.dump();
                    }
                }
                else { // we have order by
                    // if the WHERE expression is null then the condition will be ignore and this will act the same way
                    // as a ProjectOperator with a ScanOperator as a child
                    SelectOperator selectOperator = new SelectOperator(from.toString(), where);
                    ProjectOperator projectOperator = new ProjectOperator(select, selectOperator);
                    projectOperator.dump();
                }
            }

            else { // We have Joins

                selectExpressions = expressionAnalyzer.analyzer(joinTableList, where); // analyze the WHERE clause
                if (selectExpressions == null) {
                    // if one conjunction is of type (int op int) and evaluates to false
                    // no need to do anything else we return the empty table
                }

                for (Integer i = 0; i < joinTableList.size()-1; i++) { // join expressions stored with keys going from 0 to #ofTables -1

                    Expression joinExpression = selectExpressions.get(i.toString()); // we get the first join expression
                    List<String> tableList = tablesNamesFinder.getTableList(joinExpression); // we get the tables involved in join

                    // create select operator for left table of join and apply the conditions that are relevant
                    SelectOperator operatorLeft = new SelectOperator(tableList.get(0), selectExpressions.get(tableList.get(0)));
                    // create select operator for right table of join and apply the conditions that are relevant
                    SelectOperator operatorRight = new SelectOperator(tableList.get(1), selectExpressions.get(tableList.get(1)));
                    // create the join with the join condition
                    JoinOperator joinOperator = new JoinOperator(operatorLeft, operatorRight, joinExpression);
                    joinOperator.dump();
                }
            }
        }
    }
}