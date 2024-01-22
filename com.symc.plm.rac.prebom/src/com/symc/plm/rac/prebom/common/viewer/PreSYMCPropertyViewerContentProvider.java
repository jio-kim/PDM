package com.symc.plm.rac.prebom.common.viewer;

import org.eclipse.jface.viewers.IContentProvider;

import org.eclipse.jface.viewers.Viewer;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCProperty;
import com.teamcenter.rac.kernel.TCPropertyDescriptor;
import com.teamcenter.rac.util.AdapterUtil;

/**
 * 
 * Viewer Custom
 * 
 */
public class PreSYMCPropertyViewerContentProvider implements IContentProvider {

	private TCComponent targetComp;

	@Override
	public void dispose() {
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		targetComp = (TCComponent) AdapterUtil.getAdapter(newInput, TCComponent.class);
		if (targetComp == null) {
			return;
		}

		try {
			String[] propNames = targetComp.getPropertyNames();
			targetComp.getTCProperties(propNames);
		} catch (TCException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Return the TCProperty
	 * 
	 * @param propName
	 *            The property name
	 * @return TCProperty
	 */
	public TCProperty getTCPropery(String propName) {
		try {
			return targetComp.getTCProperty(propName);
		} catch (TCException e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Return the property¡¯s display name
	 * 
	 * @param propName
	 *            The property name
	 * @return String
	 */
	public String getPropertyDisplayName(String propName) {
		try {
			TCPropertyDescriptor propDesc = targetComp.getTypeComponent().getPropertyDescriptor(propName);
			return propDesc.getDisplayName();
		} catch (TCException e) {
			e.printStackTrace();
		}

		return propName;
	}

	/**
	 * Return the property value
	 * 
	 * @param propName
	 *            The property name
	 * @return Object
	 */
	public Object getPropertyValue(String propName) {
		try {
			TCProperty prop = targetComp.getTCProperty(propName);
			if (prop != null) {
				return prop.getPropertyData();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

}
