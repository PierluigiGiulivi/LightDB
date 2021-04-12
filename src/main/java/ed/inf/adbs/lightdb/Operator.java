package ed.inf.adbs.lightdb;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

/**
 * Abstract class that represents the following operators: ScanOperator, SelectOperator, ProjectOperator,
 * JoinOperator, SortOperator, and DuplicateEliminationOperator.
 */
public abstract class Operator {

    /**
     * @return next tuple or NULL when no more tuples available
     */
    abstract public Tuple getNextTuple();

    /**
     * tells the operator to reset its state and start returning its output again from the beginning
     * i.e. reset getNextTuple to start returning tuples from the first tuple
     */
    abstract public void reset();

    /**
     * Repeatedly calls getNextTuple() until the next tuple is null and prints it in a .cvs file
     */
    public void dump() {

        PrintWriter pw = null;
        try {
            pw = new PrintWriter(new File(LightDB.outputFile));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Tuple tuple = getNextTuple();
        while (tuple != null) {
            if (tuple != null) {

                pw.write(Arrays.toString(tuple.getTuple()).replaceAll("\\[|\\]|\\s", ""));
                pw.write("\n");
                tuple = getNextTuple();
            }
        }

        pw.close();
    }
}