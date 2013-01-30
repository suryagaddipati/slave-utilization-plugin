package com.suryagaddipati.jenkins;

import hudson.model.Action;
import hudson.model.FreeStyleProject;
import hudson.model.Queue;
import hudson.model.Queue.WaitingItem;
import hudson.model.queue.CauseOfBlockage;
import hudson.slaves.DumbSlave;

import java.util.ArrayList;

import org.jvnet.hudson.test.HudsonTestCase;

public class SlaveUtilizationQueueTaskDispatcherTest extends HudsonTestCase {
	public void testAllowsJobSchedulingIfCapacityRequirementIsMet() throws Exception {
		SlaveUtilizationQueueTaskDispatcher taskDispatcher = new SlaveUtilizationQueueTaskDispatcher();
		
		DumbSlave node = createSlave();
		
		 FreeStyleProject thirtyPercentJob = createFreeStyleProject();
		 thirtyPercentJob.addProperty(new SlaveUtilizationProperty(true, 30,false));
		 WaitingItem scheduledJob = new Queue.WaitingItem(null, thirtyPercentJob, new ArrayList<Action>());
		
		CauseOfBlockage causeOfBlockage = taskDispatcher.canTake(node, new Queue.BuildableItem(scheduledJob));
		assertNull(causeOfBlockage);
	}
	
	

}
