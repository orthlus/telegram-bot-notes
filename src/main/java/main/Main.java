package main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Main {
	public static void main(String[] args) {
		SpringApplication.run(Main.class);
		/*TODO
		*  удалять с quartz отправленные файлы (класс DeleteFilesJob)
		*  а может удалять файлы сразу после отправки? они больше не используются
		*  добавить пдф файлы
		*  добавить проверку наличия записей перед выгрузкой с помощью DbService.existsHistory
		*  добавить функции для админа (ADMIN_TELEGRAM_ID) - статистику использования бота
		* */
	}
}
