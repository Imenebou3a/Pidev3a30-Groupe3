package tn.esprit.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyDataBase {

    private static final String URL      = "jdbc:mysql://localhost:3306/pidev";
    private static final String USER     = "root";
    private static final String PASSWORD = "";

    private static MyDataBase instance;
    private Connection myConnection;

    // Constructeur privé — connexion établie ici
    private MyDataBase() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            myConnection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Connexion a la base de donnees reussie !");
        } catch (ClassNotFoundException e) {
            System.err.println("Driver MySQL introuvable : " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("Erreur de connexion : " + e.getMessage());
        }
    }

    // Singleton
    public static MyDataBase getInstance() {
        if (instance == null) {
            instance = new MyDataBase();
        }
        return instance;
    }

    // Retourne la connexion active
    public static Connection getConnection() {
        return getInstance().myConnection;
    }

    // Alias pour compatibilité avec certains services
    public Connection getMyConnection() {
        return myConnection;
    }

    public static void closeConnection() {
        if (instance != null && instance.myConnection != null) {
            try {
                instance.myConnection.close();
                instance = null;
                System.out.println("Connexion fermee.");
            } catch (SQLException e) {
                System.err.println("Erreur fermeture : " + e.getMessage());
            }
        }
    }
}