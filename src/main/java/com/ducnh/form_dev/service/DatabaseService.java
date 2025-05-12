package com.ducnh.form_dev.service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.springframework.orm.jpa.EntityManagerFactoryInfo;
import org.springframework.stereotype.Service;


import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Service
public class DatabaseService {
    @PersistenceContext
    private EntityManager entityManager;

    private Connection getConnection() throws SQLException {
        EntityManagerFactoryInfo info = (EntityManagerFactoryInfo) entityManager.getEntityManagerFactory();
        Connection connection = info.getDataSource().getConnection();
        return connection;
    }

    public String getTenGVFromMaGV(String maGV) {
        try (Connection conn = getConnection();
            Statement stmt = conn.createStatement();) {
            String sql = "if exists (select 1 from vhrdmgv where ma_gv = '" + maGV + "')" //
                    + "select top 1 ma_gv, ten_gv from vhrdmgv where ma_gv = '" + maGV + "'";
        
            boolean results = stmt.execute(sql);
            int rsCount = 0;
            
            // Loop through the available result sets.
            do {
                if (results) {
                    ResultSet rs = stmt.getResultSet();
                    rsCount ++;

                    // Show data from the result set
                    System.out.println("RESULT SET #" + rsCount);
                    while (rs.next()) {
                        String ten_gv = rs.getString("ten_gv");
                        return ten_gv;
                    }
                }
                System.out.println();
                results = stmt.getMoreResults();
            } while (results);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
