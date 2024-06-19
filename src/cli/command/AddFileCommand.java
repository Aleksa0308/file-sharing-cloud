package cli.command;

import app.AppConfig;
import app.ChordState;
import app.ServentInfo;
import cloud.AccessType;
import cloud.CloudFile;
import cloud.FileUtil;
import servent.message.AddFileMessage;
import servent.message.Message;
import servent.message.util.MessageUtil;

public class AddFileCommand implements CLICommand {

    @Override
    public String commandName() {
        return "add_file";
    }

    @Override
    public void execute(String args) {
        String[] fileInfo = args.split(" ");
        String filePath = AppConfig.CLOUD_PATH + fileInfo[0];
        String accessType = fileInfo[1];

        int fileKey = ChordState.hashFileName(filePath);
        if(AppConfig.chordState.isKeyMine(fileKey)) {
            AppConfig.chordState.addFile(filePath, AccessType.valueOf(accessType.toUpperCase()));
            return;
        }
        ServentInfo nextNode = AppConfig.chordState.getNextNodeForKey(fileKey);
        AppConfig.timestampedStandardPrint("Sending ADD_FILE message to " + nextNode.getListenerPort());

        Message addFileMessage = new AddFileMessage(AppConfig.myServentInfo.getListenerPort(), nextNode.getListenerPort(), args);
        MessageUtil.sendMessage(addFileMessage);
    }
}
