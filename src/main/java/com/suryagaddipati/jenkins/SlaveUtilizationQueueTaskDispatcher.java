package com.suryagaddipati.jenkins;

import hudson.Extension;
import hudson.matrix.MatrixConfiguration;
import hudson.model.Build;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Executor;
import hudson.model.Node;
import hudson.model.Queue.BuildableItem;
import hudson.model.Queue.Executable;
import hudson.model.queue.CauseOfBlockage;
import hudson.model.queue.QueueTaskDispatcher;

@Extension
@SuppressWarnings("rawtypes")
public class SlaveUtilizationQueueTaskDispatcher extends QueueTaskDispatcher {
	@Override
	public CauseOfBlockage canTake(Node node, BuildableItem item) {
		if (item.task instanceof AbstractProject) {
			AbstractProject project = (AbstractProject) item.task;
			if(isRestrictrictedToRunOnlyOneInstancePerNode(project,node) || !isRequestedCapacityAvailable(node,project)) 
				return new CauseOfBlockage.BecauseNodeIsBusy(node);
		}

		return super.canTake(node, item);
	}
	@SuppressWarnings("unchecked")
	private boolean isRestrictrictedToRunOnlyOneInstancePerNode(
			AbstractProject project, Node node) {		
		SlaveUtilizationProperty property = (SlaveUtilizationProperty) project
				.getProperty(SlaveUtilizationProperty.class);
		if(property  != null && property.isSingleInstancePerSlave() ) {
			for (Executor executor : node.toComputer().getExecutors()) {
				if (executor.getCurrentExecutable() != null
						&& executor.getCurrentExecutable() instanceof AbstractBuild) {
					AbstractBuild currentlyRunningBuild = (AbstractBuild) executor
							.getCurrentExecutable();
					if ( currentlyRunningBuild.getProject().getName().equals(project.getName())) return true;
				}
			}
		}
		return false;
	}

	private boolean isRequestedCapacityAvailable(Node node, AbstractProject project) {
		float requestedPercentage = getProjectNodeSlice( project, node);
		return availableCapacity(node) >= requestedPercentage;
	}

	private float availableCapacity(Node node) {
		return 100 - currentlyRunningJobCapacity(node);
	}

	private float currentlyRunningJobCapacity(Node node) {
		float currentRunningCapacity = 0;

		for (Executor executor : node.toComputer().getExecutors()) {
			
			Executable currentExecutable = executor.getCurrentExecutable();
			if (currentExecutable != null && currentExecutable instanceof Build)
				currentRunningCapacity += getProjectNodeSlice(((Build)currentExecutable).getProject(), node);
		}
		return currentRunningCapacity;
	}

	
	@SuppressWarnings("unchecked")
	private float getProjectNodeSlice(AbstractProject executable, Node node) {
		AbstractProject project = executable instanceof MatrixConfiguration?  ((MatrixConfiguration)executable).getParent(): executable;
	
		SlaveUtilizationProperty property = (SlaveUtilizationProperty) project.getProperty(SlaveUtilizationProperty.class);
		return property != null && property.isNeedsExclusiveAccessToNode() ? property
				.getSalveUtilizationPercentage()
				: nodeUtilizationPerNotGreedyJob(node);
	}

	private float nodeUtilizationPerNotGreedyJob(Node node) {
		return node.toComputer().getNumExecutors() > 0 ? 100 / node.toComputer().getNumExecutors() : 0;
	}


}
