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
    $(".note").filter("[id]")
        .each(function() {
            $(this).addClass('anchored-note')
        })
        .click(function() {
            document.location.hash = $(this).attr('id');
        })
    ;

    //console.log($('#search-input').length);
    //$('#search-input').on('keypress', function(e) {
    //    console.log('keypress', e);
    //});
    $('#search-input').on('keydown', function(e) {
        const search_container = $('.search-results');
        let active = search_container.find('li.active');
        //console.log(active);
        //console.log('keydown', active);
        switch (e.keyCode) {
            case 13: // RETURN
                //console.log('RETURN');
                if (active.length === 0) {
                    // Do nothing, do the normal behaviour.
                } else {
                    e.preventDefault();
                    $('#search-input').val('').blur();
                    document.location.href = active.find('a').attr('href');
                }
                break;
            case 27: // ESC
                $('#search-input').val('');

                if (window.jekyllSearch) {
                    window.jekyllSearch.emptyResultsContainer();
                }

                e.preventDefault();
                $(this).blur();

                break;
            case 38: // UP
                e.preventDefault();
                //console.log('UP');

                active.removeClass('active');
                if (active.length === 0 || active.prev().length === 0) {
                    active = search_container.find('li').last().addClass('active');
                } else {
                    active.prev().addClass('active');
                }

                break;
            case 40: // DOWN
                e.preventDefault();

                active.removeClass('active');
                if (active.length === 0 || active.next().length === 0) {
                    active = search_container.find('li').first().addClass('active');
                } else {
                    active.next().addClass('active');
                }

                //console.log('DOWN');
                break;
        }
    });
    $(document).on('keypress', function(e) {
        // Capture events just in when come from the document.body
        //if (e.target === document.body) {
        if ($(e.target).attr('id') !== 'search-input') {
            if (e.key === 's') {
                $('#search-input').focus();
                e.preventDefault();
            }
            if (e.key === '#' || e.key === 'a') {
                $('#search-input').focus();
                $('#search-input').val('#');
                e.preventDefault();
            }
        }
    });
    var hideTimeout = 0;
    $('#search-input').on('focus', function() {
        clearTimeout(hideTimeout);
        $('.search-results-inline-container').css('display', 'block');
        $(this).attr('placeholder', $(this).attr('data-placeholder-focus'));
        $(this).select();
    }).on('blur', function() {
        clearTimeout(hideTimeout);
        hideTimeout = setTimeout(function() {
            $('.search-results-inline-container').css('display', 'none');
        }, 20);
        $(this).attr('placeholder', $(this).attr('data-placeholder-blur'));
    });
});