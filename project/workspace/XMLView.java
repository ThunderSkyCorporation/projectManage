package com.yucheng.lianame.mvc;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ecc.emp.core.Context;
import com.ecc.emp.core.EMPConstance;
import com.ecc.emp.core.EMPException;
import com.ecc.emp.data.DataElement;
import com.ecc.emp.data.KeyedCollection;
import com.ecc.emp.web.servlet.view.ExceptionView;
import com.ecc.liana.base.Trace;

/**
 * XML视图实现类。 以XML文档形式将数据及其他信息输出到响应。
 * 
 * @author lijia@yuchengtech.com
 */
public class XMLView extends ExceptionView {
	/**
	 * 该视图对应的目标网页地址
	 */
	private String url;
	/**
	 * 从指定域中取得该视图对应的目标网页地址(若url未设置，则按此查找)
	 */
	private String urlField;
	/**
	 * 当该视图作为异常视图时，对应的异常类名
	 */
	private String exceptionName;
 
	/**
	 * 视图的渲染方法。将本次执行结果输出到response。
	 * 
	 * @param model
	 *            数据模型对象(包含context)
	 * @param request
	 *            HTTP请求
	 * @param response
	 *            HTTP响应
	 * @param rootPath
	 *            页面根目录
	 */
	public void render( Map model, HttpServletRequest request, HttpServletResponse response, String rootPath )
	{
		response.addHeader( "Cache-Control", "no-cache" );
		response.addHeader( "Content-Type", "application/xml" );
		try
		{
			//response.getOutputStream().write( getXMLResponse( model ).getBytes( "UTF-8" ) );
		}
		catch ( Exception e )
		{
			e.printStackTrace();
		}
	}
 
	
	/**
	 * 将数据和其他相关信息转为XML格式。
	 * 
	 * @param model
	 *            数据模型对象(包含context)
	 * @return XML文档
	 */
	private String getXMLResponse( Map model )
	{
		StringBuffer sb = new StringBuffer();
		KeyedCollection output = (KeyedCollection)model.get(EMPConstance.ATTR_FLOW_OUTPUT);
		Context context = (Context) model.get( EMPConstance.ATTR_CONTEXT );
		//Exception exception = (Exception) model.get( EMPConstance.ATTR_EXCEPTION );
		String ec = "0"; // 正常时，错误码为0
		String errorMsg = "asass";
		if ( exception != null )
		{
			if ( exception instanceof EMPException )
			{
				ec = ((EMPException) exception).getErrorCode();
				errorMsg = ((EMPException) exception).getShowMessage();
			}
			else
			{
				ec = "9999"; // 默认错误码
				errorMsg = exception.getMessage();
			}
		}
		sb.append( "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>" );
		sb.append( "<root>" );
		sb.append( "<ec>" ).append( ec ).append( "</ec>" );
		sb.append( "<em>" ).append( errorMsg ).append( "</em>" );
		if ( url != null )
		{
			sb.append( "<rp>" ).append( url ).append( "</rp>" );
		} else {
			if (urlField != null) {
				String url = (String)context.get(urlField);
				if (url != null) {
					sb.append( "<rp>" ).append( url ).append( "</rp>" );
				}
			}
		}
		if ( context != null )
		{
			sb.append( "<cd>" );
			KeyedCollection contextData = (KeyedCollection) context.getDataElement();
			
			//先将context全部输出
			for ( Iterator iterator = contextData.values().iterator(); iterator.hasNext(); )
			{
				String dataString = iterator.next().toString();
				sb.append( dataString );
			}
			
			
			if(output != null)
			{
			//再将output中配置的输出(可解决无法输出sessionContext中的数据问题)
				for (Iterator iterator = output.entrySet().iterator();iterator.hasNext();)
				{
					Entry entry = (Entry)iterator.next();
					String key = (String)entry.getKey();
					//如果output中配置的在当前context中，则不再重复输出
					if(contextData.containsKey( key ))
						continue;
					DataElement element = null;
					try
					{
						element = (DataElement)context.getDataElement(key);
					}
					catch(Exception e)
					{
						Trace.logWarn( EMPConstance.EMP_MVC, "配置了context中不存在的输出项：" + key );
						continue;
					}
					
					sb.append( element.toString() );
					
				}
			}
			sb.append( "</cd>" );
		}
		sb.append( "</root>" );
		return sb.toString();
	}

	/*
	 * setters & getters
	 */
	public String getUrl() 
	{
		return url;
	}

	public void setUrl( String url )
	{
		this.url = url;
	}

	public String getExceptionName()
	{
		return exceptionName;
	}

	public void setExceptionName( String exceptionName )
	{ 
		this.exceptionName = exceptionName; 
	}

	public String getUrlField() {
		return urlField;
	}

	public void setUrlField(String urlField) {
		this.urlField = urlField;
	}
	
	
}
