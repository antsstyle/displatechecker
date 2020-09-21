/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package antsstyle.displatechecker.enumerations;

/**
 *
 * @author Ant
 */
public enum DBTable {

    DISPLATE("displate"),

    DISPLATEFILEINFO("displatefileinfo"),

    SETTINGS("settings");

    private String tableName;

    private DBTable(String tableName) {
        this.tableName = tableName;
    }

    /**
     *
     * @return The name in the database of this DBTable.
     */
    public String getTableName() {
        return tableName;
    }

}
