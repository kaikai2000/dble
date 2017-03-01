/**
 * 
 */
package io.mycat.plan.common.item.function.castfunc;

import java.io.UnsupportedEncodingException;
import java.util.List;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.expr.SQLMethodInvokeExpr;

import io.mycat.backend.mysql.CharsetUtil;
import io.mycat.plan.common.field.Field;
import io.mycat.plan.common.item.Item;
import io.mycat.plan.common.item.function.ItemFuncKeyWord;
import io.mycat.plan.common.item.function.strfunc.ItemStrFunc;

public class ItemFuncConvCharset extends ItemStrFunc {
	private String mysqlCharset;
	private String javaCharset;

	public ItemFuncConvCharset(Item a, String charset) {
		super(a);
		mysqlCharset = charset;
		javaCharset = CharsetUtil.getJavaCharset(charset);
	}

	@Override
	public final String funcName() {
		return "CONVERT";
	}

	@Override
	public void fixLengthAndDec() {

	}

	@Override
	public String valStr() {
		String argVal = args.get(0).valStr();
		if (argVal == null) {
			nullValue = true;
			return null;
		}
		try {
			return new String(argVal.getBytes(), javaCharset);
		} catch (UnsupportedEncodingException e) {
			logger.warn("convert using charset exception", e);
			nullValue = true;
			return null;
		}
	}

	@Override
	public SQLExpr toExpression() {
		SQLMethodInvokeExpr method = new SQLMethodInvokeExpr(funcName());
		method.addParameter(args.get(0).toExpression());
		method.putAttribute(ItemFuncKeyWord.USING, mysqlCharset);
		return method;
	}

	@Override
	protected Item cloneStruct(boolean forCalculate, List<Item> calArgs, boolean isPushDown, List<Field> fields) {
		List<Item> newArgs = null;
		if (!forCalculate)
			newArgs = cloneStructList(args);
		else
			newArgs = calArgs;
		return new ItemFuncConvCharset(newArgs.get(0), mysqlCharset);
	}

}