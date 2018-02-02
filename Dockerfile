FROM jekyll/jekyll:3.6.2

RUN gem install github-pages -v 175

WORKDIR /usr/src/app

EXPOSE 4000
ENTRYPOINT ["jekyll"]
#CMD jekyll serve -d /_site --watch --force_polling -H 0.0.0.0 -P 4000
