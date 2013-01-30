gapagent
========

a tool for apache cordova development

# Usage

1. [gapagent-1.1.jar](http://pan.baidu.com/share/link?shareid=214862&uk=1578018496) [java_websocket.jar](http://pan.baidu.com/share/link?shareid=214863&uk=1578018496)
   下载这两个jar包，放到你的项目的libs中

2. 将原来继承DroidGap改为继承org.gapagent.DroidGapAgent

   `MainActivity extends DroidGapAgent`

3. 在cordova-*.js"之前加入下面代码, 其中[phone_id]为手机ip地址

   `<script src="path..to/gapagent-1.1.js#[phone_ip]:8989"></script>`
