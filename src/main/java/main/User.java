package main;

import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.nio.file.Path;
import java.util.*;

public class User {
	@Getter
	private final long id;
	@Setter
	private UserWaitInputStateEnum waitInput = UserWaitInputStateEnum.WAIT_NEW_NOTE;
//	@Getter
//	private DatePair datePair;
	private Map<UUID, Boolean> sentFiles;

	public User(long id) {
		this.id = id;
	}

	public void addFile(Path file) {
		if (sentFiles == null) {
			sentFiles = new HashMap<>();
		}
		sentFiles.put(UUID.fromString(file.getFileName().toString()), false);
	}

	public void markFileSent(File file) {
		sentFiles.put(UUID.fromString(file.getName()), true);
	}

	public List<Path> getSentFiles() {
		List<Path> result = new LinkedList<>();
		for (Map.Entry<UUID, Boolean> entry : sentFiles.entrySet()) {
			if (entry.getValue())
				result.add(Path.of("files-to-send/" + entry.getKey()));
		}
		return result;
	}

	public void markFilesDeleted(List<Path> files) {
		// TODO проверить что это вообще работает
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
