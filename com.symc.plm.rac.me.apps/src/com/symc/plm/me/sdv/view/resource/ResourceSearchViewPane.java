/**
 * 
 */
package com.symc.plm.me.sdv.view.resource;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;

import com.symc.plm.me.common.SDVBOPUtilities;
import com.symc.plm.me.common.SDVText;
import com.symc.plm.me.sdv.service.resource.ResourceUtilities;
import com.symc.plm.me.sdv.viewpart.resource.ResourceSearchViewPart;
import com.teamcenter.rac.kernel.TCComponentQuery;
import com.teamcenter.rac.kernel.TCComponentQueryType;
import com.teamcenter.rac.kernel.TCQueryClause;
import com.teamcenter.rac.kernel.TCSession;

/**
 * Class Name : Resource_View
 * Class Description :
 * 
 * @date 2013. 10. 24.
 * 
 */
public class ResourceSearchViewPane extends Composite {

    private static TCSession session;
    private HashMap<String, SDVText> conditionTextMap;

    /**
     * Create the composite.
     * 
     * @param parent
     * @param style
     */
    public ResourceSearchViewPane(Composite parent, int style, String resourceType) {
        super(parent, style);
        session = SDVBOPUtilities.getTCSession();

        ArrayList<String> arrSearchKeys = new ArrayList<String>();

        try {
            arrSearchKeys = getSearchKeys(resourceType);
        } catch (Exception e) {
            e.printStackTrace();
        }

        initUI(arrSearchKeys);
    }

    private void initUI(ArrayList<String> arrLabelNames) {
        setLayout(new FillLayout());

        final ScrolledComposite scrolledcomposite = new ScrolledComposite(this, SWT.H_SCROLL | SWT.V_SCROLL | SWT.NONE);
        scrolledcomposite.setExpandHorizontal(true);
        scrolledcomposite.setExpandVertical(true);

        final Composite composite = new Composite(scrolledcomposite, SWT.NONE);
        scrolledcomposite.setContent(composite);

        final GridLayout gridLayout = new GridLayout(6, false);
        gridLayout.marginLeft = 5;
        gridLayout.marginRight = 0;
        gridLayout.marginHeight = 10;
        gridLayout.verticalSpacing = 5;
        gridLayout.horizontalSpacing = 10;

        composite.setLayout(gridLayout);

        conditionTextMap = new HashMap<String, SDVText>();
        for (String labelName : arrLabelNames) {
            Label lblNewLabel = new Label(composite, SWT.NONE);
            lblNewLabel.setText(labelName);

            SDVText text = new SDVText(composite, SWT.BORDER | SWT.SINGLE);
            text.setLayoutData(new GridData(100, 15));
            ResourceUtilities.setTabKeyListener(text);
            setEnterKeyListener(text);

            conditionTextMap.put(labelName, text);

            scrolledcomposite.setMinSize(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
            composite.layout();
        }

        // View크기 변경에 맞춰 검색 필드 재정렬
        // composite.addControlListener(new ControlListener() {
        //
        // @Override
        // public void controlResized(ControlEvent e) {
        // Point viewSize = composite.getSize();
        //
        // if ((viewSize.x) < 500) {
        // gridLayout.numColumns = 2;
        // } else if ((viewSize.x) >= 500 && (viewSize.x) < 800) {
        // gridLayout.numColumns = 4;
        // } else if ((viewSize.x) >= 800) {
        // gridLayout.numColumns = 6;
        // }
        //
        // composite.layout();
        // }
        //
        // @Override
        // public void controlMoved(ControlEvent e) {
        // }
        // });

    }

    /**
     * 이미 만들어져 있는 Saved query를 검색하여 해당 쿼리의 EntryName들을 Return하는 method이다.
     * 
     * @param savedQueryName
     *            String 저장된 query name
     * @return ArrayList<String> EntryName배열
     * @throws Exception
     * 
     */
    public static ArrayList<String> getSearchKeys(String savedQueryName) throws Exception {

        ArrayList<String> arrKeys = new ArrayList<String>();
        TCComponentQueryType queryType = (TCComponentQueryType) session.getTypeComponent("ImanQuery");
        TCComponentQuery query = (TCComponentQuery) queryType.find(savedQueryName);
        TCQueryClause[] queryClauses = query.describe();

        for (TCQueryClause queryClause : queryClauses) {
            arrKeys.add(queryClause.getUserEntryNameDisplay());
            // arrKeys.add(queryClause.getUserEntryName());
        }

        return arrKeys;
    }

    @Override
    protected void checkSubclass() {
        // Disable the check that prevents subclassing of SWT components
    }

    // 검색 조건에 입력된 값을 찾아서 Map에 담는 함수
    public HashMap<String, String> getCondition() {
        HashMap<String, String> searchConditionMap = new HashMap<String, String>();
        for (String key : conditionTextMap.keySet()) {
            // System.out.println("key : " + key + ", Value : " + conditionTextMap.get(key));
            searchConditionMap.put(key, conditionTextMap.get(key).getText());
        }

        return searchConditionMap;
    }

    // 엔터키 입력시 검색
    public static void setEnterKeyListener(SDVText sdvText) {
        sdvText.addKeyListener(new KeyListener() {

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.keyCode == 13 || e.keyCode == 16777296) {
                    ResourceSearchViewPart resourceSearchViewPart = ResourceUtilities.getResourceSearchViewPart();
                    Button searchButton = resourceSearchViewPart.getCurrentSearchButton();
                    searchButton.notifyListeners(SWT.Selection, new Event());
                }
            }

            @Override
            public void keyPressed(KeyEvent e) {

            }
        });
    }
}
