package main;

import lombok.extern.slf4j.Slf4j;
import main.dto.DatePair;
import main.dto.HistoryNote;
import main.repository.DbService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.function.Supplier;

import static main.User.UserWaitInputStateEnum.*;

@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
@Slf4j
@Component
public class NotesHandlers extends TelegramLongPollingBot {
	private static final ThreadLocal<User> users = new ThreadLocal<>();
	private final Random random = new Random();

	@Value("${BOT_NICKNAME}")
	private String botNickname;
	@Value("${BOT_TOKEN}")
	private String botToken;
	@Autowired
	private DbService db;
	@Autowired
	private UserManager userManager;
	@Autowired
	private BotKeyboards botKeyboards;
	@Autowired
	private InputValidator inputValidator;

	private final Supplier<Boolean> chanceToAnswer = () -> random.nextDouble() < 0.05;

	@Override
	public void onUpdateReceived(Update update) {
		Message updateMessage = update.getMessage();
		users.set(userManager.getUser(updateMessage.getChatId()));
		if (!update.hasMessage() || !updateMessage.hasText()) {
			return;
		}
		String messageText = updateMessage.getText();
		if (!inputValidator.isValid(messageText)) {
			send("Не удалось сохранить :( Сообщение содержит недопустимые слова: " + inputValidator.getStopWords());
			return;
		}
		if (messageText.startsWith("/")) {
			handleCommands(messageText);
		} else {
			handleText(messageText);
		}
	}

	private void handleText(String messageText) {
		User user = users.get();
		if (user.equalsState(WAIT_NEW_NOTE)) {
			saveNote(messageText);
			if (chanceToAnswer.get()) {
				send("Ого, очень интересно! Сохранил");
			}
		} else if (user.equalsState(WAIT_CONFIRM_TO_DELETE_HISTORY)) {
			if (messageText.equals("Да")) {
				db.deleteHistory(userIdLong());
				sendWithoutKeyboard("История удалена");
				user.setWaitInput(WAIT_NEW_NOTE);
			} else if (messageText.equals("Нет")) {
				sendWithoutKeyboard("Ок :)");
				user.setWaitInput(WAIT_NEW_NOTE);
			} else {
				send("Неверная команда :( Выбери, пожалуйста, из списка");
			}
		} else if (user.equalsState(WAIT_PERIOD_TYPE)) {
			if (botKeyboards.getListPeriodsKeyboardValues().contains(messageText)) {
				DatePair datePair = botKeyboards.getDatePairByText(messageText);
				InputFile document;
				if (datePair == null) {
					// выборка за весь период
					document = makeHistoryDocument();
				} else {
					document = makeHistoryDocument(datePair);
				}
				if (document != null) {
					send(document);
				} else {
					sendWithoutKeyboard("Не найдено записей за указанный период");
				}
				user.setWaitInput(WAIT_NEW_NOTE);
			} else {
				send("Неверная команда :( Выбери, пожалуйста, из списка");
			}
		}
	}

	private void handleCommands(String messageText) {
		switch (messageText) {
			case "/start" -> {
				users.get().setWaitInput(WAIT_NEW_NOTE);
				sendWithoutKeyboard("Привет! Мне можно рассказывать, чем ты занимаешься или как у тебя дела. " +
						"А я потом помогу восстановить события во времени.");
			}
			case "/show_history" -> {
				send("Выбери период для восстановления событий", botKeyboards.getListPeriodsKeyboard());
				users.get().setWaitInput(WAIT_PERIOD_TYPE);
			}
			case "/clear" -> {
				send("Точно удалить историю?", botKeyboards.getConfirmDeleteHistoryKeyboard());
				users.get().setWaitInput(WAIT_CONFIRM_TO_DELETE_HISTORY);
			}
			default -> send("Неизвестная команда :( " +
					"Если сообщение начинается на слэш (/), оно расценивается как команда");
		}
	}

	private void send(InputFile inputFile) {
		try {
			SendDocument document = new SendDocument(userId(), inputFile);
			document.setReplyMarkup(new ReplyKeyboardRemove(true));
			execute(document);
			users.get().markFileSent(document.getDocument().getNewMediaFile());
		} catch (TelegramApiException e) {
			log.error("Ошибка отправки сообщения", e);
		}
	}

	private void send(String text) {
		try {
			execute(new SendMessage(userId(), text));
		} catch (TelegramApiException e) {
			log.error("Ошибка отправки сообщения", e);
		}
	}

	private void sendWithoutKeyboard(String text) {
		send(text, new ReplyKeyboardRemove(true));
	}

	private void send(String text, ReplyKeyboard keyboard) {
		try {
			SendMessage message = new SendMessage(userId(), text);
			message.setReplyMarkup(keyboard);
			execute(message);
		} catch (TelegramApiException e) {
			log.error("Ошибка отправки сообщения", e);
		}
	}

	private void saveNote(String text) {
		db.addNewNote(userIdLong(), text);
	}

	private InputFile makeHistoryDocument() {
		return makeHistoryDocument(db.getDatesByUser(userIdLong()));
	}

	private InputFile makeHistoryDocument(DatePair datePair) {
		if (datePair == null) return null;
		String history = selectHistory(datePair);
		if (history == null) return null;
		Path file = makeFile(history);
		users.get().addFile(file);
		return new InputFile(file.toFile(), "%s.txt".formatted(datePair));
	}

	private Path makeFile(String content) {
		try {
			UUID uuid = UUID.randomUUID();
			Path file = Path.of("files-to-send/" + uuid);
			Files.writeString(file, content);
			return file;
		} catch (IOException e) {
			log.error("Ошибка при работе с файлом", e);
			throw new RuntimeException(e);
		}
	}

	private String selectHistory(DatePair datePair) {
		List<HistoryNote> history = db.getHistory(userIdLong(), datePair.getStart(), datePair.getEnd());
		if (history.isEmpty()) {
			return null;
		}
		StringBuilder result = new StringBuilder();
		result.append("time, note\n");
		for (HistoryNote historyNote : history) {
			result.append(historyNote.getTimestamp());
			result.append(", ");
			result.append(historyNote.getNote());
			result.append("\n");
		}
		return result.toString();
	}

	private long userIdLong() {
		return users.get().getId();
	}

	private String userId() {
		return String.valueOf(users.get().getId());
	}

	@Override
	public String getBotUsername() {
		return botNickname;
	}

	@Override
	public String getBotToken() {
		return botToken;
	}
}
