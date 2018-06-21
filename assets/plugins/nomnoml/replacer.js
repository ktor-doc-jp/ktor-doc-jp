window.addEventListener('load', function() {
    //console.log('loaded!', $);
    $('.nomnoml').each(function(index, item) {
        var $item = $(item);
        var canvas = document.createElement('canvas');
        var source = $item.text();
        nomnoml.draw(canvas, source);
        $item.replaceWith(canvas);
        $item.css("display", "block")
    });
});
