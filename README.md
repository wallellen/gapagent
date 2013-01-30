gapagent
========

a tool for apache cordova development

## Documentation

1. [gapagent-1.1.jar](http://pan.baidu.com/share/link?shareid=214862&uk=1578018496) [java_websocket.jar](http://pan.baidu.com/share/link?shareid=214863&uk=1578018496)
   下载这两个jar包，放到你的项目的libs中

2. 将原来继承DroidGap的主Activity发为继承org.gapagent.DroidGapAgent
  `MainActivity extends DroidGapAgent`

3. 在<script src=".../cordova-*.js">之前加入下面代码
