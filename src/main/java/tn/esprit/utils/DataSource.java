package tn.esprit.utils;

import java.sql.Connection;

/**
 * Alias pour MyDataBase pour compatibilité
 */
public class DataSource {
    
    private static DataSource instance;
    
    private DataSource() {}
    
    public static DataSource getInstance() {
        if (instance == null) {
            instance = new DataSource();
        }
        return instance;
    }
    
    public Connection getConnection() {
        return MyDataBase.getInstance().getConnection();
    }
}
