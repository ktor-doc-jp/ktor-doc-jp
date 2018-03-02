$(document).ready(function() {
    $(".compact").on("click", function(e) {
        $(this).toggleClass('expand')
    });

    $(".doc-content").find("h1,h2,h3,h4,h5,h6").filter("[id]").each(function() {
        var heading = $(this);

        heading.html($('<a />')
            .addClass('anchored-heading')
            .attr('href', '#' + heading.attr('id'))
            .html(heading.html())
        );
    });
});