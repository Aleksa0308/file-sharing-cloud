package cli.command;

import app.AppConfig;
import app.ChordState;
import app.ServentInfo;
import servent.message.FriendRequestMessage;
import servent.message.Message;
import servent.message.util.MessageUtil;

public class AddFriendCommand implements CLICommand{

    @Override
    public String commandName() {
        return "add_friend";
    }

    @Override
    public void execute(String args) {

        try {
            String[] nodeUrlParts = args.split(":");
            String nodeIp = nodeUrlParts[0];
            int nodePort = Integer.parseInt(nodeUrlParts[1]);
            // find the closest node to the given node
            int chordId = ChordState.chordHash(nodePort);
            ServentInfo nextNode = AppConfig.chordState.getNextNodeForKey(chordId);

            // send friend request message
            AppConfig.timestampedStandardPrint("Sending friend request to " + nodePort + "...");
            Message friendRequestMessage = new FriendRequestMessage(AppConfig.myServentInfo.getListenerPort(), nextNode.getListenerPort(), args);
            MessageUtil.sendMessage(friendRequestMessage);
        }catch (Exception e){
            System.out.println("Error: " + e.getMessage());
        }
    }
}
