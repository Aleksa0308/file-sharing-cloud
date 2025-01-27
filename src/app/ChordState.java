package app;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import cloud.AccessType;
import cloud.CloudFile;
import cloud.FileUtil;
import cloud.Friend;
import servent.message.*;
import servent.message.util.MessageUtil;

/**
 * This class implements all the logic required for Chord to function.
 * It has a static method <code>chordHash</code> which will calculate our chord ids.
 * It also has a static attribute <code>CHORD_SIZE</code> that tells us what the maximum
 * key is in our system.
 * 
 * Other public attributes and methods:
 * <ul>
 *   <li><code>chordLevel</code> - log_2(CHORD_SIZE) - size of <code>successorTable</code></li>
 *   <li><code>successorTable</code> - a map of shortcuts in the system.</li>
 *   <li><code>predecessorInfo</code> - who is our predecessor.</li>
 *   <li><code>valueMap</code> - DHT values stored on this node.</li>
 *   <li><code>init()</code> - should be invoked when we get the WELCOME message.</li>
 *   <li><code>isCollision(int chordId)</code> - checks if a servent with that Chord ID is already active.</li>
 *   <li><code>isKeyMine(int key)</code> - checks if we have a key locally.</li>
 *   <li><code>getNextNodeForKey(int key)</code> - if next node has this key, then return it, otherwise returns the nearest predecessor for this key from my successor table.</li>
 *   <li><code>addNodes(List<ServentInfo> nodes)</code> - updates the successor table.</li>
 *   <li><code>putValue(int key, int value)</code> - stores the value locally or sends it on further in the system.</li>
 *   <li><code>getValue(int key)</code> - gets the value locally, or sends a message to get it from somewhere else.</li>
 * </ul>
 * @author bmilojkovic
 *
 */
public class ChordState {

	public static int CHORD_SIZE;
	public static int chordHash(int value) {
		return 61 * value % CHORD_SIZE;
	}

	public static int hashFileName(String fileName) {
		return Math.floorMod(fileName.hashCode(), CHORD_SIZE);
	}
	
	private int chordLevel; //log_2(CHORD_SIZE)
	
	private ServentInfo[] successorTable;
	private ServentInfo predecessorInfo;
	
	//we DO NOT use this to send messages, but only to construct the successor table
	private List<ServentInfo> allNodeInfo;
	
	private Map<Integer, Integer> valueMap;
	public static Map<Integer, CloudFile> backupFilesMap;
	public static Map<Integer, CloudFile> filesMap;
	public static Set<Friend> friendSet;

	private long lastPredecessorHeartbeat = -1;
	
	public ChordState() {
		this.chordLevel = 1;
		int tmp = CHORD_SIZE;
		while (tmp != 2) {
			if (tmp % 2 != 0) { //not a power of 2
				throw new NumberFormatException();
			}
			tmp /= 2;
			this.chordLevel++;
		}
		
		successorTable = new ServentInfo[chordLevel];
		for (int i = 0; i < chordLevel; i++) {
			successorTable[i] = null;
		}
		
		predecessorInfo = null;
		valueMap = new HashMap<>();
		filesMap = new HashMap<>();
		backupFilesMap = new HashMap<>();
		friendSet = ConcurrentHashMap.newKeySet();
		allNodeInfo = new ArrayList<>();
	}

	// Getter for lastPredecessorHeartbeat
	public synchronized long getLastPredecessorHeartbeat() {
		return lastPredecessorHeartbeat;
	}

	// Setter for lastPredecessorHeartbeat
	public synchronized void setLastPredecessorHeartbeat(long lastPredecessorHeartbeat) {
		this.lastPredecessorHeartbeat = lastPredecessorHeartbeat;
	}


