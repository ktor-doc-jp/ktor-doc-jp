$(document).ready(function () {
    function toggleSelection(e) {
        $(e).toggleClass('expand');
        const compact = $(e).find('.compact_open');
        if ($(e).hasClass('expand')) {
            document.getSelection().selectAllChildren(e.children[1]);
            compact.text('–');
        } else {
            document.getSelection().empty();
            compact.text('+');
        }
    }

    /*
    function getLeafDescendants(node, out = []) {
        const children = node.childNodes;
        if (children.length === 0) {
            out.push(node);
        } else {
            for (let n = 0; n < children.length; n++) {
                const child = children[n];
                getLeafDescendants(child, out);
            }
        }
        return out
    }

    function indexesOf(string, pattern) {
        var match,
            indexes = {};

        const regex2 = new RegExp(pattern, "g");

        while (match = regex2.exec(string)) {
            if (!indexes[match[0]]) indexes[match[0]] = [];
            indexes[match[0]].push(match.index);
        }

        return indexes;
    }

    function highlight(highlightText, node) {
        const children = getLeafDescendants(node);
        console.log('highlight', node);
        for (const v of children) {
            console.log(v.name, v.tagName, v.nodeValue, v);

            //const isLeaf = $v.children().length === 0;
            //if (isLeaf) {
            //    const text = $v.text();
            //    //console.log(v.nodeName);
            //    const indexes = indexesOf(text, highlightText);
            //    console.log(text, indexes);
            //} else {
            //    const text = $v.text();
            //    console.log('NO LEAF', text);
            //}

        }
    }
    */

    //function highlight(text, inputText) {
    //    var innerHTML = inputText.innerHTML;
    //    var index = innerHTML.indexOf(text);
    //    if (index >= 0) {
    //        innerHTML = innerHTML.substring(0,index) + "<span class='highlight'>" + innerHTML.substring(index,index+text.length) + "</span>" + innerHTML.substring(index + text.length);
    //        inputText.innerHTML = innerHTML;
    //    }
    //}

    var InstantSearch = {

        "highlight": function (container, highlightText) {
            var internalHighlighter = function (options) {

                    var id = {
                            container: "container",
                            tokens: "tokens",
                            all: "all",
                            token: "token",
                            className: "className",
                            sensitiveSearch: "sensitiveSearch"
                        },
                        tokens = options[id.tokens],
                        allClassName = options[id.all][id.className],
                        allSensitiveSearch = options[id.all][id.sensitiveSearch];


                    function checkAndReplace(node, tokenArr, classNameAll, sensitiveSearchAll) {
                        var nodeVal = node.nodeValue, parentNode = node.parentNode,
                            i, j, curToken, myToken, myClassName, mySensitiveSearch,
                            finalClassName, finalSensitiveSearch,
                            foundIndex, begin, matched, end,
                            textNode, span, isFirst;

                        for (i = 0, j = tokenArr.length; i < j; i++) {
                            curToken = tokenArr[i];
                            myToken = curToken[id.token];
                            myClassName = curToken[id.className];
                            mySensitiveSearch = curToken[id.sensitiveSearch];

                            finalClassName = (classNameAll ? myClassName + " " + classNameAll : myClassName);

                            finalSensitiveSearch = (typeof sensitiveSearchAll !== "undefined" ? sensitiveSearchAll : mySensitiveSearch);

                            isFirst = true;
                            while (true) {
                                if (finalSensitiveSearch)
                                    foundIndex = nodeVal.indexOf(myToken);
                                else
                                    foundIndex = nodeVal.toLowerCase().indexOf(myToken.toLowerCase());

                                if (foundIndex < 0) {
                                    if (isFirst)
                                        break;

                                    if (nodeVal) {
                                        textNode = document.createTextNode(nodeVal);
                                        parentNode.insertBefore(textNode, node);
                                    } // End if (nodeVal)

                                    parentNode.removeChild(node);
                                    break;
                                } // End if (foundIndex < 0)

                                isFirst = false;


                                begin = nodeVal.substring(0, foundIndex);
                                matched = nodeVal.substr(foundIndex, myToken.length);

                                if (begin) {
                                    textNode = document.createTextNode(begin);
                                    parentNode.insertBefore(textNode, node);
                                } // End if (begin)

                                span = document.createElement("span");
                                span.className += finalClassName;
                                span.appendChild(document.createTextNode(matched));
                                parentNode.insertBefore(span, node);

                                nodeVal = nodeVal.substring(foundIndex + myToken.length);
                            } // Whend

                        } // Next i
                    }; // End Function checkAndReplace

                    function iterator(p) {
                        if (p === null) return;

                        var children = Array.prototype.slice.call(p.childNodes), i, cur;

                        if (children.length) {
                            for (i = 0; i < children.length; i++) {
                                cur = children[i];
                                if (cur.nodeType === 3) {
                                    checkAndReplace(cur, tokens, allClassName, allSensitiveSearch);
                                }
                                else if (cur.nodeType === 1) {
                                    iterator(cur);
                                }
                            }
                        }
                    }; // End Function iterator

                    iterator(options[id.container]);
                } // End Function highlighter
            ;


            internalHighlighter(
                {
                    container: container
                    , all:
                        {
                            className: "highlighter"
                        }
                    , tokens: [
                        {
                            token: highlightText
                            , className: "highlight"
                            , sensitiveSearch: false
                        }
                    ]
                }
            ); // End Call internalHighlighter

        } // End Function highlight

    };

    function highlight(highlightText, node) {
        if (highlightText !== "") {
            InstantSearch.highlight(node, highlightText);
        }
    }

    //highlight('Ktor', document.body);

    $(".compact").each((index, element) => {
        //$(element).prepend(
        $(element).prepend(
            $('<div />').addClass("compact_open").text('+')
                .attr('title', 'Click here, or double click the content to expand and select or to compact')
                .on("click", () => {
                toggleSelection(element);
})
)
});

    $(".compact").on('dblclick', function (e) {
        window.event.preventDefault();
        toggleSelection(this);
    });

    $(".doc-content").find("h1,h2,h3,h4,h5,h6").filter("[id]").each(function () {
        const heading = $(this);

        heading.html($('<a />')
            .addClass('anchored-heading')
            .attr('href', '#' + heading.attr('id'))
            .html(heading.html())
        );
    });
    $(".note,.note-inner").filter("[id]").not(".exclude-anchor")
        .each(function () {
            $(this).addClass('anchored-note')
        })
        .click(function () {
            document.location.hash = $(this).attr('id');
        })
    ;

    //console.log($('#search-input').length);
    //$('#search-input').on('keypress', function(e) {
    //    console.log('keypress', e);
    //});
    $('#search-input').on('keydown', function (e) {
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
                    document.location.href = active.find('a').attr('href');
                    $('#search-input').val('').blur();
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

                for (let n = 0; n < 10; n++) { // Skip up to 10 links
                    active.removeClass('active');
                    if (active.length === 0 || active.prev().length === 0) {
                        active = search_container.find('li').last().addClass('active');
                    } else {
                        active = active.prev().addClass('active');
                    }
                    if (active.find('a').length !== 0) break;
                }

                break;
            case 40: // DOWN
                e.preventDefault();

                for (let n = 0; n < 10; n++) { // Skip up to 10 links
                    active.removeClass('active');
                    if (active.length === 0 || active.next().length === 0) {
                        active = search_container.find('li').first().addClass('active');
                    } else {
                        active = active.next().addClass('active');
                    }
                    // Found a link!
                    if (active.find('a').length !== 0) break;
                }

                //console.log('DOWN');
                break;
        }
    });
    $(document).on('keypress', function (e) {
        // Capture events just in when come from the document.body
        //if (e.target === document.body) {
        if ($(e.target).attr('id') !== 'search-input') {
            // 's' for search, and 't' as an alias compatible with github search in repo
            if (e.key === 's' || e.key === 't') {
                $('#search-input').focus();
                e.preventDefault();
            }
            // '#' for hash and 'a' for anchor
            if (e.key === '#' || e.key === 'a') {
                $('#search-input').focus();
                $('#search-input').val('#');
                e.preventDefault();
            }
        }
    });
    var hideTimeout = 0;
    $('#search-input').on('focus', function () {
        clearTimeout(hideTimeout);
        $('.search-results-inline-container').css('display', 'block');
        $(this).attr('placeholder', $(this).attr('data-placeholder-focus'));
        $(this).select();
    }).on('blur', function () {
        clearTimeout(hideTimeout);
        hideTimeout = setTimeout(function () {
            $('.search-results-inline-container').css('display', 'none');
        }, 250);
        $(this).attr('placeholder', $(this).attr('data-placeholder-blur'));
    });

    async function fetchSearch() {
        //console.log('Fetching /search.json'.trim());
        let res = await fetch("/search.json");
        let results = await res.json();
        for (const result of results) {
            result.category = String(result.category);
            result.title = String(result.title);
            result.caption = String(result.caption);
            result.categoryName = categoryNames[result.category] || result.category || "unknown";
            result.search = String('' + result.categoryName + ' ' + result.title + ' ' + result.caption + ' ' + result.keywords).normalizeForSearch();
        }
        return results;
    }

    let fetchSearchPromise = null;

    async function fetchSearchOnce() {
        fetchSearchPromise = fetchSearchPromise || fetchSearch();
        return await fetchSearchPromise;
    }

    // Prefetch
    fetchSearchOnce();

    //const arrow = "↬⇒";
    const sectionSeparator = " <span class='search-separator'>↬</span> ";

    function getHeadings(h) {
        const hlevel = parseInt(h.tagName.substr(1));
        const $h = $(h);
        const prevAll = $h.prevAll(`h${hlevel - 1}`);
        const prev = prevAll[0];
        if (prev === undefined) {
            return [h];
        } else {
            return getHeadings(prev).concat([h]);
        }
    }

    async function updateSearchResults() {
        let query = $('#search-input').val().trim();
        let containsHash = query.indexOf('#') >= 0;
        let querySearch = query.normalizeForSearch();
        let querySearchForSection = querySearch.replace(/#/g, '');
        let searchResults = $('#search-results-container');
        let results = await fetchSearchOnce();
        let lines = [];
        let filteredResultsAll = results
                .sorted(composeComparers(
                    comparerBy((it) => sectionPriority(it.category)),
                    comparerBy((it) => it.priority || 0),
            comparerBy((it) => String(it.search))
    ))
    .filter((it) => String(it.search).match(querySearch))
    .sorted(composeComparers(
            comparerBy((it) => it.search.length >= 0 ? String(it.search).regexIndexOf(querySearch) : 0)
        ))
        ;

        let filteredResults = filteredResultsAll.slice(0, 10);

        $(".doc-content").find("h2,h3,h4,h5,h6").filter("[id]").each(function () {
            const $this = $(this);
            const id = $this.attr('id');
            const headings = getHeadings(this);
            const searchTextLC = headings.map((v) => ($(v).text() + " " + id).normalizeForSearch()).join(" ");
            const headerRealHtml = headings.map((v) => $(v).text().escapeHTML()).join(sectionSeparator);

            if (searchTextLC.match(querySearchForSection)) {
                lines.push(`<a href='#${id.escapeHTML()}'># ${headerRealHtml}</a>`);
            }
        });

        for (const result of filteredResults) {
            lines.push(`<a href="${result.url.escapeHTML()}" title="${result.title.escapeHTML()}"><span class="search-section ${result.categoryName.escapeHTML()}">${result.categoryName.escapeHTML()}</span> - ${result.title.escapeHTML()} - ${result.caption.escapeHTML()}</a>`);
        }
        if (filteredResults.length < filteredResultsAll.length) {
            const invisibleLinks = filteredResultsAll.length - filteredResults.length;
            lines.push(`<small style="color:#999;">And ${invisibleLinks} more...</small>`);
        }
        if (query !== '' && !containsHash) {
            lines.push(`<a href="https://www.google.com/search?q=site:ktor.io+-site:*.ktor.io+${encodeURIComponent(query.trim())}">Search <code>${query.trim().escapeHTML()}</code> in google site:ktor.io</a>`)
        }
        let outLines = [];
        for (let n = 0; n < lines.length; n++) {
            const line = lines[n];
            const isHash = line.indexOf(">#") >= 0;
            const active = (n === 0) ? ' active' : '';
            const google = (line.indexOf('google.com/search') >= 0) ? ' google-search' : '';
            const hash = isHash ? ' hash' : '';
            //console.log(line);
            outLines.push(`<li class="${active}${google}${hash}">${line}</li>`);
        }
        if (outLines.length === 0) {
            outLines.push('<li>No results found</li>');
        }
        searchResults.html(outLines.join(''));
        const squery = query.replace('#', '');
        if (squery !== "") {
            highlight(squery.replace('#', ''), searchResults[0]);
        }
    }

    $('#search-input').on('focus', function () {
        //console.log('focus');
        //search-results-container
        updateSearchResults();
    }).on('change', function () {
        //console.log('change');
        updateSearchResults();
    }).on('keyup', function (e) {
        //console.log('keyup');
        if ([13, 16, 20, 37, 38, 39, 40, 91].indexOf(e.which) < 0) {
            updateSearchResults();
        }
    });

    function hashChanged() {
        const hash = document.location.hash;
        $('.anchored-heading-fixed').removeClass('anchored-heading-fixed');
        if (hash.startsWith('#') && hash.indexOf("&") < 0 && hash.indexOf("=") < 0 && hash.indexOf('"') < 0) {
            $(hash).find('a').addClass('anchored-heading-fixed');
        }
    }

    window.onhashchange = (e) => {
        hashChanged();
    };
    hashChanged();

    $('.artifact-tabs a.artifact, .gradle-tabs a.artifact').click(function (e) {
        e.preventDefault();
        $(this).tab('show');
    })

    function selectScriptLanguage(language) {
        $('.current-language').html(language + '<span class="caret"></span>');

        switch (language) {
            case 'Maven':
                $(".kts").hide();
                $(".groovy").hide();
                $(".maven").show();
                break;
            case 'Groovy':
                $(".kts").hide();
                $(".groovy").show();
                $(".maven").hide();
                break;
            case 'Kotlin':
            default:
                $(".kts").show();
                $(".groovy").hide();
                $(".maven").hide();
                break;
        }
    }

    selectScriptLanguage("Kotlin");

    $('.artifact-tabs a.scriptlang, .gradle-tabs a.scriptlang').click(function (e) {
        e.preventDefault();

        var language = this.innerText;
        selectScriptLanguage(language);
    })
});


