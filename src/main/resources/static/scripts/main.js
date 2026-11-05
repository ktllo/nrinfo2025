function toggle_display(cont_name) {
    if ($('#cont_' + cont_name).is(':visible')) {
        //Hide it
        $('#cont_' + cont_name).hide();
        $('#hint_' + cont_name).text('+');
    } else {
        //Show it
        $('#cont_' + cont_name).show();
        $('#hint_' + cont_name).text('-');
    }
}

function formatDate(date, format =  "YYYY-MM-DD HH:mm:ss") {
    return moment(date, "YYYY-MM-DD HH:mm:ss").format(format);
}

function addStationAutoComplete(dispElem, dataElem) {
    console.log(`DispElem ${dispElem}; Data Elem: ${dataElem}`);
    console.log($("#"+dispElem));
    $("#"+dispElem).autocomplete({
        minLength: 3,
        source: function(request, response) {
            var inputTerm = request.term;
            $.ajax({
                url: "/api/stations/search?q="+inputTerm,
                method: "GET",
                success: function(data) {
                    var resp = [];
                    for (const station of data.result) {
                        resp.push(station);
                    }
                    response(resp);
                },
                error: function(data) {
                    response([]);
                }
            });
        },
        select: function(event, ui) {
            $('#'+dataElem).val(ui.item.tiplocCode);
            $("#"+dispElem).val(ui.item.displayName);
            return false;
        }
    }).autocomplete("instance")._renderItem = function(q, data) {
        var item = $("<li style='display: flow-root'>");
        item.append("<div style='display: flow; float: left'>"+data.displayName+"</div>");
        item.append("<div style='display: flow; float: right; font-size: small; z-index: -1;'>"+data.crsCode+"</div>");
        return item.appendTo(q);
    };
}