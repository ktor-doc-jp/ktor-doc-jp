async function loadSnippet(url, into, lang) {
    const res = await((await(fetch(url))).text());
    const html = Prism.highlight(res, Prism.languages[lang], lang);
    //console.log(res);
    //$(into).text(res);
    $(into).html(html);
}

$('.code-snippet').each(function() {
    loadSnippet($(this).data('src'), this, $(this).data('lang') || 'json');
});
