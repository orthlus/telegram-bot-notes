package main;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Slf4j
public class DeleteFilesJob {
	@Autowired
	private UserManager userManager;

	public void deleteSentFiles() {
		log.info("Start DeleteFilesJob.deleteSentFiles");
		List<User> users = userManager.getUsers().values().stream().toList();
		for (User user : users) {
			List<Path> sentFiles = user.getSentFiles();
			for (Path sentFile : sentFiles) {
				try {
					Files.deleteIfExists(sentFile);
				} catch (IOException e) {
					log.error("Ошибка при удалении файла {} юзера {}", sentFile, user.getId(), e);
					sentFiles.remove(sentFile);
				}
			}
			user.markFilesDeleted(sentFiles);
		}
		log.info("Finish DeleteFilesJob.deleteSentFiles");
	}
}
