package com.helios.gao;

import com.helios.gao.utils.LeftOrRight;
import com.helios.gao.utils.SelectUtil;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.*;

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
        Select select = transformSelect(sql, "table2");
        System.out.println(select);
//        select t3.a, t2.b from table1 t1 inner join table t2 inner join table3 t3 on t1.id = t2.id where t1.id = 1

        Select select1 = transformSelectTwo(select, "table3", "t3");
        System.out.println(select1);

    }

    public static Select transformSelect(String sql, String tableName) throws JSQLParserException {
        SelectUtil selectUtil = new SelectUtil();
        Select select = (Select) CCJSqlParserUtil.parse(sql);
        PlainSelect plainSelect = (PlainSelect) select.getSelectBody();

        //获取需要查询的列名
        List<String> str_items = selectUtil.getColumnList(plainSelect);

        //获取 where 部分
        Expression expr_where = selectUtil.getWhere(plainSelect);

        //与 table2 内连接
        selectUtil.addInnerJoin(plainSelect, new Table(tableName));

        //获取表名
        List<String> tableList = selectUtil.getTableName(select);
        //给表名添加表别名，别名手动输入,可以直接批量定义别名（默认 usaAs 为 false），也可以单个定义别名
        List<Table> tables = new ArrayList<>();
        List<String> aliasList = new ArrayList<>();
        aliasList.add("t1");
        aliasList.add("t2");
        try {
            tables =  selectUtil.addAliasForTable(tableList, aliasList);
        } catch (Exception e) {
            System.out.println(e);
        }



        //获取表别名
        List<String> tableAliasList = new ArrayList<>();
        for (int i = 0; i < tables.size(); i++) {
            String tableAlias = tables.get(i).getAlias().getName();
            tableAliasList.add(tableAlias);
        }

        //改变 select items
        List<String> changed_items = selectUtil.updateSelectItem(str_items, tableAliasList);

        //改变原有查询主体
        String str_left = selectUtil.getWhere(plainSelect, LeftOrRight.LEFT).toString();
        String where = tableAliasList.get(0) + "." + str_left + " = " + tableAliasList.get(1) + "." + str_left;
        Expression on_Expression = CCJSqlParserUtil.parseCondExpression(where);
        //很奇怪明明已经清空了 但是为何还会保留?
        //很奇怪 重新去创建一个也不可以么？
        //PlainSelect 对象是一个代理对象?
//        plainSelect.setFromItem(null);

        //重新创建一个 select 语句，不在原有的语句上做修改
        Select select1 = selectUtil.buildSelectSql(tables.get(0), changed_items);
        PlainSelect plainSelect1 = (PlainSelect) select1.getSelectBody();
        //或许应该从新去创建一个新得语句，而不是在原有得语句上修改
        selectUtil.addInnerJoin(plainSelect1, tables.get(1), on_Expression);


        //该变 where 查询条件
        String str_change_left = tableAliasList.get(0) + "." + selectUtil.getWhere(plainSelect, LeftOrRight.LEFT).toString();
        Expression change_left = CCJSqlParserUtil.parseCondExpression(str_change_left);
        ((BinaryExpression) expr_where).setLeftExpression(change_left);
        //添加 where 查询条件
        selectUtil.addWhere(plainSelect1, expr_where);

        return select1;
    }

    public static Select transformSelectTwo(Select select, String tableName, String tableAlias) throws JSQLParserException {
        Select select1 = new Select();
        SelectUtil selectUtil = new SelectUtil();

        //连接 table3
        PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
        selectUtil.addInnerJoin(plainSelect, new Table(tableName), "t3");

        //获取 selectItem
        List<String> selectitems = selectUtil.getColumnList(plainSelect);
        //将 selectItem 中参数 a 的前缀改为 t3
        selectUtil.replaceColumnPrefix(selectitems, "a", "t3");

        //替换 selectItems
        List<SelectItem> items = selectUtil.ListToSelectItems(selectitems);
        plainSelect.setSelectItems(items);

        select1.setSelectBody(plainSelect);
        return select1;
    }
}
