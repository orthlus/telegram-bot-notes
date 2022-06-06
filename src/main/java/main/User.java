package main;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.file.Path;
import java.util.*;

@Slf4j
public class User {
	@Getter
	private final long id;
	@Setter
	private UserWaitInputStateEnum waitInput = UserWaitInputStateEnum.WAIT_NEW_NOTE;
//	@Getter
//	private DatePair datePair;
	private Map<UUID, Boolean> sentFiles;
	private Set<UUID> sentFilesSet;

	public User(long id) {
		this.id = id;
	}

	public void addFile(Path file) {
		if (sentFilesSet == null) {
			sentFilesSet = new HashSet<>();
		}
		sentFilesSet.add(UUID.fromString(file.getFileName().toString()));
	}

	public void deleteFile(File file) {
		UUID uuid = UUID.fromString(file.getName());
		if (!file.delete()) {
			log.warn("Файл {} не был удалён", uuid);
		}
		sentFiles.remove(uuid);
	}
	@Deprecated
	public void markFileSent(File file) {
		sentFiles.put(UUID.fromString(file.getName()), true);
	}

	@Deprecated
	public List<Path> getSentFiles() {
		List<Path> result = new LinkedList<>();
		for (Map.Entry<UUID, Boolean> entry : sentFiles.entrySet()) {
			if (entry.getValue())
				result.add(Path.of("files-to-send/" + entry.getKey()));
		}
		return result;
	}

	@Deprecated
	public void markFilesDeleted(List<Path> files) {
		List<UUID> uuids = files.stream().map(file -> UUID.fromString(file.getFileName().toString())).toList();
		Set<UUID> keySet = sentFiles.keySet();
		uuids.forEach(keySet::remove);
	}

//	public void clearDates() {
//		datePair = null;
//	}

	public boolean equalsState(UserWaitInputStateEnum state) {
		return waitInput.equals(state);
	}

	public enum UserWaitInputStateEnum {
		SELECTS_PERIOD_OF_HISTORY,
		WAIT_PERIOD_TYPE,
		WAIT_NEW_NOTE,
		WAIT_DOCUMENT_TYPE,
		WAIT_CONFIRM_TO_DELETE_HISTORY
	}
}
