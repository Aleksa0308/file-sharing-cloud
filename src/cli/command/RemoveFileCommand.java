package cli.command;

import app.AppConfig;
import app.ChordState;
import app.ServentInfo;
import servent.message.Message;
import servent.message.RemoveFileMessage;
import servent.message.util.MessageUtil;

public class RemoveFileCommand implements CLICommand{
    @Override
    public String commandName() {
        return "remove_file";
    }

    @Override
    public void execute(String args) {
        String filePath = AppConfig.CLOUD_PATH + args;
        int fileKey = ChordState.hashFileName(filePath);

        if(AppConfig.chordState.isKeyMine(fileKey)){
            AppConfig.chordState.removeFile(filePath);
            return;
        }else{
            AppConfig.timestampedStandardPrint("File is not on this node.");
            ServentInfo nextNode = AppConfig.chordState.getNextNodeForKey(fileKey);
            AppConfig.timestampedStandardPrint("Sending REMOVE_FILE message to " + nextNode.getListenerPort());

            Message removeFileMessage = new RemoveFileMessage(AppConfig.myServentInfo.getListenerPort(), nextNode.getListenerPort(), args);
            MessageUtil.sendMessage(removeFileMessage);
        }
    }
}
