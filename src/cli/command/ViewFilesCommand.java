package cli.command;

import app.AppConfig;
import app.ChordState;
import app.ServentInfo;
import servent.message.Message;
import servent.message.ViewFilesMessage;
import servent.message.util.MessageUtil;

public class ViewFilesCommand implements CLICommand{

        @Override
        public String commandName() {
            return "view_files";
        }

        @Override
        public void execute(String args) {
            if (args == null) {
                AppConfig.timestampedErrorPrint("View files command requires a address.");
                return;
            }
            String[] nodeUrlParts = args.split(":");
            String nodeIp = nodeUrlParts[0];
            int nodePort = Integer.parseInt(nodeUrlParts[1]);

            int chordId = ChordState.getChordIdFromMessage(args);
            ServentInfo nextNode = AppConfig.chordState.getNextNodeForKey(chordId);
            AppConfig.timestampedStandardPrint("Sending view files request to " + nodePort + "...");

            Message viewFilesMessage = new ViewFilesMessage(AppConfig.myServentInfo.getListenerPort(), nextNode.getListenerPort(), args);
            MessageUtil.sendMessage(viewFilesMessage);
        }
}
