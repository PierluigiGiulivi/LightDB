package ed.inf.adbs.lightdb;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.util.TablesNamesFinder;

/**
 * Lightweight in-memory database system
 */
public class LightDB {

	static String databaseDir;
	static String outputFile;
	static HashMap<String, String> aliases = new HashMap<String, String>();

	public static void main(String[] args) {

		if (args.length != 3) {
			System.err.println("Usage: LightDB database_dir input_file output_file");
			return;
		}

		databaseDir = args[0];
		String inputFile = args[1];
		outputFile = args[2];

		parsing(inputFile);
	}

	/**
	 * Parses the given query and returns the answer to the query
	 * @param filename the name in string format of the query file
	 */
	public static void parsing(String filename) {

		try {
			// Statement statement = CCJSqlParserUtil.parse("SELECT * FROM Boats");
			Statement statement = CCJSqlParserUtil.parse(new FileReader(filename));

			if (statement != null) {

				Select select = (Select) statement;
				PlainSelect plain = (PlainSelect) select.getSelectBody();
				TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();

				// Create a query Plan and executes it to get answer to query
				QueryPlan queryPlan = new QueryPlan(plain, tablesNamesFinder.getTableList(select));
				queryPlan.plan();
			}

		} catch (Exception e) {
			System.err.println("Exception occurred during parsing");
			e.printStackTrace();
		}
	}
}