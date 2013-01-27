package com.suryagaddipati.jenkins;

import hudson.Extension;
import hudson.model.Build;
import hudson.model.AbstractProject;
import hudson.model.Executor;
import hudson.model.Node;
import hudson.model.Queue.BuildableItem;
import hudson.model.queue.CauseOfBlockage;
import hudson.model.queue.QueueTaskDispatcher;
@Extension
public class SlaveUtilizationQueueTaskDispatcher extends QueueTaskDispatcher {
	@Override
	public CauseOfBlockage canTake(Node node, BuildableItem item) {
		if (item.task instanceof AbstractProject) {
			AbstractProject project = (AbstractProject) item.task;

			SlaveUtilizationProperty property = (SlaveUtilizationProperty) project
					.getProperty(SlaveUtilizationProperty.class);

			if (property != null) {
				boolean needsExlusiveAcess = property
						.isNeedsExclusiveAccessToNode();
				if (needsExlusiveAcess) {
					// Dont run if anyother job is running
					for (Executor executor : node.toComputer().getExecutors()) {
						if (executor.getCurrentExecutable() != null)
							return new CauseOfBlockage.BecauseNodeIsBusy(node);
					}
				} else {
					// Dont run if exclusive access job is running
					for (Executor executor : node.toComputer().getExecutors()) {
						if (currentlyRunningExclusiveAccessProject(executor))return new CauseOfBlockage.BecauseNodeIsBusy(node);	
						
					}
				}

			}
		}

		return super.canTake(node, item);
	}

	private boolean currentlyRunningExclusiveAccessProject(Executor executor) {
		if (executor.getCurrentExecutable() != null
				&& executor.getCurrentExecutable() instanceof Build) {
			Build currentlyRunningBuild = (Build) executor
					.getCurrentExecutable();

			SlaveUtilizationProperty currentProjectproperty = (SlaveUtilizationProperty) currentlyRunningBuild.getProject()
					.getProperty(SlaveUtilizationProperty.class);
			boolean needsExclusiveAccess = currentProjectproperty != null
					&& currentProjectproperty
							.isNeedsExclusiveAccessToNode();
			return needsExclusiveAccess;
			
		}
		return false;
		
	}
}