String.prototype.normalizeForSearch = function () {
    return this.toLowerCase().replace(/[\s\/,.;\-]+/g, '');
};

String.prototype.escapeHTML = function () {
    return String(this)
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
};

Array.prototype.sorted = function (method) {
    let out = this.slice();
    out.sort(method);
    return out;
};

function comparerBy(selector) {
    return function (a, b) {
        let ra = selector(a), rb = selector(b);
        if (ra < rb) return -1;
        if (ra > rb) return +1;
        return 0;
    }
}

function composeComparers(...comparers) {
    return function (a, b) {
        for (const comparer of comparers) {
            const result = comparer(a, b);
            if (result !== 0) return result;
        }
        return 0;
    }
}

function sectionPriority(category) {
    switch (String(category).toLowerCase().replace(/\s+/, '')) {
        case 'quickstart': return 0;
        case 'servers': return 1;
        case 'clients': return 2;
        case 'features': return 3;
        case 'advanced': return 4;
        case 'samples': return 5;
        case 'drafts': return 6;
    }
    return 7;
}

String.prototype.regexIndexOf = function (regex, startpos) {
    var indexOf = this.substring(startpos || 0).search(regex);
    return (indexOf >= 0) ? (indexOf + (startpos || 0)) : indexOf;
};

String.prototype.regexLastIndexOf = function (regex, startpos) {
    regex = (regex.global) ? regex : new RegExp(regex.source, "g" + (regex.ignoreCase ? "i" : "") + (regex.multiLine ? "m" : ""));
    if (typeof (startpos) == "undefined") {
        startpos = this.length;
    } else if (startpos < 0) {
        startpos = 0;
    }
    var stringToWorkWith = this.substring(0, startpos + 1);
    var lastIndexOf = -1;
    var nextStop = 0;
    while ((result = regex.exec(stringToWorkWith)) != null) {
        lastIndexOf = result.index;
        regex.lastIndex = ++nextStop;
    }
    return lastIndexOf;
};