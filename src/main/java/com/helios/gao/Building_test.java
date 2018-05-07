package com.helios.gao;
/**
*@author : gaozhiwen
*@date : 2018/5/6
*/

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.util.TablesNamesFinder;

public class Building_test {

    // 由已知的List<String> 或 String 组装成新的 sql 语句

    // 下面是   三个 sql语句 测试例子，select，insert，update
    public static void build_select(String sql) throws JSQLParserException {
        String change_sql = sql;

        // *********change body items
        List<String> str_items = new ArrayList<String>();
        str_items.add("school");
        str_items.add("teacher");
        change_sql = build_select_items(change_sql, str_items);

        // ********************change table only use " , " as join  such as (from table1,table2,table3)
//		List<String> str_table =new ArrayList<String>();
//		str_table.add("preson1");
//		str_table.add("preson2");
//		str_table.add("preson3");
//		change_sql = build_select_table(change_sql, str_table);

        // ********************change table with join  such as(from tb1 join tb2 left join tb3 right join tb4)
        List<String> str_table =new ArrayList<String>();
        str_table.add("preson1");
        str_table.add("preson2");
        str_table.add("preson3");
        str_table.add("preson4");
        List<String> str_tablewithjoin =new ArrayList<String>();
        str_tablewithjoin.add("preson1");
        str_tablewithjoin.add("LEFT JOIN preson2");
        str_tablewithjoin.add("RIGHT JOIN preson3");
        str_tablewithjoin.add("preson4");
        change_sql=Building_test.build_select_tablewithjoin(change_sql,str_tablewithjoin,str_table);

        // ******************select where
        String str_where = "name like 'abc'";
        change_sql = build_select_where(change_sql, str_where);

        // ******select group by
        List<String> str_groupby = new ArrayList<String>();
        str_groupby.add("studentname");
        str_groupby.add("studentid");
        change_sql = build_select_groupby(change_sql, str_groupby);

        // **************select order by
        List<String> str_orderby = new ArrayList<String>();
        str_orderby.add("name asc");// asc默认不显示
        str_orderby.add("id desc");
        change_sql = build_select_orderby(change_sql, str_orderby);

        System.out.println("final:" + change_sql);
    }

    public static void build_insert(String sql) throws JSQLParserException {
        String change_sql = sql;
        // *******insert table
        String str_table = "teacher";
        change_sql = build_insert_table(change_sql, str_table);

        // ********* insert column
        List<String> str_column = new ArrayList<String>();
        str_column.add("school");
        str_column.add("city");
        change_sql = build_insert_column(change_sql, str_column);

        // ********* insert value
        List<String> str_value = new ArrayList<String>();
        str_value.add("'s'");
        str_value.add("6");
        change_sql = build_insert_values(change_sql, str_value);
        // System.out.println("final:" + change_sql);

    }
    public static void build_update(String sql) throws JSQLParserException {
        String change_sql = sql;
        // *********update table name
        List<String> str_table = new ArrayList<String>();
        str_table.add("yourtable");
        str_table.add("mytable");
        change_sql = build_update_table(change_sql, str_table);
        // *********update column
        List<String> str_column = new ArrayList<String>();
        str_column.add("col4");
        str_column.add("col5");
        str_column.add("col6");
        change_sql = build_update_column(change_sql, str_column);

        // *********update values
        List<String> str_values = new ArrayList<String>();
        str_values.add("'seemlike'");
        str_values.add("'cdc'");
        str_values.add("555");
        change_sql = build_update_values(change_sql, str_values);

        // *******update where
        String str_where = "id like '3'";
        change_sql = build_update_values(change_sql, str_where);
        //	System.out.println("final:" + change_sql);
    }

    // *********change body items
    public static String build_select_items(String sql, List<String> str_items)
            throws JSQLParserException {
        CCJSqlParserManager parserManager = new CCJSqlParserManager();
        Select select = (Select) parserManager.parse(new StringReader(sql));
        // 改写 selectitems 由 List<string> 类型转化为 selectitem 格式的函数
        select = Building_test.buildSelectFromStringExpressions(select,
                str_items);
        return select.toString();

    }

    // 原函数为SelectUtils.buildSelectFromTableAndExpressions
    public static Select buildSelectFromStringExpressions(Select select,
                                                          List<String> expr) throws JSQLParserException {
        SelectItem[] list = new SelectItem[expr.size()];
        PlainSelect plain = (PlainSelect) select.getSelectBody();
        plain.setSelectItems(null); // empty the existing selectitems
        for (int i = 0; i < expr.size(); i++) {
            list[i] = new SelectExpressionItem(
                    CCJSqlParserUtil.parseExpression(expr.get(i)));
            plain.addSelectItems(list[i]);
        }
        select.setSelectBody(plain);
        return select;
    }

