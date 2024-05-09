package com.kgm.common.attachfile;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;

import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.common.TCTypeRenderer;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentForm;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.util.Registry;

/**
 * �� class�� AttachFilePanel�� JList���� ���Ǵ� renderer�̴�.<br>
 * Dataset�� ���� icon�� ���Ͽ� ���� icon�� �߰� ������ �κп� ���� �̹����� ���ǵȴ�.
 */
@SuppressWarnings("rawtypes")
public class AttachFileListCellRenderer extends JPanel implements ListCellRenderer
{
	private static final long serialVersionUID = 1L;

	public AttachFileListCellRenderer()
	{
		setLayout(new FlowLayout(FlowLayout.LEFT, 2, 0));
		setMinimumSize(new Dimension(100, 15));
	}

	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus)
	{
		if(value == null)
		{
			JLabel nameLabel = new JLabel();
			add(nameLabel);
			return this;
		}
		removeAll();
		Registry commonRegistry = Registry.getRegistry("com.teamcenter.rac.common.common");

		AttachFileComponent attachFileComp = (AttachFileComponent)value;
		int state = attachFileComp.getState();
		if(state == AttachFileComponent.ADD)
		{
			JLabel addLabel = new JLabel(commonRegistry.getImageIcon("addButton.ICON"));
			add(addLabel);
		} else if(state == AttachFileComponent.DELETE)
		{
			JLabel removeLabel = new JLabel(commonRegistry.getImageIcon("removeButton.ICON"));
			add(removeLabel);
		} else
		{
			JLabel blankLabel = new JLabel(commonRegistry.getImageIcon("defaultBlank.ICON"));
			add(blankLabel);
		}

		if(attachFileComp.isFileComponent())
		{
			JLabel fileLabel = new JLabel(commonRegistry.getImageIcon("ImanFile.ICON"));
			add(fileLabel);
		} else if(attachFileComp.isAIFComponentContext())
		{
			if(((AIFComponentContext)attachFileComp.getAttachObject()).getComponent() instanceof TCComponentItemRevision){
				JLabel revisionLabel = new JLabel(TCTypeRenderer.getIcon((TCComponentItemRevision)((AIFComponentContext)attachFileComp.getAttachObject()).getComponent()));
				add(revisionLabel);
			}
			else if(((AIFComponentContext)attachFileComp.getAttachObject()).getComponent() instanceof TCComponentDataset){
				JLabel datasetLabel = new JLabel(TCTypeRenderer.getIcon((TCComponentDataset)((AIFComponentContext)attachFileComp.getAttachObject()).getComponent()));
				add(datasetLabel);
			}
			else{
				JLabel itemLabel = new JLabel(TCTypeRenderer.getIcon((TCComponentItem)((AIFComponentContext)attachFileComp.getAttachObject()).getComponent()));
				add(itemLabel);
			}
			
		}
		else if(attachFileComp.isWebLinkComponent()){
			JLabel datasetLabel = new JLabel(TCTypeRenderer.getIcon((TCComponentForm)attachFileComp.getAttachObject()));
			add(datasetLabel);
		}
		JLabel nameLabel = new JLabel(attachFileComp.toString());
		add(nameLabel);

		if(isSelected)
		{
			setBackground(list.getSelectionBackground());
			setForeground(list.getSelectionForeground());
			nameLabel.setBackground(list.getSelectionBackground());
			nameLabel.setForeground(list.getSelectionForeground());
		} else
		{
			setBackground(list.getBackground());
			setForeground(list.getForeground());
			nameLabel.setBackground(list.getBackground());
			nameLabel.setForeground(list.getForeground());
		}

		return this;
	}
}
