package main.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
@Setter
public class DatePair {
	private LocalDateTime start;
	private LocalDateTime end;

	@Override
	public String toString() {
		return start.toLocalDate() + " - " + end.toLocalDate();
	}
}
