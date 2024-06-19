package servent.message;

import cloud.CloudFile;
import cloud.Friend;

import java.util.Map;
import java.util.Set;

public class WelcomeMessage extends BasicMessage {

	private static final long serialVersionUID = -8981406250652693908L;

	private Map<Integer, CloudFile> files;
	private Map<Integer, CloudFile> backupFiles;
	
	public WelcomeMessage(int senderPort, int receiverPort, Map<Integer, CloudFile> files, Map<Integer, CloudFile> backupFiles) {
		super(MessageType.WELCOME, senderPort, receiverPort);
		
		this.files = files;
		this.backupFiles = backupFiles;
	}
	
	public Map<Integer, CloudFile> getFiles() {
		return files;
	}

	public Map<Integer, CloudFile> getBackupFiles() {
		return backupFiles;
	}
}