    // ********************change table only use " , " as join  such as (from table1,table2,table3)
    //**************notice*********parsing sql use function:test_select_table(sql) get List<Sting>
    public static String build_select_table(String sql, List<String> str_table)
            throws JSQLParserException {
        CCJSqlParserManager parserManager = new CCJSqlParserManager();
        Select select = (Select) parserManager.parse(new StringReader(sql));
        PlainSelect plain = (PlainSelect) select.getSelectBody();
        plain.setFromItem(null);
        plain.setJoins(null);
        List<Join>select_join =new ArrayList<Join>();

        if(str_table.size()==1){
            plain.setFromItem(new Table(str_table.get(0)));
        }
        for(int i=0;i<str_table.size()-1&&str_table.size()>=2;){      //no i++
            Join obj_join= new Join();
            if (i == 0) {
                plain.setFromItem(new Table(str_table.get(0)));
            }
            obj_join.setSimple(true);    //假定所有表的连接均为，——内连接
            i++;
            obj_join.setRightItem(new Table(str_table.get(i)));
            select_join.add(obj_join);
        }
        plain.setJoins(select_join);
        //	plain.setForUpdateTable(new Table(str_table));
        return select.toString();
    }

    // ********************change table with join  such as(from tb1 join tb2 left join tb3 right join tb4)
    //**************notice*********parsing use function:test_select_join(sql) get List<Sting>
    public static String build_select_tablewithjoin(String sql, List<String> str_tablewithjoin,List<String> str_table)
            throws JSQLParserException {
        CCJSqlParserManager parserManager = new CCJSqlParserManager();
        Select select = (Select) parserManager.parse(new StringReader(sql));
        PlainSelect plain = (PlainSelect) select.getSelectBody();

        plain.setFromItem(null);
        plain.setJoins(null);
        List<Join>select_join =new ArrayList<Join>();

        if(str_tablewithjoin.size()==1){
            plain.setFromItem(new Table(str_table.get(0)));
        }
        for(int i=0;i<str_tablewithjoin.size()-1&&str_tablewithjoin.size()>=2;){   //no i++
            Join obj_join= new Join();
            if (i == 0) {
                plain.setFromItem(new Table(str_table.get(0)));
            }
            i++;
            String judge=str_tablewithjoin.get(i).substring(0, 3);
            //这里的判断还不够充分，繁琐；；还有bug、和table name冲突
            if (judge.equals("LEF")) {
                obj_join.setLeft(true);
            } else if (judge.equals("RIG")) {
                obj_join.setRight(true);
            } else if (judge.equals("INN")) {
                obj_join.setInner(true);
            } else if (judge.equals("OUT")) {
                obj_join.setOuter(true);
            } else if (judge.equals("FUL")) {
                obj_join.setFull(true);
            }
            //	else obj_join.setSimple(true);      //tb1 , tb2 == tb1 JOIN tb2

            obj_join.setRightItem(new Table(str_table.get(i)));
            select_join.add(obj_join);
        }
        plain.setJoins(select_join);
        //	plain.setForUpdateTable(new Table(str_table));
        return select.toString();
    }

    // ********************change where
    public static String build_select_where(String sql, String str_where)
            throws JSQLParserException {
        CCJSqlParserManager parserManager = new CCJSqlParserManager();
        Select select = (Select) parserManager.parse(new StringReader(sql));
        PlainSelect plain = (PlainSelect) select.getSelectBody();
        // parseCondExpression函数 不会被空格截断
        Expression where_expression = (Expression) (CCJSqlParserUtil
                .parseCondExpression(str_where));
        plain.setWhere(where_expression);
        return select.toString();
    }

    // ********************change group by
    public static String build_select_groupby(String sql,
                                              List<String> str_groupby) throws JSQLParserException {
        CCJSqlParserManager parserManager = new CCJSqlParserManager();
        Select select = (Select) parserManager.parse(new StringReader(sql));
        PlainSelect plain = (PlainSelect) select.getSelectBody();
        List<Expression> GroupByColumnReferences = new ArrayList<Expression>();
        for (int i = 0; i < str_groupby.size(); i++) {
            GroupByColumnReferences.add(CCJSqlParserUtil
                    .parseExpression(str_groupby.get(i)));
        }
        plain.setGroupByColumnReferences(GroupByColumnReferences);
        return select.toString();
    }

