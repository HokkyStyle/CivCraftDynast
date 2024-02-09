
package com.dynast.civcraft.threading.tasks;


import org.bukkit.entity.Player;

import com.dynast.civcraft.config.CivSettings;
import com.dynast.civcraft.main.CivGlobal;
import com.dynast.civcraft.main.CivMessage;
import com.dynast.civcraft.questions.QuestionBaseTask;
import com.dynast.civcraft.questions.QuestionResponseInterface;
import com.dynast.civcraft.util.CivColor;

public class PlayerQuestionTask extends QuestionBaseTask implements Runnable {

	Player askedPlayer; /* player who is being asked a question. */
	Player questionPlayer; /* player who has asked the question. */
	String question; /* Question being asked. */
	long timeout; /* Timeout after question expires. */
//	RunnableWithArg finishedTask; /* Task to run when a response has been generated. */
	QuestionResponseInterface finishedFunction;
	
	protected String response = new String(); /* Response to the question. */
	protected Boolean responded = Boolean.FALSE; /*Question was answered. */
	
	public PlayerQuestionTask() {
	}
	
	public PlayerQuestionTask(Player askedplayer, Player questionplayer, String question, long timeout, 
			QuestionResponseInterface finishedFunction) {
		
		this.askedPlayer = askedplayer;
		this.questionPlayer = questionplayer;
		this.question = question;
		this.timeout = timeout;
		this.finishedFunction = finishedFunction;
		
	}
	
	@Override
	public void run() {	
		CivMessage.send(askedPlayer, CivColor.LightGray+CivSettings.localize.localizedString("civleaderQtast_prompt1")+" "+CivColor.LightBlue+questionPlayer.getName());
		CivMessage.send(askedPlayer, CivColor.LightPurple+CivColor.BOLD+question);
		CivMessage.send(askedPlayer, CivColor.LightGray+CivSettings.localize.localizedString("civleaderQtast_prompt2"));
		
		try {
			synchronized(this) {
				this.wait(timeout);
			}
		} catch (InterruptedException e) {
			cleanup();
			return;
		}
		
		if (responded) {
			finishedFunction.processResponse(response);
			cleanup();
			return;
		}
		
		CivMessage.send(askedPlayer, CivColor.LightGray+CivSettings.localize.localizedString("var_PlayerQuestionTask_failedInTime",questionPlayer.getName()));
		CivMessage.send(questionPlayer, CivColor.LightGray+CivSettings.localize.localizedString("var_civQtast_NoResponse",askedPlayer.getName()));
		cleanup();
	}

	public Boolean getResponded() {
		synchronized(responded) {
			return responded;
		}
	}

	public void setResponded(Boolean response) {
		synchronized(this.responded) {
			this.responded = response;
		}
	}

	public String getResponse() {
		synchronized(response) {
			return response;
		}
	}

	public void setResponse(String response) {
		synchronized(this.response) {
			setResponded(true);
			this.response = response;
		}
	}
	
	/* When this task finishes, remove itself from the hashtable. */
	private void cleanup() {
		CivGlobal.removeQuestion(askedPlayer.getName());
	}
	
	
	
}
