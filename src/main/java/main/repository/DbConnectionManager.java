package main.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Slf4j
@Component
public class DbConnectionManager {
	private Connection connection;
	@Value("${DB_URL}")
	private String url;
	@Value("${DB_USER}")
	private String user;
	@Value("${DB_PASSWORD}")
	private String password;

	Connection getConnection() {
		try {
			synchronized (this) {
				if (connection == null || !connection.isValid(0)) {
					connection = DriverManager.getConnection(url, user, password);
				}
			}
			return connection;
		} catch (SQLException e) {
			log.error("Ошибка подключения к БД user: {} url: {}", user, url, e);
			throw new RuntimeException(e);
		}
	}
}
