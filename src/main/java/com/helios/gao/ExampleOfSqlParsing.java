package com.helios.gao;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.util.AddAliasesVisitor;
import net.sf.jsqlparser.util.SelectUtils;
import net.sf.jsqlparser.util.TablesNamesFinder;

import java.util.ArrayList;
import java.util.List;

/**
*@author : gaozhiwen
*@date : 2018/5/4
*/
public class ExampleOfSqlParsing {
    public static void main(String[] args) throws JSQLParserException {
        List<String> tableList = getTableName("SELECT * FROM MY_TABLE1,MY_TABLE2");
        tableList.forEach(t -> System.out.println(t));

        List<String> items = getItems("select a,b,c from table1 where id=1");
        items.forEach(i -> System.out.println(i));

        Select select = applyAliases("select a,b,c from test");
        System.out.println(select.toString());

        Select select1 = addColumn("select a from test", "b");
        System.out.println(select1.toString());

        Select select2 = addGroupBy("select name from test", "name");
        System.out.println(select2.toString());

        Select select3 = addJoin("select a from table", "test", "b");
        System.out.println(select3.toString());

        Select select4 = SelectUtils.buildSelectFromTable(new Table("mytable"));
        System.out.println(select4.toString());

        Select select5 = SelectUtils.buildSelectFromTableAndExpressions(new Table("mytable"),"a","b");
        System.out.println(select5.toString());

        Select select6 = SelectUtils.buildSelectFromTableAndExpressions(new Table("mytable"),new Column("a"));
        System.out.println(select6.toString());

        Select select7 = addInnerJoin("select a,b from mytable", "mytable2", "c");
        System.out.println(select7.toString());

        Select select8 = addWhere("select a,b from mytable", "a = 1");
        System.out.println(select8.toString());

        Select select9 = (Select) CCJSqlParserUtil.parse("select a from table where a = 1");
        PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
        Expression expr =  plainSelect.getWhere();
        System.out.println(select9.toString());
    }

    /**
     * 获取 sql 语句中的表名
     * @param sql
     * @return
     * @throws JSQLParserException
     */
    public static List<String> getTableName(String sql) throws JSQLParserException {
        Statement statement = CCJSqlParserUtil.parse(sql);
        Select selectStatement = (Select) statement;
        TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
        List<String> tableList = tablesNamesFinder.getTableList(selectStatement);
        return tableList;
    }

    /**
     * 获取 sql 语句里面的参数
     * @param sql
     * @return
     * @throws JSQLParserException
     */
    public static List<String> getItems(String sql) throws JSQLParserException {
        Select select = (Select) CCJSqlParserUtil.parse(sql);
        PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
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
     * 给表达式中的参数起别名
     * @param sql
     * @return
     * @throws JSQLParserException
     */
    public static Select applyAliases(String sql) throws JSQLParserException {
        Select select = (Select) CCJSqlParserUtil.parse(sql);
        final AddAliasesVisitor addAliasesVisitor = new AddAliasesVisitor();
        select.getSelectBody().accept(addAliasesVisitor);
        return select;
    }

    /**
     * 给表达式中添加一个需要查询的参数
     * @param sql
     * @param col
     * @return
     * @throws JSQLParserException
     */
    public static Select addColumn(String sql, String col) throws JSQLParserException {
        Select select = (Select) CCJSqlParserUtil.parse(sql);
        SelectUtils.addExpression(select, new Column(col));
        return select;
    }

    /**
     * 给表达式添加一个 group by 查询条件
     * @param sql
     * @param expr
     * @return
     * @throws JSQLParserException
     */
    public static Select addGroupBy(String sql, String expr) throws JSQLParserException {
        Select select = (Select) CCJSqlParserUtil.parse(sql);
        SelectUtils.addGroupBy(select, new Column(expr));
        return select;
    }

    /**
     * 给表达式添加一个 Join  on
     * @param sql
     * @param tableName
     * @param expr
     * @return
     * @throws JSQLParserException
     */
    public static Select addJoin(String sql, String tableName, String expr) throws JSQLParserException {
        Select select = (Select) CCJSqlParserUtil.parse(sql);
        SelectUtils.addJoin(select, new Table(tableName), new Column(expr));
        return select;
    }

    /**
     * 给 sql 语句加一个 Inner Join
     * @param sql
     * @param tableName
     * @return
     * @throws JSQLParserException
     */
    public static Select addInnerJoin(String sql, String tableName, String expr) throws JSQLParserException {
        Select select = (Select) CCJSqlParserUtil.parse(sql);
        PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
        List<Join> joinList = new ArrayList<>();
        Join join = new Join();
        join.setInner(true);
        join.setRightItem(new Table(tableName));
        join.setOnExpression(new Column(expr));
        joinList.add(join);
        plainSelect.setJoins(joinList);
        return select;
    }

    /**
     * 给 sql 语句加 where 条件
     * @param sql
     * @param where_sql
     * @return
     * @throws JSQLParserException
     */
    public static Select addWhere(String sql, String where_sql) throws JSQLParserException {
        Select select = (Select) CCJSqlParserUtil.parse(sql);
        PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
        Expression where_expression = (Expression) CCJSqlParserUtil.parseCondExpression(where_sql);
        plainSelect.setWhere(where_expression);
        return select;
    }

}