    // ********************change order by
    public static String build_select_orderby(String sql,
                                              List<String> str_orderby) throws JSQLParserException {
        CCJSqlParserManager parserManager = new CCJSqlParserManager();
        Select select = (Select) parserManager.parse(new StringReader(sql));
        PlainSelect plain = (PlainSelect) select.getSelectBody();
        List<OrderByElement> OrderByElements = new ArrayList<OrderByElement>();
        for (int i = 0; i < str_orderby.size(); i++) {
            OrderByElement obj_OrderbyElement = new OrderByElement(); // build
            // Element类实例放在循环内部，后面得到新的element实例
            String str = str_orderby.get(i);
            int k = str.length() - 3;
            char char_final = str.charAt(k);
            Expression orderby_expression = (Expression) (CCJSqlParserUtil
                    .parseExpression(str));
            // 实例初始化，赋值expression以及boolean asc
            obj_OrderbyElement.setExpression(orderby_expression);
            if (char_final == 'a') {
                obj_OrderbyElement.setAsc(true); // asc默认不显示
            }
            else {
                obj_OrderbyElement.setAsc(false);
            }
            // element加入到list中去
            OrderByElements.add(obj_OrderbyElement);
        }
        plain.setOrderByElements(OrderByElements);
        return select.toString();
    }

    // *******insert table
    public static String build_insert_table(String sql, String str_table)
            throws JSQLParserException {
        Statement statement = CCJSqlParserUtil.parse(sql);
        Insert insertStatement = (Insert) statement;
        insertStatement.setTable(new Table(str_table));
        return insertStatement.toString();

    }

    // ********* insert column
    public static String build_insert_column(String sql, List<String> str_column)
            throws JSQLParserException {
        Statement statement = CCJSqlParserUtil.parse(sql);
        Insert insertStatement = (Insert) statement;
        List<Column> insert_column = new ArrayList<Column>();
        for (int i = 0; i < str_column.size(); i++) {
            insert_column.add((Column) CCJSqlParserUtil
                    .parseCondExpression(str_column.get(i)));
        }
        insertStatement.setColumns(insert_column);
        return insertStatement.toString();

    }

    // ********* insert values
    public static String build_insert_values(String sql, List<String> str_value)
            throws JSQLParserException {
        Statement statement = CCJSqlParserUtil.parse(sql);
        Insert insertStatement = (Insert) statement;
        List<Expression> insert_values_expression = new ArrayList<Expression>();
        for (int i = 0; i < str_value.size(); i++) {
            insert_values_expression.add((Expression) CCJSqlParserUtil
                    .parseExpression(str_value.get(i)));
        }
        // list to expressionlist to Itemlist
        insertStatement.setItemsList(new ExpressionList(
                insert_values_expression));
        return insertStatement.toString();

    }

    public static String build_update_table(String sql, List<String> str_table)
            throws JSQLParserException {
        Statement statement = CCJSqlParserUtil.parse(sql);
        Update updateStatement = (Update) statement;
        List<Table> update_table = new ArrayList<Table>();
        for (int i = 0; i < str_table.size(); i++) {
            Table tabletest = new Table(); // string to Table
            tabletest.setName(str_table.get(i));
            update_table.add(tabletest);
        }
        updateStatement.setTables(update_table);
        return updateStatement.toString();

    }
    // *********update column
    public static String build_update_column(String sql, List<String> str_column)
            throws JSQLParserException {
        Statement statement = CCJSqlParserUtil.parse(sql);
        Update updateStatement = (Update) statement;
        List<Column> update_column = new ArrayList<Column>();
        for (int i = 0; i < str_column.size(); i++) {
            update_column.add((Column) CCJSqlParserUtil
                    .parseExpression(str_column.get(i)));
        }
        updateStatement.setColumns(update_column);
        return updateStatement.toString();

    }
    // *********update values
    public static String build_update_values(String sql, List<String> str_values)
            throws JSQLParserException {
        Statement statement = CCJSqlParserUtil.parse(sql);
        Update updateStatement = (Update) statement;
        List<Expression> update_values = new ArrayList<Expression>();
        for (int i = 0; i < str_values.size(); i++) {
            update_values.add((Expression) CCJSqlParserUtil
                    .parseExpression(str_values.get(i)));
        }
        updateStatement.setExpressions(update_values);
        return updateStatement.toString();

    }
    // *******update where
    public static String build_update_values(String sql, String str_where)
            throws JSQLParserException {
        Statement statement = CCJSqlParserUtil.parse(sql);
        Update updateStatement = (Update) statement;
        Expression where_expression = (Expression) (CCJSqlParserUtil
                .parseCondExpression(str_where));
        updateStatement.setWhere(where_expression);
        return updateStatement.toString();

    }
}


