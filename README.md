# TCD Past Papers [![Build Status][travis-badge]][travis-link]

<img src="/public/logo.png" width="100px"></img>

A Website for students at Trinity College Dublin to more easily navigate the past examination papers.

It is currently live at: https://tcdpastpapers.firebaseapp.com/

## Overview
The idea for this project is to mimic what has already been done for Computer Science students with [this project][nating-repo] that you can see [here][nating-site], for students of every faculty of the college.  

## How it works
Trinity College's past papers are organised questionably [here][trinity-examinations].  

The past paper links have been scraped into a json with this repo's src files.  

The site is being hosted on [Firebase][firebase] and the past paper links are in a JSON file in a NoSQL (😬) server there.

Users select the modules they study and cookies store this information in their browser.  

When users view the homepage, the links for their past papers in their modules show up for them.

## Where are we at?
The site is currently live and has active users.

There is more to be done in terms of UI and Usability. [(See Issues)][issues]

### Thanks
Shout out to [Tiarnan McGrath][tiarnan] who helped me out a lot even though he'd be able to do it a lot better himself.

[firebase]: https://firebase.google.com/
[issues]: https://github.com/nating/tcd-past-papers/issues
[nating-repo]: www.github.com/nating/trinity-cs-website
[nating-site]: https://nating.netsoc.ie
[tiarnan]: https://github.com/tiarnann
[travis-badge]: https://img.shields.io/travis/nating/tcd-past-papers.svg
[travis-link]: https://travis-ci.org/nating/tcd-past-papers
[trinity-examinations]: https://www.tcd.ie/academicregistry/exams/past-papers/annual/
