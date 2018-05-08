package com.helios.gao.utils;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.util.SelectUtils;
import net.sf.jsqlparser.util.TablesNamesFinder;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
*@author : gaozhiwen
*@date : 2018/5/7
*/
public class SelectUtil {

    /**
     * 获取 Select 语句中的 Column
     * @param plainSelect
     * @return
     */
    public List<String> getColumnList(PlainSelect plainSelect) {
        List<SelectItem> selectItems = plainSelect.getSelectItems();
        List<String> str_items = new ArrayList<>();
        if (selectItems != null) {
            for (int i = 0; i < selectItems.size(); i++) {
                str_items.add(selectItems.get(i).toString());
            }
        }
        return str_items;
    }

    /**
     * 获取 Select 语句中的 where 部分
     * @param plainSelect
     * @return
     */
    public Expression getWhere(PlainSelect plainSelect) {
        return getWhere(plainSelect, LeftOrRight.ALL);
    }

    /**
     * 获取 Select 语句中的 where 部分
     * @param plainSelect
     * @param leftOrRight
     * @return
     */
    public Expression getWhere(PlainSelect plainSelect, LeftOrRight leftOrRight) {
        Expression expr_where = plainSelect.getWhere();
        if (leftOrRight.equals(LeftOrRight.LEFT)) {
            return ((BinaryExpression) expr_where).getLeftExpression();
        } else if (leftOrRight.equals(LeftOrRight.RIGHT)) {
            return ((BinaryExpression) expr_where).getRightExpression();
        }
        return expr_where;
    }

    /**
     * 加一个内连接的表
     * @param plainSelect
     * @param tableName
     */
    public void addInnerJoin(PlainSelect plainSelect, Table tableName) {
        Expression expression = null;
        addInnerJoin(plainSelect, tableName, expression);
    }

    /**
     * 加一个内连接的表 如果本来的表中含有 Join 会覆盖（不会覆盖了）
     * @param plainSelect
     * @param tableName
     * @param expression
     */
    public void addInnerJoin(PlainSelect plainSelect, Table tableName, Expression expression) {
        List<Join> joins;
        if (plainSelect.getJoins() != null) {
            joins = plainSelect.getJoins();
        } else {
            joins = new ArrayList<>();
        }
//        List<Join> joins = new ArrayList<>();
        //若 tableName 为 null 如何去抛出这个错误？
        Join join = new Join();
        join.setInner(true);
        join.setRightItem(tableName);
        if (expression != null) {
            join.setOnExpression(expression);
        }
        joins.add(join);
        plainSelect.setJoins(joins);
    }

    /**
     * 加一个内连接，自定义别名
     * @param plainSelect
     * @param table
     * @param tableAlias
     */
    public void addInnerJoin(PlainSelect plainSelect, Table table, String tableAlias) {
        addAliasForTable(table, tableAlias);
        addInnerJoin(plainSelect, table);
    }

    /**
     * 获取 select 语句的表名
     * @param select
     * @return
     */
    public List<String> getTableName(Select select) {
        TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
        List<String> tableNameList = tablesNamesFinder.getTableList(select);
        return tableNameList;
    }

    /**
     * 根据传入的 List 改变 select 中原有的列
     * @param plainSelect
     * @param list
     * @throws JSQLParserException
     */
    public void updateColumn(PlainSelect plainSelect, List<String> list) throws JSQLParserException{
        plainSelect.setSelectItems(null);
        for (int i = 0; i < list.size(); i++) {
            SelectItem selectItem = new SelectExpressionItem(CCJSqlParserUtil.parseExpression(list.get(i)));
            plainSelect.addSelectItems(selectItem);
        }
    }

    /**
     * 根据输入的参数列表和表别名列表重新改变参数列表的值，这方法很死板
     * @param selectItems
     * @param tableAliasList
     * @return
     */
    public List<String> updateSelectItem(List<String> selectItems, List<String> tableAliasList) {
        List<String> changedSelectItem = new ArrayList<>();
        for (int i = 0; i < selectItems.size(); i++) {
            //检查 selectItem 是否已经有 表名前缀
            //jsqlparser 应该有自己的方法，怎么操作呢？
            if (selectItems.get(i).indexOf(".") != -1) {
                //selectItem 已经存在表名前缀，去除表名前缀
                int j = selectItems.get(i).indexOf(".") + 1;
                selectItems.set(i, selectItems.get(i).substring(j));
            }
            String item = tableAliasList.get(i) + "." + selectItems.get(i);
            changedSelectItem.add(item);
        }
        return changedSelectItem;
    }

