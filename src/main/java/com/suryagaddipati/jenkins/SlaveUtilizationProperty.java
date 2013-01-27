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
	private final int salveUtilizationPercentage;

	public SlaveUtilizationProperty(boolean needsExclusiveAccessToNode, int salveUtilizationPercentage) {
		this.needsExclusiveAccessToNode = needsExclusiveAccessToNode;
		this.salveUtilizationPercentage = salveUtilizationPercentage;
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
	         int requestedSalveUtilizationPercentage = needsExclusiveAccessToNode? Integer.parseInt( ((Map<String,String>)formData.get("needsExclusiveAccessToNode")).get("salveUtilizationPercentage")): -1;
			 return new SlaveUtilizationProperty(needsExclusiveAccessToNode,requestedSalveUtilizationPercentage);

	        }
	}

	public boolean isNeedsExclusiveAccessToNode() {
		return needsExclusiveAccessToNode;
	}

	public int getSalveUtilizationPercentage() {
		return salveUtilizationPercentage;
	}
}
