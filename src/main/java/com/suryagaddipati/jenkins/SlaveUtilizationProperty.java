package com.suryagaddipati.jenkins;

import hudson.Extension;
import hudson.model.JobProperty;
import hudson.model.JobPropertyDescriptor;
import hudson.model.Job;

import java.util.Map;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.StaplerRequest;

public class SlaveUtilizationProperty extends JobProperty<Job<?, ?>> {
	private final boolean needsExclusiveAccessToNode;
	private final boolean singleInstancePerSlave;
	private final int slaveUtilizationPercentage;

	public SlaveUtilizationProperty(boolean needsExclusiveAccessToNode, int slaveUtilizationPercentage, boolean singleInstancePerSlave) {
		this.needsExclusiveAccessToNode = needsExclusiveAccessToNode;
		this.slaveUtilizationPercentage = slaveUtilizationPercentage;
		this.singleInstancePerSlave =  singleInstancePerSlave;
	}

	@Extension
    public static final class ExclusiveAccessPropertyDescriptor extends JobPropertyDescriptor {

		@Override
		public String getDisplayName() {
			return "Needs exclusive access to Node" ;
		}
		 @Override
	        public boolean isApplicable(Class<? extends Job> jobType) {
	            return true;
	        }
		 @Override
	        public SlaveUtilizationProperty newInstance(StaplerRequest req, JSONObject formData) throws FormException {
			 boolean needsExclusiveAccessToNode = formData.containsKey("needsExclusiveAccessToNode");
			 boolean singleInstancePerSlave = formData.containsKey("singleInstancePerSlave");
	         int requestedSlaveUtilizationPercentage = needsExclusiveAccessToNode? Integer.parseInt( ((Map<String,String>)formData.get("needsExclusiveAccessToNode")).get("slaveUtilizationPercentage")): 0;
			 return new SlaveUtilizationProperty(needsExclusiveAccessToNode,requestedSlaveUtilizationPercentage,singleInstancePerSlave);

	        }
	}

	public boolean isNeedsExclusiveAccessToNode() {
		return needsExclusiveAccessToNode;
	}

	public int getSlaveUtilizationPercentage() {
		return slaveUtilizationPercentage;
	}

	public boolean isSingleInstancePerSlave() {
		return singleInstancePerSlave;
	}
}
