package com.helios.gao;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;
import net.sf.jsqlparser.util.deparser.SelectDeParser;

/**
 *替换一个 sql 语句中的特定的字符串
*@author : gaozhiwen
*@date : 2018/5/4
*/
public class ReplaceStringValue {
    public static void main(String[] args) throws JSQLParserException{
        String sql = "SELECT NAME, ADDRESS, COL1 FROM USER WHERE SSN IN ('1111111111111', '22222222222222');";
        Select select = (Select) CCJSqlParserUtil.parse(sql);

        //Start of value modification
        StringBuilder buffer = new StringBuilder();
        ExpressionDeParser expressionDeParser = new ExpressionDeParser() {
            @Override
            public void visit(StringValue stringValue) {
                this.getBuffer().append("XXXX");
            }
        };
        // ???
        SelectDeParser deParser = new SelectDeParser(expressionDeParser, buffer);
        expressionDeParser.setSelectVisitor(deParser);
        expressionDeParser.setBuffer(buffer);
        select.getSelectBody().accept(deParser);
        //End of value modification

        System.out.println(buffer.toString());
    }
}
