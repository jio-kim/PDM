package com.kgm.soa.bop.util;

import com.teamcenter.soa.client.Connection;
import com.teamcenter.soa.client.model.strong.BOMLine;
import com.teamcenter.soa.client.model.strong.MECollaborationContext;
import com.teamcenter.soa.client.model.strong.MEPlantContext;
import com.teamcenter.soa.client.model.strong.MEProcessContext;
import com.teamcenter.soa.client.model.strong.MEProductContext;
import com.teamcenter.soa.client.model.strong.Mfg0BvrProcess;
import com.teamcenter.soa.client.model.strong.Mfg0BvrWorkarea;

public class MPPTopLines {
	
	private Connection connection;
	private MECollaborationContext meCollaborationContext;

	public BOMLine productLine = null;
	public Mfg0BvrProcess processLine = null;
	public Mfg0BvrWorkarea plantLine = null;
	
	private MEProductContext productContext = null;
	private MEProcessContext processContext = null;
	private MEPlantContext plantContext = null;
	
	public MECollaborationContext getMeCollaborationContext() {
		return meCollaborationContext;
	}

	public void setMeCollaborationContext(
			MECollaborationContext meCollaborationContext) {
		this.meCollaborationContext = meCollaborationContext;
	}
	
	public MPPTopLines(Connection connection, BOMLine productLine, Mfg0BvrProcess processLine, Mfg0BvrWorkarea plantLine){
		this.productLine = productLine;
		this.processLine = processLine;
		this.plantLine = plantLine;
	}
	
	public MEProductContext getProductContext() {
		return this.productContext;
	}

	public void setProductContext(MEProductContext productContext) {
		this.productContext = productContext;
	}

	public MEProcessContext getProcessContext() {
		return this.processContext;
	}

	public void setProcessContext(MEProcessContext processContext) {
		this.processContext = processContext;
	}

	public MEPlantContext getPlantContext() {
		return this.plantContext;
	}

	public void setPlantContext(MEPlantContext plantContext) {
		this.plantContext = plantContext;
	}
	
}
