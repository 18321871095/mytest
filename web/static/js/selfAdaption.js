function setframeHeight(iframeId) {
    var reportFrame = document.getElementById(''+iframeId);
    // 获得页面中的所有行
    var tr = reportFrame.contentWindow.document.getElementsByTagName("tr");
    //为了避免报表加载结束后出现滚动条现象，这里将报表容器的overflow属性设置为hidden
    //由于在报表容器属性的设置只能在报表计算之后，所以用setTimeout来设置延迟执行时间，如果数据过多，请按照具体情况修改延迟时间
    setTimeout(function () {
        document.getElementById(''+iframeId).contentWindow.document.getElementById("content-container").style.overflow = "hidden";
        document.getElementById(''+iframeId).contentWindow.document.getElementById("content-container").style.overflowX = "auto";
    }, 10)
    // 由于报表页面还存在页边距，因此框架高度是大于所有行累计的高度的，这里赋一个初始值以表示边距的大小
    var height = 400;var flag=false;
    for (var i = 0; i < tr.length; i++) {
        //由于报表页面加载完成之后，可能会将单元格也在加载成一个tr，会导致重复计算，这里通过条件判断来获取行的tr
        if (tr[i].id.substring(0, 1) == "r") {
            flag=true;
            height = height + tr[i].offsetHeight;
        }
    }
    var frozen = reportFrame.contentWindow.document.getElementById('frozen-west');
    if(flag){
        reportFrame.height =frozen!=null? height/2-65:height-300;
    }else{
        reportFrame.height =frozen!=null? height/2-65:height;
    }

    return height;
}

function selfAdaption(iframeId) {
    var time=setInterval(function () {
        var height= setframeHeight(""+iframeId);
        if(height!=400){
            clearInterval(time);
            var contentPane = document.getElementById(""+iframeId).contentWindow.contentPane;
            contentPane.on("afterload",function(){
                setframeHeight(""+iframeId);
            });
        }
        else{
            console.log("select")
        }
    },1000);
}

function isIE() {
    if (!!window.ActiveXObject || "ActiveXObject" in window)
        return true;
    else
        return false;
}