    /**
     * 给参数添加一个 table 前缀
     * @param selectItem
     * @param tableAlias
     * @return
     */
    public String addTableForSelectItem(String selectItem, String tableAlias) {
        String newSelectItem = tableAlias + "." + selectItem;
        return newSelectItem;
    }



    /**
     * 传入一个新的 PlainSelect 对象，给该对象添加 selectItems and fromItems
     * @param plainSelect
     * @param items
     * @param tableName
     * @throws JSQLParserException
     */
    public void createSelectSql(PlainSelect plainSelect, List<String> items, Table tableName) throws JSQLParserException{
        //是否应该对传入的 sql 做判断，正常的应该是一个新的 PlainSelect 对象
        updateColumn(plainSelect, items);
        plainSelect.setFromItem(tableName);
    }

    public Select buildSelectSql(Table tableName, List<String> items) {
        Column [] columns = new Column[items.size()];
        for (int i = 0; i < items.size(); i++) {
            columns[i] = new Column(items.get(i));
        }
        return SelectUtils.buildSelectFromTableAndExpressions(tableName, columns);
    }

    /**
     * 添加 where 条件
     * @param plainSelect
     * @param whereItems
     * @throws JSQLParserException
     */
    public void addWhere(PlainSelect plainSelect, Expression whereItems) throws JSQLParserException{
        plainSelect.setWhere(whereItems);
    }

    /**
     * 根据输入的表和表别名给表添加表别名，useAs 默认为 false
     * @param tableName
     * @param alias
     */
    public void addAliasForTable(Table tableName, String alias) {
        Boolean useAs = false;
        addAliasForTable(tableName, alias, useAs);
    }

    /**
     * 根据输入的表和表别名给表添加表别名
     * @param tableName
     * @param alias
     * @param useAs
     */
    public void addAliasForTable(Table tableName, String alias, Boolean useAs) {
        tableName.setAlias(new Alias(alias, useAs));
    }

    /**
     * 添加表别名
     * @param tableNameList
     * @param aliasList
     * @return
     * @throws Exception
     */
    public List<Table> addAliasForTable(List<String> tableNameList, List<String> aliasList) throws Exception{
        if (tableNameList.size() != aliasList.size()) {
            throw new Exception("传入参数 List 的长度不相等");
        }
        List<Table> tableList = new ArrayList<>();
        for (int i = 0; i < tableNameList.size(); i++) {
            Table table = new Table(tableNameList.get(i));
            addAliasForTable(table, aliasList.get(i));
            tableList.add(table);
        }
        return tableList;
    }

    /**
     * 将一个 selectItems 中指定的参数的前缀替换
     * @param selectItems
     * @param column
     * @param tableAlias
     */
    public void replaceColumnPrefix(List<String> selectItems, String column, String tableAlias) {
        for (int i = 0; i < selectItems.size(); i++) {
            if (selectItems.get(i).indexOf(column) != -1) {
                //找到了含有 column 的 selectItem
                //替换为新的前缀
                selectItems.set(i, replaceColumnPrefix(selectItems.get(i), tableAlias));
                //跳出 for 循环，只能将最先发现的匹配参数的前缀替换
                //可以设置一个参数实现是否替换全部匹配的参数
                break;
            }
        }
    }

    /**
     * 替换一个 selectItem 的前缀
     * @param columnName
     * @param tableAlias
     * @return
     */
    public String replaceColumnPrefix(String columnName, String tableAlias) {
        //应该检查一下 columnName 是否有前缀
        int i = columnName.indexOf(".") + 1;
        String newColumn = tableAlias + "." + columnName.substring(i);
        return newColumn;
    }

    /**
     * 将 List<String> 转换为 List<SelectItem></>
     * @param selectItems
     * @return
     * @throws JSQLParserException
     */
    public List<SelectItem> ListToSelectItems(List<String> selectItems) throws JSQLParserException{
        List<SelectItem> items = new ArrayList<>();
        for (int i = 0; i < selectItems.size(); i++) {
            SelectItem selectItem = new SelectExpressionItem(CCJSqlParserUtil.parseExpression(selectItems.get(i)));
            items.add(selectItem);
        }
        return items;
    }
}
