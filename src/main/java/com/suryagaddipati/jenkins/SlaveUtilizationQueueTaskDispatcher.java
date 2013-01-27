package com.suryagaddipati.jenkins;

import hudson.Extension;
import hudson.matrix.MatrixConfiguration;
import hudson.model.Build;
import hudson.model.AbstractProject;
import hudson.model.Executor;
import hudson.model.Node;
import hudson.model.Queue.BuildableItem;
import hudson.model.Queue.Executable;
import hudson.model.queue.CauseOfBlockage;
import hudson.model.queue.QueueTaskDispatcher;

@Extension
public class SlaveUtilizationQueueTaskDispatcher extends QueueTaskDispatcher {
	@Override
	public CauseOfBlockage canTake(Node node, BuildableItem item) {
		if (item.task instanceof AbstractProject) {

			float requestedPercentage = getProjectNodeSlice((AbstractProject) item.task, node);
			float currentRunningCapacity = 0;

			for (Executor executor : node.toComputer().getExecutors()) {
				
				Executable currentExecutable = executor.getCurrentExecutable();
				if (currentExecutable != null && currentExecutable instanceof Build)
					currentRunningCapacity += getProjectNodeSlice(((Build)currentExecutable).getProject(), node);
			}
			float availableCapacity = 100 - currentRunningCapacity;
            if(requestedPercentage > availableCapacity) return new CauseOfBlockage.BecauseNodeIsBusy(node);
		}

		return super.canTake(node, item);
	}

	private float getProjectNodeSlice(AbstractProject executable, Node node) {
		AbstractProject project = executable instanceof MatrixConfiguration?  ((MatrixConfiguration)executable).getParent(): executable;
		
		SlaveUtilizationProperty property = (SlaveUtilizationProperty) project
				.getProperty(SlaveUtilizationProperty.class);
		return property != null && property.isNeedsExclusiveAccessToNode() ? property
				.getSalveUtilizationPercentage()
				: nodeUtilizationPerNotGreedyJob(node);
	}

	private float nodeUtilizationPerNotGreedyJob(Node node) {
		return node.toComputer().getNumExecutors() > 0 ? 100 / node
				.toComputer().getNumExecutors() : 0;
	}

	private boolean currentlyRunningExclusiveAccessProject(Executor executor) {
		if (executor.getCurrentExecutable() != null
				&& executor.getCurrentExecutable() instanceof Build) {
			Build currentlyRunningBuild = (Build) executor
					.getCurrentExecutable();

			SlaveUtilizationProperty currentProjectproperty = (SlaveUtilizationProperty) currentlyRunningBuild
					.getProject().getProperty(SlaveUtilizationProperty.class);
			boolean needsExclusiveAccess = currentProjectproperty != null
					&& currentProjectproperty.isNeedsExclusiveAccessToNode();
			return needsExclusiveAccess;

		}
		return false;

	}
}
