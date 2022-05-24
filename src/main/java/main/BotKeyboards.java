package main;

import lombok.Getter;
import main.dto.DatePair;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Stream;

@Component
public class BotKeyboards {
	private final ZoneId zoneId = ZoneId.of("Europe/Moscow");

	@Getter
	private final ReplyKeyboardMarkup listPeriodsKeyboard;
	@Getter
	private final List<String> listPeriodsKeyboardValues;

	@Getter
	private final ReplyKeyboardMarkup confirmDeleteHistoryKeyboard;

	public BotKeyboards() {
		listPeriodsKeyboard = ReplyKeyboardMarkup.builder()
				.resizeKeyboard(true)
				.keyboard(List.of(
						row("За неделю"),
						row("За месяц"),
						row("За всё время")
				))
				.build();
		listPeriodsKeyboardValues = List.of("За неделю", "За месяц", "За всё время");
		confirmDeleteHistoryKeyboard = ReplyKeyboardMarkup.builder()
				.resizeKeyboard(true)
				.keyboard(List.of(row("Да", "Нет")))
				.build();
	}

	public DatePair getDatePairByText(String text) {
		DatePair datePair;
		switch (text) {
			case "За неделю" -> datePair = new DatePair(now().truncatedTo(ChronoUnit.DAYS).minusWeeks(1), now());
			case "За месяц" -> datePair = new DatePair(now().truncatedTo(ChronoUnit.DAYS).minusMonths(1), now());
			case "За всё время" -> datePair = null;
			default -> throw new IllegalStateException("Unexpected value: " + text);
		}
		return datePair;
	}

	private LocalDateTime now() {
		return LocalDateTime.now(zoneId);
	}

	private KeyboardRow row(String... s) {
		return new KeyboardRow(Stream.of(s).map(KeyboardButton::new).toList());
	}
}
