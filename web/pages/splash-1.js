var reducido = window.matchMedia && window.matchMedia('(prefers-reduced-motion: reduce)').matches;
    setTimeout(function () { location.replace('welcome.html'); }, reducido ? 200 : 1600);
