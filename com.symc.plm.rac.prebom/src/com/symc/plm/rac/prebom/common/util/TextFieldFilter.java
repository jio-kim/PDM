package com.symc.plm.rac.prebom.common.util;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;

public class TextFieldFilter extends DocumentFilter {
	
	private boolean onlyInteger = false;
	private int length = -1;
	public TextFieldFilter(){
		this(false);
	}
	
	public TextFieldFilter(boolean onlyInteger){
		this.onlyInteger = onlyInteger;
	}
	
	public TextFieldFilter(int length){
		this.length = length;
	}
	
   @Override
   public void insertString(FilterBypass fb, int offset, String string,
         AttributeSet attr) throws BadLocationException {

      Document doc = fb.getDocument();
      StringBuilder sb = new StringBuilder();
      sb.append(doc.getText(0, doc.getLength()));
      sb.insert(offset, string);

      if (test(sb.toString())) {
         super.insertString(fb, offset, string, attr);
      } else {
         // warn the user and don't allow the insert
      }
   }

   private boolean test(String text) {
      try {
    	  if( length > -1){
    		  return text.length() <= length;
    	  }else{
    		  if( text == null || text.length() < 1){
    			  return true;
    		  }
	    	  if( onlyInteger ){
	    		  Integer.parseInt(text);
	    	  }else{
	    		  Double.parseDouble(text);
	    	  }
	    	  
	    	  if( text.endsWith("d") || text.endsWith("D") || text.endsWith("f") || text.endsWith("F")){
	    		  return false;
	    	  }
    	  }
         return true;
      } catch (NumberFormatException e) {
         return false;
      }
   }

   @Override
   public void replace(FilterBypass fb, int offset, int length, String text,
         AttributeSet attrs) throws BadLocationException {

      Document doc = fb.getDocument();
      StringBuilder sb = new StringBuilder();
      sb.append(doc.getText(0, doc.getLength()));
      sb.replace(offset, offset + length, text);

      if (test(sb.toString())) {
         super.replace(fb, offset, length, text, attrs);
      } else {
         // warn the user and don't allow the insert
      }

   }

   @Override
   public void remove(FilterBypass fb, int offset, int length)
         throws BadLocationException {
      Document doc = fb.getDocument();
      StringBuilder sb = new StringBuilder();
      sb.append(doc.getText(0, doc.getLength()));
      sb.delete(offset, offset + length);

      if (test(sb.toString())) {
         super.remove(fb, offset, length);
      } else {
    	  if( sb.toString().equals("")){
    		  super.remove(fb, offset, length);
    	  }
      }

   }
}