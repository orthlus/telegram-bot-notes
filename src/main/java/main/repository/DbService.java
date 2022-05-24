package main.repository;

import lombok.extern.slf4j.Slf4j;
import main.dto.DatePair;
import main.dto.HistoryNote;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.CheckForNull;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;

@Slf4j
@Component
public class DbService {

	@Autowired
	private DbConnectionManager dbConnectionManager;

	public boolean existsHistory(long userId, LocalDateTime startDate, LocalDateTime endDate) {
		String query = """
				SELECT FROM users
				WHERE user_id = %d
				AND notes_start >= '%s' AND notes_end <= '%s'
				OR notes_start >= '%s' AND notes_end <= '%s';"""
				.formatted(userId, startDate, endDate, endDate, startDate);
		ResultSet resultSet = requestDBWithResponse(query);
		try {
			return resultSet.next();
		} catch (SQLException e) {
			log.error("Ошибка получения данных из БД", e);
		}
		return false;
	}

	public void deleteHistory(long userId) {
		String query = "DELETE FROM notes WHERE user_id = %d;".formatted(userId);
		requestDB(query);
		query = "DELETE FROM users WHERE user_id = %d;".formatted(userId);
		requestDB(query);
	}

	public List<HistoryNote> getHistory(long userId, LocalDateTime startDate, LocalDateTime endDate) {
		String query = """
					SELECT timestamp::timestamp(0)::text, note FROM notes
					WHERE user_id = %d
						AND timestamp >= '%s'
						AND timestamp <= '%s'
					ORDER BY 1;"""
				.formatted(userId, startDate, endDate);
		List<HistoryNote> result = new LinkedList<>();
		try {
			ResultSet resultSet = requestDBWithResponse(query);
			while (resultSet.next()) {
				result.add(new HistoryNote(resultSet.getString(1), resultSet.getString(2)));
			}
		} catch (SQLException e) {
			log.error("Ошибка получения данных из БД", e);
		}
		return result;
	}

	public void addNewNote(long userId, String note) {
		String query = "INSERT INTO notes (user_id, note) VALUES (%d, '%s');".formatted(userId, note);
		requestDB(query);
	}

	@CheckForNull
	public DatePair getDatesByUser(long userId) {
		String query = "SELECT notes_start, notes_end FROM users WHERE user_id = %d;".formatted(userId);
		ResultSet resultSet = requestDBWithResponse(query);
		try {
			if (resultSet.next()) {
				return new DatePair(
						resultSet.getTimestamp(1).toLocalDateTime(),
						resultSet.getTimestamp(2).toLocalDateTime()
				);
			}
		} catch (SQLException e) {
			log.error("Ошибка получения данных из БД", e);
		}
		return null;
	}

	private ResultSet requestDBWithResponse(String query) {
		try {
			return dbConnectionManager.getConnection().createStatement().executeQuery(query);
		} catch (SQLException e) {
			log.error("Ошибка выполнения запроса: QUERY_START\n{}\nQUERY_END", query, e);
			throw new RuntimeException(e);
		}
	}

	private void requestDB(String query) {
		try {
			dbConnectionManager.getConnection().createStatement().execute(query);
		} catch (SQLException e) {
			log.error("Ошибка выполнения запроса: QUERY_START\n{}\nQUERY_END", query, e);
			throw new RuntimeException(e);
		}
	}
}
