package com.helios.gao;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.util.TablesNamesFinder;

import java.util.ArrayList;
import java.util.List;

/**
*@author : gaozhiwen
*@date : 2018/5/4
*/
public class Demo {
    public static void main(String[] args) throws JSQLParserException {
        String sql = "select a,b from table1 where id=1";
//        select t1.a, t2.b from table1 t1 inner join table2 t2 on t1.id = t2.id where t1.id=1
        Select select = transformSelect(sql);
        System.out.println(select);
    }

    public static Select transformSelect(String sql) throws JSQLParserException {
        Select select = (Select) CCJSqlParserUtil.parse(sql);
        PlainSelect plainSelect = (PlainSelect) select.getSelectBody();

        //获取需要查询的列名
        List<SelectItem> selectItems = plainSelect.getSelectItems();
        List<String> str_items = new ArrayList<>();
        if (selectItems != null) {
            for (int i = 0; i < selectItems.size(); i++) {
                str_items.add(selectItems.get(i).toString());
            }
        }

        //获取 where 部分
        Expression expr_where = plainSelect.getWhere();
        //获取左边表达式部分
        Expression leftExpression = ((BinaryExpression) expr_where).getLeftExpression();
        //获取右边表达式部分
        Expression rightExpression = ((BinaryExpression) expr_where).getRightExpression();

        //与 table2 内连接
        List<Join> joins = new ArrayList<>();
        Join join = new Join();
        join.setInner(true);
        join.setRightItem(new Table("table2"));
        joins.add(join);
        plainSelect.setJoins(joins);

        //获取表名并添加表别名
        TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
        List<String> tableList = tablesNamesFinder.getTableList(select);
        List<Table> tables = new ArrayList<>();
        for (int i = 0; i < tableList.size(); i++) {
            Table table = new Table(tableList.get(i));
            String alias = "t" + (i + 1);
            table.setAlias(new Alias(alias, false));
            tables.add(table);
        }

        //获取表别名
        List<String> tableAliasList = new ArrayList<>();
        for (int i = 0; i < tables.size(); i++) {
            String tableAlias = tables.get(i).getAlias().getName();
            tableAliasList.add(tableAlias);
        }

        //改变要查询的列
        for (int i = 0; i < str_items.size(); i++) {
            String item = tableAliasList.get(i) + "." + str_items.get(i);
            str_items.set(i, item);
        }

        //该变原有的查询列
        SelectItem [] itemList = new SelectItem[str_items.size()];
        plainSelect.setSelectItems(null);
        for (int i = 0; i < str_items.size(); i++) {
            itemList[i] = new SelectExpressionItem(CCJSqlParserUtil.parseExpression(str_items.get(i)));
            plainSelect.addSelectItems(itemList[i]);
        }

        //改变原有查询主体
        String str_left = leftExpression.toString();
        String where = tableAliasList.get(0) + "." + str_left + " = " + tableAliasList.get(1) + "." + str_left;
        Expression where_Expression = (Expression)(CCJSqlParserUtil.parseCondExpression(where));
        plainSelect.setFromItem(null);
        plainSelect.setFromItem(tables.get(0));
        List<Join> joins1 = new ArrayList<>();
        Join join1 = new Join();
        join1.setInner(true);
        join1.setRightItem(tables.get(1));
        join1.setOnExpression(where_Expression);
        joins1.add(join1);
        plainSelect.setJoins(joins1);

        //该变 where 查询条件
        String str_change_left = tableAliasList.get(0) + "." + leftExpression.toString();
        Expression change_left = (Expression)(CCJSqlParserUtil.parseCondExpression(str_change_left));
        ((BinaryExpression) expr_where).setLeftExpression(change_left);

        return select;
    }
}
