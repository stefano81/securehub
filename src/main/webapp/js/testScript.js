$(document).ready(function() {
    $("#result").hide();
    $("#searchForm").submit(function(){
        var dataString = 'query='+$("#query").val();
        $.ajax({
            url: "/cryptosocial/resource",
            dataType: "json",
            data: dataString,
            success: function(data) {
                var resultList = $("#result");
                resultList.empty();
                for(i = 0; i < data.length; i++) {
		    resultList.append('<li><a href="/cryptosocial/resource/' + data[i].id + '">' + data[i].name + '</a></li>');
                }

		resultList.show();
            },
        });
        return false;
    });
});
