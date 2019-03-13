package vegas.render
import vegas.Theme.Theme

/**
  * @author Aish Fenton.
  */
case class StaticHTMLRenderer(specJson: String, theme: Theme)
    extends BaseHTMLRenderer {

  def importsHTML(additionalImports: String*) =
    (JSImports ++ additionalImports)
      .map { s =>
        "<script src=\"" + s + "\" charset=\"utf-8\"></script>"
      }
      .mkString("\n")

  // 查看这里更新 js
  // https://vega.github.io/vega-lite/usage/embed.html
  def headerHTML(additionalImports: String*) =
    s"""
       |<html>
       |<head>
       |    <script src="https://cdn.jsdelivr.net/npm/vega@5.0.0"></script>
       |    <script src="https://cdn.jsdelivr.net/npm/vega-lite@3.0.0-rc14"></script>
       |    <script src="https://cdn.jsdelivr.net/npm/vega-embed@3.29.1"></script>
       |</head>
       |<style>
       |    #vg-tooltip-element table {
       |      font-size: 10;
       |    }
       |</style>
       |<body style="text-align: center;">
    """.stripMargin

  def plotHTML(name: String = this.defaultName) =
    s"""
       |    <div id='$name'></div>
       |    <script>
       |        var embedSpec = {
       |            spec: $specJson
       |        };
       |        vegaEmbed('#$name', embedSpec.spec, {theme: '${theme.toString.toLowerCase}', defaultStyle: true}).catch(console.warn);
       |    </script>

    """.stripMargin

  val footerHTML =
    """
      |</body>
      |</html>
    """.stripMargin

  def pageHTML(name: String = defaultName) = {
    headerHTML().trim + plotHTML(name) + footerHTML.trim
  }

  /**
    * Typically you'll want to use this method to render your chart. Returns a full page of HTML wrapped in an iFrame
    * for embedding within existing HTML pages (such as Jupyter).
    * XXX Also contains an ugly hack to resize iFrame height to fit chart, if anyone knows a better way open to suggestions
    *
    * @param name The name of the chart to use as an HTML id. Defaults to a UUID.
    * @return HTML containing iFrame for embedding
    */
  def frameHTML(name: String = defaultName) = {
    val frameName = "frame-" + name
    s"""
      |  <iframe id="${frameName}" sandbox="allow-scripts allow-same-origin" style="border: none; width: 100%" srcdoc="${xml.Utility
         .escape(pageHTML(name))}"></iframe>
      |  <script>
      |    (function() {
      |      function resizeIFrame(el, k) {
      |        var height = el.contentWindow.document.body.scrollHeight || '400'; // Fallback in case of no scroll height
      |        el.style.height = height + 'px';
      |        if (k <= 10) { setTimeout(function() { resizeIFrame(el, k+1) }, 1000 + (k * 250)) };
      |      }
      |      resizeIFrame(document.querySelector('#${frameName}'), 1);
      |    })(); // IIFE
      |  </script>
    """.stripMargin
  }

}
