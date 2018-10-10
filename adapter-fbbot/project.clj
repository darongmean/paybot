(defproject
  adapter-fbbot "1.0.0"
  :description "FIXME: write this!"
  :url "http://example.com/FIXME"

  :plugins [[lein-tools-deps "0.4.1"]]
  :middleware [lein-tools-deps.plugin/resolve-dependencies-with-deps-edn]
  :lein-tools-deps/config {:config-files [:install :user :project]})
