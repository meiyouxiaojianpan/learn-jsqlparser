package com.helios.gao;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;
import net.sf.jsqlparser.util.deparser.SelectDeParser;
import net.sf.jsqlparser.util.deparser.StatementDeParser;

/**
 * 以一种更一般的方式替换各种语句中的 String and long value
*@author : gaozhiwen
*@date : 2018/5/4
*/
public class ReplaceColumnValues {
    static class ReplaceColumnAndLongValues extends ExpressionDeParser {
        @Override
        public void visit(LongValue longValue) {
            this.getBuffer().append("?");
        }

        @Override
        public void visit(StringValue stringValue) {
            this.getBuffer().append("?");
        }
    }

    public static String cleanStatement(String sql) throws JSQLParserException {
        StringBuilder buffer = new StringBuilder();
        ExpressionDeParser expr = new ReplaceColumnAndLongValues();

        SelectDeParser deParser = new SelectDeParser(expr,buffer);
        expr.setSelectVisitor(deParser);
        expr.setBuffer(buffer);
        StatementDeParser statementDeParser = new StatementDeParser(expr, deParser, buffer);

        Statement stmt = CCJSqlParserUtil.parse(sql);

        stmt.accept(statementDeParser);
        return statementDeParser.getBuffer().toString();
    }

    public static void main(String[] args) throws JSQLParserException{
        System.out.println(cleanStatement("SELECT 'abc', 5 FROM mytable WHERE col='test'"));
        System.out.println(cleanStatement("UPDATE table1 A SET A.columna = 'XXX' WHERE A.col_table='YYY'"));
        System.out.println(cleanStatement("INSERT INTO example (num, name, address, tel) VALUES (1, 'name', 'test', '1234-1234')"));
        System.out.println(cleanStatement("DELETE FROM table1 WHERE col='5' AND col2='4'"));
    }
}
