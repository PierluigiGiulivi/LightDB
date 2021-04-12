Readme


How to extract joins from the WHERE clause:

The QueryPlaner class uses ExpressionAnalyzer to break down the WHERE clause into chunks of 
(table/int op table/int) and organise arrange this by category.

ExpressionAnalyzer uses a HashMap<String, Expression> to organise these chunks of the WHERE clause
E.g. of HashMap:

KEY, Expression
Table1, Table1.A < Table1.B
Table2, null
Table3, Table3.F = 100
0, Table1.A < Table2.E
1, Table2.D = Table3.G

With this HashMap we can easily build the query plan. When we have a join we create a join operator
That has 2 Childs. Each child is a selector operator with the corresponding expression for that child  table. If the expression is null then it is a scan operator.

How does ExpressionAnalyzer creates the HashMap?
It divides the WHERE clause in conjunctions. It takes each conjunction and recognises what format it is in.
Cases:

(Int op int) -> checks if it evaluates to true or false. If it is false as we only have conjunctions then the whole WHERE clause is false and hence it tells the QueryPlanner to return an empty table

(Table1/int op Table1/int) -> there is only one table involved so this is a selection conjunction. It adds all the conjunctions involving the same table and stores it in the HashMap

(Table1 op Table2) -> there are two tables involved it must be a join condition. Then it loops over the tables names in the query in the order as they appear in the FROM clause and recognises if it involves the first table or the second or ... . Once it knows it involves a certain table then it adds all the expressions of this type and stores them in the appropriate join number.
e.g. FROM R, S, T and WHERE R.A = S.B AND T.D = S.C
R.A = S.B tells ExpressionAnalyzer that the first table is involved so it puts it with key 0
T.D = S.C tells ExpressionAnalyzer that the smallest index table involved has index 2 so it puts it with key 1. As we have 3 tables we only have 2 joins.
This only works if the join conditions are in this format if we have instead R.A = S.B AND R.C = T.D this will not work we will end up having R.A = S.B AND R.C = T.D in key 0.



Extra Information:


Even if ExpressionAnalyzer is capable of recognising more than one join, QueryPlanner can only process queries with a single join of two tables.


Projection Operator does not work with join, we can only project if we don't have joins


Scan Operator uses the given sample/db path but the actual data tables are inside another directory called data. For LightDB to work we need:
Data tables path: givenPath/data/tableName.csv
Schema path: givenPath/Schema.txt
Given path is given when executing the query put the rest needs to be as showed, if not nothing will work.

Aliases are not implemented for queries with joins. If we don't have joins aliases work greatly.

ORDER BY is not implemented for queries with joins. If we don't have joins ORDER BY works greatly.

Distinct is not implemented for queries with joins. If we don't have joins Disticnt might work.




 