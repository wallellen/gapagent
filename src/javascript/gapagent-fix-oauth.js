(function() {

    window.LocalFileSystem = {
        PERSISTENT : 1
    };
    window.requestFileSystem = window.requestFileSystem || window.webkitRequestFileSystem;


    (function() {

        var postInterval;
        var onloadstart;

        window.superOpen = window.open;

        window.addEventListener('message', function(e) {
            if(postInterval) {
                clearInterval(postInterval);
            }
            onloadstart({url:e.data});
        }, false);

        window.open = function(url) {
            var win = window.superOpen.apply(window, arguments);
            win.super_addEventListener = win.addEventListener;
            win.addEventListener = function(type, listener, flag) {
                if(type === 'loadstart') {
                    onloadstart = listener;
                } else {
                    win.super_addEventListener.apply(win, arguments);
                }
            };
            postInterval = setInterval(function() {
                win.postMessage(document.URL, '*');
            }, 100);

            return win;
        };
    })();

})();