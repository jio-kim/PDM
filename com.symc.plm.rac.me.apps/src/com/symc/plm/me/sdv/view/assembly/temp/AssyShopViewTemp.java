package com.symc.plm.me.sdv.view.assembly.temp;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;

public class AssyShopViewTemp extends Composite {
    private Text txtShop;
    private Text txtProduct;
    private Label lblShopKorName;
    private Text txtShopKorName;
    private Label lboShopEngName;
    private Text text;
    private Label lblJph;
    private Composite composite;
    private Text text_1;
    private Label lblAllowance;
    private Text txtAllowance;
    private Label lblVehicleKorName;
    private Text txtVehicleName;
    private Label lblVehicleEngName;
    private Text txtVehicleEngName;

    public AssyShopViewTemp(Composite parent, int style) {
        super(parent, style);
        FillLayout fillLayout = new FillLayout(SWT.HORIZONTAL);
        fillLayout.marginWidth = 10;
        fillLayout.marginHeight = 10;
        setLayout(fillLayout);
        
        Group grpShop = new Group(this, SWT.NONE);
        grpShop.setFont(SWTResourceManager.getFont("¸¼Àº °íµñ", 11, SWT.NORMAL));
        grpShop.setText("Shop Á¤º¸");
        grpShop.setLayout(new FillLayout(SWT.HORIZONTAL));
        
        Composite compositeInform = new Composite(grpShop, SWT.NONE);
        GridLayout gl_compositeInform = new GridLayout(2, false);
        gl_compositeInform.marginLeft = 10;
        compositeInform.setLayout(gl_compositeInform);
        
        Label lblShop = new Label(compositeInform, SWT.NONE);
        lblShop.setFont(SWTResourceManager.getFont("¸¼Àº °íµñ", 11, SWT.NORMAL));
        lblShop.setText("Shop ÄÚµå");
        
        txtShop = new Text(compositeInform, SWT.BORDER);
        GridData gd_txtShop = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_txtShop.widthHint = 100;
        txtShop.setLayoutData(gd_txtShop);
        
        Label lblProduct = new Label(compositeInform, SWT.NONE);
        lblProduct.setText("Product ÄÚµå");
        lblProduct.setFont(SWTResourceManager.getFont("¸¼Àº °íµñ", 11, SWT.NORMAL));
        
        txtProduct = new Text(compositeInform, SWT.BORDER);
        GridData gd_txtProduct = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_txtProduct.widthHint = 100;
        txtProduct.setLayoutData(gd_txtProduct);
        
        lblShopKorName = new Label(compositeInform, SWT.NONE);
        lblShopKorName.setText("Shop ÇÑ±Û¸í");
        lblShopKorName.setFont(SWTResourceManager.getFont("¸¼Àº °íµñ", 11, SWT.NORMAL));
        
        txtShopKorName = new Text(compositeInform, SWT.BORDER);
        GridData gd_txtShopKorName = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_txtShopKorName.widthHint = 100;
        txtShopKorName.setLayoutData(gd_txtShopKorName);
        
        lboShopEngName = new Label(compositeInform, SWT.NONE);
        lboShopEngName.setText("Shop ¿µ¹®¸í");
        lboShopEngName.setFont(SWTResourceManager.getFont("¸¼Àº °íµñ", 11, SWT.NORMAL));
        
        text = new Text(compositeInform, SWT.BORDER);
        GridData gd_text = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_text.widthHint = 340;
        text.setLayoutData(gd_text);
        
        lblJph = new Label(compositeInform, SWT.NONE);
        lblJph.setText("JPH");
        lblJph.setFont(SWTResourceManager.getFont("¸¼Àº °íµñ", 11, SWT.NORMAL));
        
        composite = new Composite(compositeInform, SWT.NONE);
        GridLayout gl_composite = new GridLayout(6, false);
        gl_composite.marginWidth = 0;
        composite.setLayout(gl_composite);
        
        text_1 = new Text(composite, SWT.BORDER);
        GridData gd_text_1 = new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1);
        gd_text_1.widthHint = 100;
        text_1.setLayoutData(gd_text_1);
        new Label(composite, SWT.NONE);
        
        lblAllowance = new Label(composite, SWT.NONE);
        lblAllowance.setFont(SWTResourceManager.getFont("¸¼Àº °íµñ", 11, SWT.NORMAL));
        lblAllowance.setText("ºÎ´ë°è¼ö");
        new Label(composite, SWT.NONE);
        
        txtAllowance = new Text(composite, SWT.BORDER);
        GridData gd_txtAllowance = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_txtAllowance.widthHint = 100;
        txtAllowance.setLayoutData(gd_txtAllowance);
        
        lblVehicleKorName = new Label(compositeInform, SWT.NONE);
        lblVehicleKorName.setText("Â÷Á¾ ÇÑ±Û¸í");
        lblVehicleKorName.setFont(SWTResourceManager.getFont("¸¼Àº °íµñ", 11, SWT.NORMAL));
        
        txtVehicleName = new Text(compositeInform, SWT.BORDER);
        GridData gd_txtVehicleName = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_txtVehicleName.widthHint = 340;
        txtVehicleName.setLayoutData(gd_txtVehicleName);
        
        lblVehicleEngName = new Label(compositeInform, SWT.NONE);
        lblVehicleEngName.setText("Â÷Á¾ ¿µ¹®¸í");
        lblVehicleEngName.setFont(SWTResourceManager.getFont("¸¼Àº °íµñ", 11, SWT.NORMAL));
        
        txtVehicleEngName = new Text(compositeInform, SWT.BORDER);
        GridData gd_txtVehicleEngName = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_txtVehicleEngName.widthHint = 340;
        txtVehicleEngName.setLayoutData(gd_txtVehicleEngName);

    }

 
}
