# TCD Past Papers [![Build Status][travis-badge]][travis-link]

<img src="/public/logo.png" width="100px"></img>

A Website for students at Trinity College Dublin to more easily navigate the past examination papers.

It is currently live at: https://tcdpastpapers.firebaseapp.com/

## Overview
The idea for this project is to mimic what has already been done for Computer Science students with www.github.com/nating/trinity-cs-website (https://nating.netsoc.ie), for students of every faculty of the college.  

## How it works
Past papers are organised questionably [here][https://www.tcd.ie/academicregistry/exams/past-papers/annual/].  

The past paper links have been scraped with the java programs in this repo's src files.  

The site is being hosted on [Firebase][firebase] and the past paper links are in a JSON file in a NoSQL (😬) server there.

Users select the modules they study and cookies store this information in their browser.  

When users view the homepage, the links for their past papers in their modules show up for them.

## Where we at?
The site is currently live and has active users.

There is more to be done in terms of UI and Usability. (See Issues)

[travis-badge]: https://img.shields.io/travis/nating/examinating.svg
[travis-link]: https://travis-ci.org/nating/examinating
[firebase]: https://firebase.google.com/
