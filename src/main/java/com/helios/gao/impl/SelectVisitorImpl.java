package com.helios.gao.impl;

import net.sf.jsqlparser.statement.select.*;

/**
*@author : gaozhiwen
*@date : 2018/5/8
*/
public class SelectVisitorImpl implements SelectVisitor{
    @Override
    public void visit(PlainSelect plainSelect) {
        //访问 select
        if (plainSelect.getSelectItems() != null) {
            for (SelectItem item : plainSelect.getSelectItems()) {
                item.accept(new SelectItemVisitorImpl());
            }
        }
    }

    @Override
    public void visit(SetOperationList setOperationList) {

    }

    @Override
    public void visit(WithItem withItem) {

    }
}
