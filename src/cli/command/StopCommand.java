package cli.command;

import app.AppConfig;
import cli.CLIParser;
import failure_detection.HeartbeatMonitor;
import servent.SimpleServentListener;

public class StopCommand implements CLICommand {

	private CLIParser parser;
	private SimpleServentListener listener;
	private HeartbeatMonitor heartbeatMonitor;
	
	public StopCommand(CLIParser parser, SimpleServentListener listener, HeartbeatMonitor heartbeatMonitor) {
		this.parser = parser;
		this.listener = listener;
		this.heartbeatMonitor = heartbeatMonitor;
	}
	
	@Override
	public String commandName() {
		return "stop";
	}

	@Override
	public void execute(String args) {
		AppConfig.timestampedStandardPrint("Stopping...");
		parser.stop();
		listener.stop();
		heartbeatMonitor.stop();
	}

}
