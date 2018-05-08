package com.helios.gao;

import com.helios.gao.utils.SelectUtil;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.ItemsListVisitor;
import net.sf.jsqlparser.expression.operators.relational.MultiExpressionList;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.select.SubSelect;

import java.util.ArrayList;
import java.util.List;

/**
 * 一个简单的扩展插入示例
*@author : gaozhiwen
*@date : 2018/5/4
*/
public class ExampleOfSqlBuilding {
    public static void main(String[] args) throws JSQLParserException{

        Insert insert = (Insert) CCJSqlParserUtil.parse("insert into mytable (col1) values (1)");
        System.out.println(insert.toString());

        //adding a column
        insert.getColumns().add(new Column("col2"));

        //adding a value using a visitor (visitor 有什么作用？)
        insert.getItemsList().accept(new ItemsListVisitor() {
            @Override
            public void visit(SubSelect subSelect) {
                throw new UnsupportedOperationException("Not supported yet");
            }

            @Override
            public void visit(ExpressionList expressionList) {
                expressionList.getExpressions().add(new LongValue(5));
            }

            @Override
            public void visit(MultiExpressionList multiExpressionList) {
                throw new UnsupportedOperationException("Not supported yet");
            }
        });
        System.out.println(insert.toString());

        //adding another column
        insert.getColumns().add(new Column("col3"));

        //adding another value (the easy way)
        ((ExpressionList) insert.getItemsList()).getExpressions().add(new LongValue(10));

        //adding four column
        insert.getColumns().add(new Column("col4"));
        //adding value
        ((ExpressionList) insert.getItemsList()).getExpressions().add(new LongValue(12));

        System.out.println(insert.toString());

        // 输出 table.a
        SelectExpressionItem selectItem = new SelectExpressionItem();
        selectItem.setExpression(new Column(new Table("table"),"a"));

        PlainSelect plainSelect = new PlainSelect();
        List<SelectItem> items = new ArrayList<>();
        items.add(new SelectExpressionItem(CCJSqlParserUtil.parseExpression("a")));
        items.add(new SelectExpressionItem(CCJSqlParserUtil.parseExpression("b")));
        plainSelect.setSelectItems(items);

        plainSelect.setFromItem(new Table("table"));

        plainSelect.setWhere(CCJSqlParserUtil.parseCondExpression("id = 1"));
        System.out.println(plainSelect);

        //根据输入添加表别名
        Table table = new Table("table1");
//        table.setAlias(new Alias("t1", false));

        SelectUtil selectUtil = new SelectUtil();
        selectUtil.addAliasForTable(table, "t1", false);
        System.out.println(table);
    }
}