	/**
	 * This should be called once after we get <code>WELCOME</code> message.
	 * It sets up our initial value map and our first successor so we can send <code>UPDATE</code>.
	 * It also lets bootstrap know that we did not collide.
	 */
	public void init(WelcomeMessage welcomeMsg) {
		//set a temporary pointer to next node, for sending of update message
		successorTable[0] = new ServentInfo("localhost", welcomeMsg.getSenderPort());
		filesMap = welcomeMsg.getFiles();
		backupFilesMap = welcomeMsg.getBackupFiles();

		// Initialize the last heartbeat time for the predecessor if it exists and is not the current node
		if (this.predecessorInfo != null && !this.predecessorInfo.equals(AppConfig.myServentInfo)) {
			this.lastPredecessorHeartbeat = System.currentTimeMillis();
		}

		//tell bootstrap this node is not a collider
		try {
			Socket bsSocket = new Socket("localhost", AppConfig.BOOTSTRAP_PORT);
			
			PrintWriter bsWriter = new PrintWriter(bsSocket.getOutputStream());
			bsWriter.write("New\n" + AppConfig.myServentInfo.getListenerPort() + "\n");
			
			bsWriter.flush();
			bsSocket.close();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public int getChordLevel() {
		return chordLevel;
	}
	
	public ServentInfo[] getSuccessorTable() {
		return successorTable;
	}

	public int getNextNodePort() {
		if (successorTable[0] != null) {
			return successorTable[0].getListenerPort();
		}
		// Return an invalid port number (negative) if no successor is set
		return -1;
	}
	
	public ServentInfo getPredecessor() {
		return predecessorInfo;
	}
	
	public void setPredecessor(ServentInfo newNodeInfo) {
		this.predecessorInfo = newNodeInfo;
	}

	public Map<Integer, CloudFile> getCloudFiles() {
		return filesMap;
	}

	public Set<Friend> getFriends() {
		return friendSet;
	}

	public Map<Integer, Integer> getValueMap() {
		return valueMap;
	}
	
	public void setValueMap(Map<Integer, Integer> valueMap) {
		this.valueMap = valueMap;
	}

	public void setFilesMap(Map<Integer, CloudFile> filesMap) {
		this.filesMap = filesMap;
	}
	
	public boolean isCollision(int chordId) {
		if (chordId == AppConfig.myServentInfo.getChordId()) {
			return true;
		}
		for (ServentInfo serventInfo : allNodeInfo) {
			if (serventInfo.getChordId() == chordId) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Returns true if we are the owner of the specified key.
	 */
	public boolean isKeyMine(int key) {
		if (predecessorInfo == null) {
			return true;
		}
		
		int predecessorChordId = predecessorInfo.getChordId();
		int myChordId = AppConfig.myServentInfo.getChordId();
		
		if (predecessorChordId < myChordId) { //no overflow
			if (key <= myChordId && key > predecessorChordId) {
				return true;
			}
		} else { //overflow
			if (key <= myChordId || key > predecessorChordId) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Main chord operation - find the nearest node to hop to to find a specific key.
	 * We have to take a value that is smaller than required to make sure we don't overshoot.
	 * We can only be certain we have found the required node when it is our first next node.
	 */
	public ServentInfo getNextNodeForKey(int key) {
		if (isKeyMine(key)) {
			return AppConfig.myServentInfo;
		}
		
		//normally we start the search from our first successor
		int startInd = 0;
		
		//if the key is smaller than us, and we are not the owner,
		//then all nodes up to CHORD_SIZE will never be the owner,
		//so we start the search from the first item in our table after CHORD_SIZE
		//we know that such a node must exist, because otherwise we would own this key
		if (key < AppConfig.myServentInfo.getChordId()) {
			int skip = 1;
			while (successorTable[skip].getChordId() > successorTable[startInd].getChordId()) {
				startInd++;
				skip++;
			}
		}
		
		int previousId = successorTable[startInd].getChordId();
		
		for (int i = startInd + 1; i < successorTable.length; i++) {
			if (successorTable[i] == null) {
				AppConfig.timestampedErrorPrint("Couldn't find successor for " + key);
				break;
			}
			
			int successorId = successorTable[i].getChordId();
			
			if (successorId >= key) {
				return successorTable[i-1];
			}
			if (key > previousId && successorId < previousId) { //overflow
				return successorTable[i-1];
			}
			previousId = successorId;
		}
		//if we have only one node in all slots in the table, we might get here
		//then we can return any item
		return successorTable[0];
	}

	public static int getChordIdFromMessage(String message) {
		String[] splitMessage = message.split(":");
		int msgReceiverPort = Integer.parseInt(splitMessage[1]);
        return chordHash(msgReceiverPort);
	}

	private void updateSuccessorTable() {
		//first node after me has to be successorTable[0]
		
		int currentNodeIndex = 0;
		ServentInfo currentNode = allNodeInfo.get(currentNodeIndex);
		successorTable[0] = currentNode;
		
		int currentIncrement = 2;
		
		ServentInfo previousNode = AppConfig.myServentInfo;
		
		//i is successorTable index
		for(int i = 1; i < chordLevel; i++, currentIncrement *= 2) {
			//we are looking for the node that has larger chordId than this
			int currentValue = (AppConfig.myServentInfo.getChordId() + currentIncrement) % CHORD_SIZE;
			
			int currentId = currentNode.getChordId();
			int previousId = previousNode.getChordId();
			
			//this loop needs to skip all nodes that have smaller chordId than currentValue
			while (true) {
				if (currentValue > currentId) {
					//before skipping, check for overflow
					if (currentId > previousId || currentValue < previousId) {
						//try same value with the next node
						previousId = currentId;
						currentNodeIndex = (currentNodeIndex + 1) % allNodeInfo.size();
						currentNode = allNodeInfo.get(currentNodeIndex);
						currentId = currentNode.getChordId();
					} else {
						successorTable[i] = currentNode;
						break;
					}
				} else { //node id is larger
					ServentInfo nextNode = allNodeInfo.get((currentNodeIndex + 1) % allNodeInfo.size());
					int nextNodeId = nextNode.getChordId();
					//check for overflow
					if (nextNodeId < currentId && currentValue <= nextNodeId) {
						//try same value with the next node
						previousId = currentId;
						currentNodeIndex = (currentNodeIndex + 1) % allNodeInfo.size();
						currentNode = allNodeInfo.get(currentNodeIndex);
						currentId = currentNode.getChordId();
					} else {
						successorTable[i] = currentNode;
						break;
					}
				}
			}
		}
		
	}

	public void removeNode(int deadNodePort){
		// if dead node is my predecessor also notify the bootstrap
		if (predecessorInfo.getListenerPort() == deadNodePort) {
			try {
				Socket bsSocket = new Socket("localhost", AppConfig.BOOTSTRAP_PORT);

				PrintWriter bsWriter = new PrintWriter(bsSocket.getOutputStream());
				bsWriter.write("Dead\n" + deadNodePort + "\n");

				bsWriter.flush();
				bsSocket.close();
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		ServentInfo deadNode = null;
		for (ServentInfo serventInfo : allNodeInfo) {
			if (serventInfo.getListenerPort() == deadNodePort) {
				deadNode = serventInfo;
				break;
			}
		}
		if(deadNode != null){
			allNodeInfo.remove(deadNode);
			if(!allNodeInfo.isEmpty()){
				updateSuccessorTable();
			}else{
				// empty the successor table
				for (int i = 0; i < chordLevel; i++) {
					successorTable[i] = null;
				}
				// empty the predecessor
				predecessorInfo = null;
				this.lastPredecessorHeartbeat = -1;
			}
		}


		// set new predecessor
		if (predecessorInfo != null && predecessorInfo.getListenerPort() == deadNodePort && !allNodeInfo.isEmpty()) {
			predecessorInfo = allNodeInfo.get(allNodeInfo.size()-1);
		}

		// update the last heartbeat time for the predecessor if it exists and is not the current node
		if (this.predecessorInfo != null && !this.predecessorInfo.equals(AppConfig.myServentInfo) && !allNodeInfo.isEmpty()) {
			this.lastPredecessorHeartbeat = System.currentTimeMillis();
		}
		AppConfig.timestampedStandardPrint(AppConfig.myServentInfo.getListenerPort() + " NEW NODES: " + allNodeInfo);
	}

	/**
	 * This method constructs an ordered list of all nodes. They are ordered by chordId, starting from this node.
	 * Once the list is created, we invoke <code>updateSuccessorTable()</code> to do the rest of the work.
	 * 
	 */
	public void addNodes(List<ServentInfo> newNodes) {
		allNodeInfo.addAll(newNodes);
		
		allNodeInfo.sort(new Comparator<ServentInfo>() {
			
			@Override
			public int compare(ServentInfo o1, ServentInfo o2) {
				return o1.getChordId() - o2.getChordId();
			}
			
		});
		
		List<ServentInfo> newList = new ArrayList<>();
		List<ServentInfo> newList2 = new ArrayList<>();
		
		int myId = AppConfig.myServentInfo.getChordId();
		for (ServentInfo serventInfo : allNodeInfo) {
			if (serventInfo.getChordId() < myId) {
				newList2.add(serventInfo);
			} else {
				newList.add(serventInfo);
			}
		}
		
		allNodeInfo.clear();
		allNodeInfo.addAll(newList);
		allNodeInfo.addAll(newList2);
		if (newList2.size() > 0) {
			predecessorInfo = newList2.get(newList2.size()-1);
		} else {
			predecessorInfo = newList.get(newList.size()-1);
		}
		
		updateSuccessorTable();
	}

	/**
	 * The Chord put operation. Stores locally if key is ours, otherwise sends it on.
	 */
	public void putValue(int key, int value) {
		if (isKeyMine(key)) {
			valueMap.put(key, value);
		} else {
			ServentInfo nextNode = getNextNodeForKey(key);
			PutMessage pm = new PutMessage(AppConfig.myServentInfo.getListenerPort(), nextNode.getListenerPort(), key, value);
			MessageUtil.sendMessage(pm);
		}
	}
	
	/**
	 * The chord get operation. Gets the value locally if key is ours, otherwise asks someone else to give us the value.
	 * @return <ul>
	 *			<li>The value, if we have it</li>
	 *			<li>-1 if we own the key, but there is nothing there</li>
	 *			<li>-2 if we asked someone else</li>
	 *		   </ul>
	 */
	public int getValue(int key) {
		if (isKeyMine(key)) {
			if (valueMap.containsKey(key)) {
				return valueMap.get(key);
			} else {
				return -1;
			}
		}
		
		ServentInfo nextNode = getNextNodeForKey(key);
		AskGetMessage agm = new AskGetMessage(AppConfig.myServentInfo.getListenerPort(), nextNode.getListenerPort(), String.valueOf(key));
		MessageUtil.sendMessage(agm);
		
		return -2;
	}

	public void addFriend(int port) {
		Friend friend = new Friend("localhost", port);
		friendSet.add(friend);
	}

	public void addFile(String path, AccessType accessType) {
		int fileKey = hashFileName(path);
		CloudFile cloudFile = FileUtil.createFile(path, accessType);
		filesMap.put(fileKey, cloudFile);
		AppConfig.timestampedStandardPrint("Added a new file: " + cloudFile.getFileInfo());
		// send save backup to the successor
		if(successorTable[0] != null){
			Message saveBackupMessage = new SaveBackupMessage(AppConfig.myServentInfo.getListenerPort(), successorTable[0].getListenerPort(), cloudFile);
			MessageUtil.sendMessage(saveBackupMessage);
		}
	}

	public String tellFiles(int port) {
		boolean isFriend = false;
		for (Friend friend : friendSet) {
			if (friend.getPort() == port) {
				isFriend = true;
				break;
			}
		}
		StringBuilder sb = new StringBuilder();
		for (CloudFile file : filesMap.values()) {
			// if the file is not private or the receiver is a friend
			if (!file.getFileType().equals(AccessType.PRIVATE) || isFriend) {
				sb.append(file.getFileInfo());
			}
		}
		return sb.toString();
	}

	public void removeFile (String path) {
		int fileKey = hashFileName(path);
		filesMap.remove(fileKey);
		Message removeBackupMessage = new RemoveBackupMessage(AppConfig.myServentInfo.getListenerPort(), successorTable[0].getListenerPort(), path);
		MessageUtil.sendMessage(removeBackupMessage);
		AppConfig.timestampedStandardPrint("Removed file: " + path);
	}

	public void addBackupFile(String path, CloudFile cloudFile) {
		int fileKey = hashFileName(path);
		backupFilesMap.put(fileKey, cloudFile);
		AppConfig.timestampedStandardPrint("Added a new backup file: " + cloudFile.getFileInfo());
	}

	public void removeBackupFile(String path) {
		int fileKey = hashFileName(path);
		backupFilesMap.remove(fileKey);
		AppConfig.timestampedStandardPrint("Removed backup file: " + path);
	}

	public void transferBackupFiles() {
		// save the files as the same key as it was in the backupFilesMap
		if(backupFilesMap.isEmpty()){
			return;
		}

		for (Map.Entry<Integer, CloudFile> entry : backupFilesMap.entrySet()) {
			filesMap.put(entry.getKey(), entry.getValue());
		}
		backupFilesMap.clear();
	}

	public Map<Integer, CloudFile> getBackupFiles() {
		return backupFilesMap;
	}

	public void setBackupFilesMap(Map<Integer, CloudFile> backupFilesMap) {
		this.backupFilesMap = backupFilesMap;
	}
}
