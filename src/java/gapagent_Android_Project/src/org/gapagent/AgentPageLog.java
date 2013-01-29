package org.gapagent;

public abstract class AgentPageLog {
	
	String javascriptTpl = "gapagent.${func}('${tag}', '${msg}');";

	public void d(String tag, String msg) {
		this.callJavascript(this.buildJavascript("debug", tag, msg));
	}
	
	public void e(String tag, String msg) {
		this.callJavascript(this.buildJavascript("error", tag, msg));
	}
	
	public String buildJavascript(String func, String tag, String msg) {
		return javascriptTpl
				.replace("${func}", func)
				.replace("${tag}", this.escapeQuote(tag))
				.replace("${msg}", this.escapeQuote(msg));
	}
	
	public String escapeQuote(String str) {
		return str.replace("'", "\\'");
	}
	
	public abstract void callJavascript(String javascript);
	
}
