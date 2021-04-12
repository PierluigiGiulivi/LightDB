package ed.inf.adbs.lightdb;

import java.io.*;
import java.util.Arrays;
import java.util.Scanner;
import java.util.HashMap;

/**
 * A Catalog of all the tables with their attributes
 */
public class DatabaseCatalog {

    HashMap<String, String[]> database = new HashMap<String, String[]>(); //HashMap that uses table name as key and a list of attributes

    /**
     * Constructor
     * creates the HashMap that has each table name as key and the corresponding list of attributes
     * this is taken from the schema.txt file
     */
    public DatabaseCatalog() throws IOException {

        try {
            File schema = new File( LightDB.databaseDir + "/schema.txt");
            Scanner scanner = new Scanner(schema);

            while (scanner.hasNextLine()) {
                String[] data = scanner.nextLine().split(" ");
                // First string of each line is the table name and the subsequent strings are the attributes
                database.put(data[0], Arrays.copyOfRange(data, 1, data.length));
            }
            scanner.close();

        } catch (FileNotFoundException e) {
            System.out.println("A schema.txt file was not found");
            e.printStackTrace();
        }
    }

    /**
     * @return the database catalog
     */
    public HashMap<String, String[]> getDatabase() {
        return database;
    }

    /**
     * @param tableWithAttribute a string with the table name and attribute name connected by a dot, e.g. Sailors.A
     * @return index in tuple that corresponds to the attribute name from a specific table
     */
    public Integer getAttributeIndex(String tableWithAttribute) {

        String table = tableWithAttribute.trim().split("\\.")[0];

        if (LightDB.aliases.containsKey(table)) {
            table = LightDB.aliases.get(table);
        }

        String attribute = tableWithAttribute.trim().split("\\.")[1];

        String[] attributeList = database.get(table);

        for (int i = 0; i < attributeList.length; i++) {
            if (attributeList[i].equals(attribute)) {
                return i;
            }
        }
        System.out.println("No such attribute name found");
        return null;
    }

    /**
     * Get the number of attributes or columns in a table
     * @param table string of the taBLE name
     * @return integer equal to the number of columns
     */
    public Integer getTableSize(String table) {

        String[] attributeList = database.get(table);
        return attributeList.length;
    }
}