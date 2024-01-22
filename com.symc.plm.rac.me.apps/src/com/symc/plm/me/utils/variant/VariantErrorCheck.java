/**
 * 
 */
package com.symc.plm.me.utils.variant;

import java.util.ArrayList;

import com.teamcenter.rac.pse.variants.modularvariants.ConditionElement;
import com.teamcenter.rac.pse.variants.modularvariants.ConstraintsModel;
import com.teamcenter.rac.pse.variants.modularvariants.ErrorCheck;
import com.teamcenter.rac.pse.variants.modularvariants.MVLLexer;
import com.teamcenter.rac.pse.variants.modularvariants.ModularOptionModel;
import com.teamcenter.rac.util.Registry;

public class VariantErrorCheck extends ErrorCheck {
    public void setCondition(ConditionElement[] condition){
        this.condition = condition;
    }
    
    public ConditionElement[] getCondition(){
        return condition;
    }
    
    public void addCondition(ConditionElement conditionElm){
        if( condition == null || condition.length == 0){
            condition = new ConditionElement[]{conditionElm};
            return;
        }
        
        ConditionElement[] newCondition = new ConditionElement[condition.length + 1];
        System.arraycopy(condition, 0, newCondition, 0, condition.length);
        newCondition[condition.length] = conditionElm;
        
        condition = newCondition;
    }
    
    public int getConditionSize(){
        if( condition == null || condition.length == 0){
            return 0;
        }else{
            return condition.length;
        }
    }
    
     public void appendConstraints(ConditionElement[] conditions, StringBuilder stringbuilder)
        {
            if(conditions != null)
            {
                stringbuilder.append("if ");
                ConstraintsModel.appendCondition(conditions, stringbuilder);
                stringbuilder.append(" then\n ");
            }
            super.appendConstraint(stringbuilder);
            if(condition != null){
                stringbuilder.append("\nendif");
            }
            stringbuilder.append((char)13);
        }
     
    public void appendConstraints(StringBuilder stringbuilder)
    {
        if(condition != null)
        {
            stringbuilder.append("if ");
            ConstraintsModel.appendCondition(condition, stringbuilder);
            stringbuilder.append(" then\n ");
        }
        super.appendConstraint(stringbuilder);
        if(condition != null)
            stringbuilder.append("\nendif");
        
        stringbuilder.append((char)13);
    }
    
    private String parsePath(MVLLexer lexer)
    {
        StringBuilder stringbuilder = new StringBuilder();
        if(lexer.token() == 1)
            for(; lexer.token() == 1 || lexer.token() == 52; lexer.advance())
                stringbuilder.append(lexer.id());

        return stringbuilder.toString();
    }
    
    private String localizeBool(int i)
    {
        Registry registry = Registry.getRegistry("com.teamcenter.rac.pse.variants.modularvariants.modularvariants");
        if(i == 4)
            return registry.getString("ovetrue.TEXT");
        else
            return registry.getString("ovefalse.TEXT");
    }
    
    private String parseValue(MVLLexer lexer)
    {
        String s = null;
        switch(lexer.token())
        {
        case 0: // '\0'
            s = lexer.literal();
            break;

        case 4: // '\004'
        case 5: // '\005'
            s = localizeBool(lexer.token());
            break;

        case 2: // '\002'
        case 3: // '\003'
            s = lexer.id();
            break;

        case 1: // '\001'
        default:
            return null;
        }
        lexer.advance();
        return s;
    }
    
    private ItemOptPair parseOption(MVLLexer lexer)
    {
        if(lexer.token() != 1)
            return null;
        ItemOptPair itemoptpair = new ItemOptPair();
        itemoptpair.item = lexer.id();
        lexer.advance();
        if(lexer.token() != 53)
            return null;
        lexer.advance();
        if(lexer.token() != 1)
            return null;
        itemoptpair.option = lexer.id();
        lexer.advance();
        if(lexer.token() == 58)
        {
            lexer.advance();
            itemoptpair.path = parsePath(lexer);
            if(itemoptpair.path.length() == 0)
                return null;
            if(lexer.token() != 59)
                return null;
            lexer.advance();
        }
        return itemoptpair;
    }
    
    public ConditionElement[] parseConditionElements(MVLLexer lexer)
    {
        ArrayList<ConditionElement> arraylist = new ArrayList<ConditionElement>();
        ConditionElement conditionelement = new ConditionElement();
        conditionelement.ifOrAnd = "if";
        do
        {
            if(lexer.token() == 23 || lexer.token() == 61)
                break;
            ItemOptPair itemoptpair = parseOption(lexer);
            if(itemoptpair == null || itemoptpair.path.length() > 0)
                return null;
            conditionelement.item = itemoptpair.item;
            conditionelement.option = itemoptpair.option;
            conditionelement.fullName = ModularOptionModel.fullOptionName(conditionelement.item, conditionelement.option);
            if(lexer.token() < 33 || lexer.token() > 38)
                return null;
            conditionelement.op = lexer.id();
            lexer.advance();
            boolean flag = lexer.token() == 0;
            if(lexer.token() == 4 || lexer.token() == 5)
                conditionelement.deLocalizedValue = lexer.id();
            conditionelement.value = parseValue(lexer);
            if(conditionelement.value == null)
                return null;
            conditionelement.valueIsString = flag;
            switch(lexer.token())
            {
            case 39: // '\''
            case 41: // ')'
                arraylist.add(conditionelement);
                conditionelement = new ConditionElement();
                conditionelement.ifOrAnd = lexer.id();
                lexer.advance();
                if(lexer.token() != 1)
                    return null;
                break;

            case 23: // '\027'
            case 61: // '='
                arraylist.add(conditionelement);
                break;

            default:
                return null;
            }
        } while(true);
        
        condition = arraylist.toArray(new ConditionElement[arraylist.size()]);
        return condition;
    }
    
    private class ItemOptPair
    {

        public String item;
        public String option;
        public String path;

        private ItemOptPair()
        {
            path = new String();
        }

    }
}
