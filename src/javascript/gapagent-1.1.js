(function() {

    var gapagent = {

        DEBUG : true,

        errors : [],

        printErrors : function(printStack) {
            var errors = gapagent.errors;
            for(var i=0; i<errors.length; i++) {
                var e = errors[i];
                if(printStack) {
                    console.log(e, e.stack);
                } else {
                    console.log(e);
                }
            }
        }
    };

    var tools = {
        getConfigUrl : function() {
            var scripts = document.getElementsByTagName('script');
            var src;
            for(var i= 0, len=scripts.length; i<len; i++) {
                var s = scripts[i].src;
                if(~s.indexOf("gapagent")) {
                    src = s;
                }
            }
            return src.substr(src.indexOf('#')+1);
        }
    };


    /*
    *   a client to communicate the websocket server of gapagent android side
    */
    var client = new function() {
        var url = 'ws://' + tools.getConfigUrl();
        var socket;

        this.connect = function(params) {
            socket = new WebSocket(url);
            socket.onopen = function() {
                params.onOpen && params.onOpen();
                socket.onmessage = function(message) {
                    params.onMessage && params.onMessage(message.data);
                };
                socket.onclose = function() {
                    params.onClose && params.onClose();
                };
            };
        };

        this.close = function() {
            socket && socket.close();
        };

        this.send = function(data) {
            if(typeof data === 'object') {
                data = _json.stringify(data);
            }
            socket.send(data);
        }
    };


    /**
     * in web side, there are not exposed js api from native
     * so we implements the _cordovaNative api to btain cordova.exec arguments,
     * and then send the arguments to android side via websocket
     */

    // _cordovaNative
    var cordovaNativeApi = {
        exec : function(service, action, callbackId, argsJson) {
            try {
                var data = {
                    service : service,
                    action : action,
                    callbackId : callbackId,
                    args : _json.parse(argsJson)
                };
                if(gapagent.DEBUG) {
                    console && console.log('[gapagent debug] send : ' + _json.stringify(data));
                }
                client.send(data);
            } catch(e) {
                if(gapagent.DEBUG) {
                    console.log(e);
                }
                gapagent.errors.push(e);
            }
        },
        retrieveJsMessages : function() {},
        setNativeToJsBridgeMode : function() {}
    };

    /**
     * mock splashscreen
     */
    navigator.splashscreen = { hide : function() {} };


    var _json = function () {
        var m = {
                '\b': '\\b',
                '\t': '\\t',
                '\n': '\\n',
                '\f': '\\f',
                '\r': '\\r',
                '"' : '\\"',
                '\\': '\\\\'
            },
            s = {
                'boolean': function (x) {
                    return String(x);
                },
                number: function (x) {
                    return isFinite(x) ? String(x) : 'null';
                },
                string: function (x) {
                    if (/["\\\x00-\x1f]/.test(x)) {
                        x = x.replace(/([\x00-\x1f\\"])/g, function(a, b) {
                            var c = m[b];
                            if (c) {
                                return c;
                            }
                            c = b.charCodeAt();
                            return '\\u00' +
                                Math.floor(c / 16).toString(16) +
                                (c % 16).toString(16);
                        });
                    }
                    return '"' + x + '"';
                },
                object: function (x) {
                    if (x) {
                        var a = [], b, f, i, l, v;
                        if (x instanceof Array) {
                            a[0] = '[';
                            l = x.length;
                            for (i = 0; i < l; i += 1) {
                                v = x[i];
                                f = s[typeof v];
                                if (f) {
                                    v = f(v);
                                    if (typeof v == 'string') {
                                        if (b) {
                                            a[a.length] = ',';
                                        }
                                        a[a.length] = v;
                                        b = true;
                                    }
                                }
                            }
                            a[a.length] = ']';
                        } else if (x instanceof Object) {
                            a[0] = '{';
                            for (i in x) {
                                v = x[i];
                                f = s[typeof v];
                                if (f) {
                                    v = f(v);
                                    if (typeof v == 'string') {
                                        if (b) {
                                            a[a.length] = ',';
                                        }
                                        a.push(s.string(i), ':', v);
                                        b = true;
                                    }
                                }
                            }
                            a[a.length] = '}';
                        } else {
                            return;
                        }
                        return a.join('');
                    }
                    return 'null';
                }
            };
        return {
            stringify: function (v) {
                var f = s[typeof v];
                if (f) {
                    v = f(v);
                    if (typeof v == 'string') {
                        return v;
                    }
                }
                return null;
            },
            parse: function (text) {
                try {
                    return !(/[^,:{}\[\]0-9.\-+Eaeflnr-u \n\r\t]/.test(
                        text.replace(/"(\\.|[^"\\])*"/g, ''))) &&
                        eval('(' + text + ')');
                } catch (e) {
                    return false;
                }
            }
        };
    }();

    function start() {
        setTimeout(function() {
            client.connect({
                onOpen : function() {
                    var channel = cordova.require("cordova/channel");
                    channel.onNativeReady.fire();
                },
                onMessage : function(message) {
                    try {
                        if(gapagent.DEBUG) {
                            console.log('[gapagent debug] receive : ', message);
                        }
                        window.eval(message);
                    } catch(e) {
                        if(gapagent.DEBUG) {
                            console && console.log(e);
                        }
                        gapagent.errors.push(e);
                    }
                }
            });
        }, 500);
    }

    window._cordovaNative = cordovaNativeApi;
    window.gapagent = gapagent;

    var checkCount = 100;
    var checkCordovaLoaded = setInterval(function() {
        if(window.cordova && checkCount-- > 0) {
            checkCount = 0;
            start();
        } else {
            checkCordovaLoaded && clearInterval(checkCordovaLoaded);
        }
    }, 100);



})();