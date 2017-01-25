# Examinating [![Build Status][travis-badge]][travis-link]
A Website for students at Trinity College Dublin to more easily navigate the past examination papers.

It can be found at: https://tcdpastpapers.firebaseapp.com/

## Overview
The idea for this project is to mimic what has already been done for Computer Science students with www.github.com/nating/trinity-cs-website, for students of every faculty of the college.  

## Where we at?
The src files are java programs that scrape the pages of various different formats at https://www.tcd.ie/academicregistry/exams/ and place past paper information into CSV or JSON files..

The CSV & JSON files hold information about every past paper that was scraped from the trinity web pages.

The html files are being hosted on https://tcdpastpapers.firebaseapp.com/ and the JSON files are being stored on the server there. 
The javascript in the html files can access the csv files here on GitHub in the repo to access the data.   
This code needs to be changed to access the JSON in the database so that the entire csv does not have to be read in every time the page is loaded, and the data can be more quickly queried.  

The code needs to be written in the src files to write the JSON file as a NoSQL Document type database rather than an SQL Relational database.

The webpages are looking alright at the moment. Code needs to be added to make it responsive.

[travis-badge]: https://img.shields.io/travis/nating/examinating.svg
[travis-link]: https://travis-ci.org/nating/examinating
