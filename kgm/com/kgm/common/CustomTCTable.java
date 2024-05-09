package com.kgm.common;

import com.teamcenter.rac.common.TCTable;
import com.teamcenter.rac.kernel.TCComponentType;
import com.teamcenter.rac.kernel.TCSession;

/**
 * TCTable을 상속받아서 만듬, D&D 기능을 막기 위함
 * @Copyright : S-PALM
 * @author   : 이정건
 * @since    : 2011. 11. 16.
 * Package ID : com.teamcenter.posco.common.CustomTCTable.java
 */
public class CustomTCTable extends TCTable
{
	private static final long serialVersionUID = 1L;

	public CustomTCTable()
    {
        super();
    }

    public CustomTCTable(String[] as, String[] as1)
    {
        super(as, as1);
    }

    public CustomTCTable(String[] as)
    {
        super(as);
    }

    public CustomTCTable(TCSession imansession, String[] as)
    {
        super(imansession, as);
    }

    public CustomTCTable(TCSession imansession, String s, String s1)
    {
        super(imansession, s, s1);
    }

    public CustomTCTable(TCSession imansession, String s, String s1, TCComponentType imancomponenttype)
    {
        super(imansession, s, s1, imancomponenttype);
    }

    public CustomTCTable(TCSession imansession, String s, String s1, String s2)
    {
        super(imansession, s, s1, s2);
    }

    public CustomTCTable(TCSession imansession, String s, String s1, String s2, boolean flag)
    {
        super(imansession, s, s1, s2, flag);
    }

    protected void enableDnDrop()
    {
    }

}
