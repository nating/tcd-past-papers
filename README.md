# Examinating [![Build Status][travis-badge]][travis-link]
A Website for students at Trinity College Dublin to more easily navigate the past examination papers.

It can be found at: https://nating.github.io/examinating/

## Overview
The idea for this project is to mimic what has already been done for Computer Science students with www.github.com/nating/trinity-cs-website, for students of every faculty of the college.  

## Where we at?
The src files are java program that scrape the pages of various different formats at https://www.tcd.ie/academicregistry/exams/ and place past paper information into csv files.

The CSV files hold information about every past paper that was scraped from the trinity web pages.

The html files *can* access the csv files to display the right links, though at the moment the layout of the information is being worked on. Cookies need to be added to remember what modules each user is enrolled in, and CSS needs to be added to make the website nice for users.

[travis-badge]: https://travis-ci.org/nating/examinating.png?branch=master
[travis-link]: https://travis-ci.org/nating/examinating
