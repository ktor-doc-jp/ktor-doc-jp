---
title: Generator
caption: Generate a Ktor project 
category: quickstart
#permalink: /start
#redirect_from:
#  - /quickstart/quickstart/generator.html
---

<https://ktor.io/start>

<div id="generator_id"></div>

<script type="text/javascript">
window.addEventListener('message', function(event) {
    //console.log(event);
    //console.log(event.data);
    if (event.data && event.data.type == "updateHash") {
        location.hash = event.data.value.replace(/^#/, '');
    }
});
document.getElementById('generator_id').innerHTML = '<iframe src="{{ site.ktor_init_tools_url }}' + location.hash.replace(/"/g, '\\"') + '" style="border:1px solid #343a40;width:100%;height:500px;"></iframe>';
</script>
