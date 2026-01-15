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