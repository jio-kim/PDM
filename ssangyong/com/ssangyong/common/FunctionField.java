package com.ssangyong.common;

import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.util.Painter;
import java.awt.Graphics;
import java.awt.event.FocusEvent;
import javax.swing.JTextField;
import javax.swing.text.*;

/**
 * ****************************************************************************************************
 * 개요  FunctionField<BR>
 * 설명  swing 에 사용되는 필드나 박스에 옵션을 걸어주는 클래스
 *****************************************************************************************************
 */
public class FunctionField extends JTextField implements SYMCInterfaceComponent
{
	private static final long serialVersionUID = 1L;
	
	
	private TCComponent tcComponent;

	


  private class FunctionDocument extends PlainDocument
	{

		private static final long serialVersionUID = 1L;

		public void insertString(int offs, String str, AttributeSet attr)
		throws BadLocationException
		{
			if(str == null)
				return;
			String afterStr = "";
			if(getLimitedSize() != 0)
			{
				if(limitSize <= getLength())
				{
					getToolkit().beep();
					return;
				}
				if(limitSize < getLength() + str.length())
					str = str.substring(0, limitSize - getLength());
			}
			if(getFunctionMode() == 0)
				afterStr = str;
			else
				if(getFunctionMode() == 1)
					afterStr = str.toUpperCase();
				else
					if(getFunctionMode() == 2)
					{
						afterStr = str.toLowerCase();
					} else
					{
						char aNnumber[] = str.toCharArray();
						for(int i = 0; i < aNnumber.length; i++)
						{
							String tmpStr = (new StringBuffer(String.valueOf(aNnumber[i]))).toString();
							if(tmpStr.hashCode() == 46)
							{

								if(getFunctionMode() == 3)
								{
									getToolkit().beep();
									continue;
								}
							} else
								if(getFunctionMode() == 5 && (tmpStr.hashCode() == 43 || tmpStr.hashCode() == 45))
								{
									if(offs != 0)
									{
										getToolkit().beep();
										continue;
									}
								} else
									if(tmpStr.hashCode() < 48 || tmpStr.hashCode() > 57)
									{
										getToolkit().beep();
										continue;
									}
							afterStr = afterStr + tmpStr;
						}

					}
			super.insertString(offs, afterStr, attr);
		}

		FunctionDocument()
		{
		}
	}


	public static final int DEFAULT = 0;
	public static final int UPPER_CASE = 1;
	public static final int LOWER_CASE = 2;
	public static final int NUMERIC = 3;
	public static final int DOUBLE = 4;
	public static final int PMDOUBLE = 5;
	private int functionType;
	private int limitSize;
	private boolean mandatory = false;

	public FunctionField()
	{
		this(null, 10, 0, false);
	}
	
	public FunctionField(boolean mandatory)
	{
		this(null, 10, 0, mandatory);
	}
	
	public FunctionField(int columns)
	{
		this(null, columns, 0, false);
	}

	public FunctionField(int columns, boolean mandatory)
	{
		this(null, columns, 0, mandatory);
	}
	
	public FunctionField(String text)
	{
		this(text, 10, 0, false);
	}

	public FunctionField(String text, boolean mandatory)
	{
		this(text, 10, 0, mandatory);
	}
	
	public FunctionField(int columns, int function)
	{
		this(null, columns, function, false);
	}

	public FunctionField(int columns, int function, boolean mandatory)
	{
		this(null, columns, function, mandatory);
	}
	
	public FunctionField(String text, int column)
	{
		this(text, column, 0, false);
	}

	public FunctionField(String text, int column, boolean mandatory)
	{
		this(text, column, 0, mandatory);
	}
	
	public FunctionField(String text, int columns, int function)
	{
		super(text, columns);
		this.functionType = function;
		this.mandatory = false;
		enableEvents(4L);
	}

	public FunctionField(String text, int columns, int function, boolean mandatory)
	{
		super(text, columns);
		this.functionType = function;
		this.mandatory = mandatory;
		enableEvents(4L);
	}
	
	public FunctionField(String text, int columns, int function, int limitSize)
	{
		super(text, columns);
		this.functionType = function;
		this.limitSize = limitSize;
		this.mandatory = false;
		enableEvents(4L);
	}

	public FunctionField(String text, int columns, int function, int limitSize, boolean mandatory)
	{
		super(text, columns);
		this.functionType = function;
		this.limitSize = limitSize;
		this.mandatory = mandatory;
		enableEvents(4L);
	}

	public void setFunctionMode(int function)
	{
		this.functionType = function;
	}

	public int getFunctionMode()
	{
		return functionType;
	}

	public void setLimitingSize(int size)
	{
		this.limitSize = size;
	}

	public int getLimitedSize()
	{
		return limitSize;
	}

	public int getIntValue()
	{
		String value = getText();
		if(value != null && !value.equals(""))
			try
		{
				return Integer.parseInt(value);
		}
		catch(NumberFormatException numberformatexception) { }
		return 0;
	}

	public void processFocusEvent(FocusEvent fe)
	{
		if(fe.getID() == 1004)
			selectAll();
		super.processFocusEvent(fe);
	}

	protected Document createDefaultModel()
	{
		return new FunctionDocument();
	}

	public void setMandatory(boolean flag)
	{
		mandatory = flag;
	}

	public boolean isMandatory()
	{
		return mandatory;
	}

	public void paint(Graphics g)
	{
		super.paint(g);
		if(mandatory)
			Painter.paintIsRequired(this, g);
	}

	@Override
	public Object getValue() {
		return getText();
	}
	
	 
  public TCComponent getTcComponent()
  {
    return tcComponent;
  }

  public void setTcComponent(TCComponent tcComponent)
  {
    this.tcComponent = tcComponent;
  }

}
